/*
  Copyright (C) 2020 Alexander Kapitman

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package ru.akman.maven.plugins.jpackage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.languages.java.jpms.JavaModuleDescriptor;
import org.codehaus.plexus.languages.java.jpms.LocationManager;
import org.codehaus.plexus.languages.java.jpms.ModuleNameSource;
import org.codehaus.plexus.languages.java.jpms.ResolvePathsRequest;
import org.codehaus.plexus.languages.java.jpms.ResolvePathsResult;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import ru.akman.maven.plugins.BaseToolMojo;
import ru.akman.maven.plugins.CommandLineBuilder;
import ru.akman.maven.plugins.CommandLineOption;

/**
 * The jpackage goal lets you create a custom runtime image with
 * the jpackage tool introduced in Java 13.
 *
 * <p>
 * The main idea is to avoid being tied to project artifacts and allow the user
 * to fully control the process of creating an image.
 * </p>
 */
@Mojo(
    name = "jpackage",
    requiresDependencyResolution = ResolutionScope.RUNTIME
//    defaultPhase = LifecyclePhase.VERIFY,
//    requiresProject = true,
//    aggregator = <false|true>,
//    configurator = "<role hint>",
//    executionStrategy = "<once-per-session|always>",
//    inheritByDefault = <true|false>,
//    instantiationStrategy = InstantiationStrategy.<strategy>,
//    requiresDependencyCollection = ResolutionScope.<scope>,
//    requiresDirectInvocation = <false|true>,
//    requiresOnline = <false|true>,
//    threadSafe = <false|true>,
)
// @Execute(
//    This will fork an alternate build lifecycle up to the specified phase
//    before continuing to execute the current one.
//    If no lifecycle is specified, Maven will use the lifecycle
//    of the current build.
//    phase = LifecyclePhase.VERIFY
//
//    This will execute the given goal before execution of this one.
//    The goal name is specified using the prefix:goal notation.
//    goal = "prefix:goal"
//
//    This will execute the given alternate lifecycle. A custom lifecycle
//    can be defined in META-INF/maven/lifecycle.xml.
//    lifecycle = "<lifecycle>", phase="<phase>"
// )
public class JPackageMojo extends BaseToolMojo {

  /**
   * The name of the subdirectory where the tool live.
   */
  private static final String TOOL_HOME_BIN = "bin";

  /**
   * The tool name.
   */
  private static final String TOOL_NAME = "jpackage";

  /**
   * Filename for temporary file contains the tool options.
   */
  private static final String OPTS_FILE = TOOL_NAME + ".opts";

  /**
   * Filename of a module descriptor.
   */
  private static final String DESCRIPTOR_NAME = "module-info.class";

  /**
   * Filename prefix for temporary file contains the launcher properties.
   */
  private static final String PROPS_PREFIX = "launcher.";

  /**
   * Filename suffix for temporary file contains the launcher properties.
   */
  private static final String PROPS_SUFFIX = ".properties";

  /**
   * The line ending pattern.
   */
  private static final String SPACES_PATTERN = "\\s+";

  /**
   * The char that will used to wrap an option string.
   */
  private static final char WRAP_CHAR = '\'';

  /**
   * Error message pattern for unability to resolve file path.
   */
  private static final String ERROR_RESOLVE =
      "Error: Unable to resolve file path for {0} [{1}]";

  /**
   * List of temporary files.
   */
  private final List<File> tempFiles = new ArrayList<>();

  /**
   * Resolved project dependencies.
   */
  private ResolvePathsResult<File> projectDependencies;

  /**
   * Resolved main module descriptor.
   */
  private JavaModuleDescriptor mainModuleDescriptor;

  /**
   * JPMS location manager.
   */
  @Component
  private LocationManager locationManager;

  /**
   * Specifies the path to the JDK home directory providing the tool needed.
   */
  @Parameter
  private File toolhome;


  // generic options


  /**
   * Specifies the location in which generated output files are placed.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--dest path</code></p>
   */
  @Parameter(
      defaultValue = "${project.build.directory}/jpackage"
  )
  private File dest;

  /**
   * Specifies the location in which temporary files are placed.
   * If specified, the directory will not be removed upon the task
   * completion and must be removed manually.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--temp path</code></p>
   */
  @Parameter
  private File temp;

  /**
   * Specifies the type of package
   * to create: { 'PLATFORM', 'IMAGE', 'EXE', 'MSI' }.
   *
   * <p>The jpackage CLI is: <code>--type {app-image|exe|msi}</code></p>
   */
  @Parameter(
      defaultValue = "PLATFORM"
  )
  private PackageType type;

  /**
   * Enable verbose tracing.
   *
   * <p>The jpackage CLI is: <code>--verbose</code></p>
   */
  @Parameter(
      defaultValue = "false"
  )
  private boolean verbose;

  /**
   * Specifies version of the application and/or package.
   *
   * <p>The jpackage CLI is: <code>--app-version version</code></p>
   */
  @Parameter
  private String appversion;

  /**
   * Specifies copyright for the application.
   *
   * <p>The jpackage CLI is: <code>--copyright copyright</code></p>
   */
  @Parameter
  private String copyright;

  /**
   * Specifies description of the application.
   *
   * <p>The jpackage CLI is: <code>--description description</code></p>
   */
  @Parameter
  private String description;

  /**
   * Specifies the name of subdirectory relative to the destination
   * directory in which files of generated application image are placed.
   *
   * <em>BUG: A name cannot contain spaces and unicode characters.</em>
   * <em>BUG: The names used to create the application image and
   * the application installer must match.</em>
   *
   * <p>The jpackage CLI is: <code>--name directory-name</code></p>
   */
  @Parameter
  private String name;

  /**
   * Specifies vendor of the application.
   *
   * <p>The jpackage CLI is: <code>--vendor vendor</code></p>
   */
  @Parameter
  private String vendor;


  // options for creating the application image


  /**
   * Specifies the location of the icon of the application launcher.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--icon path</code></p>
   */
  @Parameter
  private File icon;

  /**
   * Specifies the location of the input directory that contains
   * the files to be packaged. All files in the input directory
   * will be packaged into the application image.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--input path</code></p>
   */
  @Parameter
  private File input;


  // options for creating the runtime image


  /**
   * Specifies the location of the predefined runtime image (result of jlink)
   * that will be copied into the application image.
   * If not specified, jpackage will run jlink to create
   * the runtime image using options:
   *   - <code>--strip-debug</code>
   *   - <code>--no-header-files</code>
   *   - <code>--no-man-pages</code>
   *   - <code>--strip-native-commands</code>
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--runtime-image path</code></p>
   */
  @Parameter
  private File runtimeimage;

  /**
   * Specifies the module path. The path where the jlink tool discovers
   * observable modules: modular JAR files, JMOD files, exploded modules.
   * If this option is not specified, then the default module path
   * is $JAVA_HOME/jmods. This directory contains the java.base module
   * and the other standard and JDK modules. If this option is specified
   * but the java.base module cannot be resolved from it, then
   * the jlink command appends $JAVA_HOME/jmods to the module path.
   * Pass on --modulepath option to jlink.
   *
   * <p>
   * pathelements - passed to jlink as is
   * filesets - sets of files (without directories)
   * dirsets - sets of directories (without files)
   * dependencysets - sets of dependencies with specified includes and
   *                  excludes patterns (glob: or regex:) for file names
   *                  and regex patterns only for module names
   * </p>
   *
   * <p><pre>
   * &lt;modulepath&gt;
   *   &lt;pathelements&gt;
   *     &lt;pathelement&gt;mod.jar&lt;/pathelement&gt;
   *     &lt;pathelement&gt;mod.jmod&lt;/pathelement&gt;
   *     &lt;pathelement&gt;mods/exploded/mod&lt;/pathelement&gt;
   *   &lt;/pathelements&gt;
   *   &lt;filesets&gt;
   *     &lt;fileset&gt;
   *       &lt;directory&gt;${project.build.directory}&lt;/directory&gt;
   *       &lt;includes&gt;
   *         &lt;include&gt;*&#42;/*&lt;/include&gt;
   *       &lt;/includes&gt;
   *       &lt;excludes&gt;
   *         &lt;exclude&gt;*&#42;/*Empty.jar&lt;/exclude&gt;
   *       &lt;/excludes&gt;
   *       &lt;followSymlinks&gt;false&lt;/followSymlinks&gt;
   *     &lt;/fileset&gt;
   *   &lt;/filesets&gt;
   *   &lt;dirsets&gt;
   *     &lt;dirset&gt;
   *       &lt;directory&gt;target&lt;/directory&gt;
   *       &lt;includes&gt;
   *         &lt;include&gt;*&#42;/*&lt;/include&gt;
   *       &lt;/includes&gt;
   *       &lt;excludes&gt;
   *         &lt;exclude&gt;*&#42;/*Test&lt;/exclude&gt;
   *       &lt;/excludes&gt;
   *       &lt;followSymlinks&gt;true&lt;/followSymlinks&gt;
   *     &lt;/dirset&gt;
   *   &lt;/dirsets&gt;
   *   &lt;dependencysets&gt;
   *     &lt;dependencyset&gt;
   *       &lt;includeoutput&gt;false&lt;/includeoutput&gt;
   *       &lt;excludeautomatic&gt;false&lt;/excludeautomatic&gt;
   *       &lt;includes&gt;
   *         &lt;include&gt;glob:*&#42;/*.jar&lt;/include&gt;
   *         &lt;include&gt;regex:foo-(bar|baz)-.*?\.jar&lt;/include&gt;
   *       &lt;/includes&gt;
   *       &lt;includenames&gt;
   *         &lt;includename&gt;.*&lt;/includename&gt;
   *       &lt;/includenames&gt;
   *       &lt;excludes&gt;
   *         &lt;exclude&gt;glob:*&#42;/javafx.*Empty&lt;/exclude&gt;
   *       &lt;/excludes&gt;
   *       &lt;excludenames&gt;
   *         &lt;excludename&gt;javafx\..+Empty&lt;/excludename&gt;
   *       &lt;/excludenames&gt;
   *     &lt;/dependencyset&gt;
   *   &lt;/dependencysets&gt;
   * &lt;/modulepath&gt;
   * </pre></p>
   *
   * <p>The jpackage CLI is: <code>--modulepath path</code></p>
   */
  @Parameter
  private ModulePath modulepath;

  /**
   * Specifies the modules names (names of root modules) to add to
   * the runtime image. Their transitive dependencies will add too.
   * This module list, along with the main module (if specified)
   * will be passed to jlink as the --add-module argument.
   * If not specified, either just the main module (if module is specified),
   * or the default set of modules (if mainjar is specified) are used.
   *
   * <p><pre>
   * &lt;addmodules&gt;
   *   &lt;addmodule&gt;java.base&lt;/addmodule&gt;
   *   &lt;addmodule&gt;org.example.rootmodule&lt;/addmodule&gt;
   * &lt;/addmodules&gt;
   * </pre></p>
   *
   * <p>The jpackage CLI is: <code>--add-modules module [, module...]</code></p>
   */
  @Parameter
  private List<String> addmodules;

  /**
   * Link service provider modules and their dependencies.
   * Pass on --bind-services option to jlink.
   *
   * <p>The jpackage CLI is: <code>--bind-services</code></p>
   */
  @Parameter(
      defaultValue = "false"
  )
  private boolean bindservices;


  // options for creating the application launcher(s)


  /**
   * Specifies the main module (and optionally main class) of
   * the application. This module must be located on the module path.
   * When this option is specified, the main module will be linked
   * in the Java runtime image. Either module or mainjar option
   * can be specified but not both.
   *
   * <p>The jpackage CLI is: <code>--module module-name[/class-name]</code></p>
   */
  @Parameter
  private String module;

  /**
   * Specifies the main JAR of the application, specified as a path
   * relative to the input path, containing the main class.
   * Either module or mainjar option can be specified but not both.
   *
   * <p>The jpackage CLI is: <code>--main-jar jar-name</code></p>
   */
  @Parameter
  private String mainjar;

  /**
   * Specifies the qualified name of the application main class to execute.
   * This option can only be used if mainjar is specified.
   *
   * <p>The jpackage CLI is: <code>--main-class class-name</code></p>
   */
  @Parameter
  private String mainclass;

  /**
   * Specifies the command line arguments to pass to the main class
   * if no command line arguments are given to the launcher.
   *
   * <p>The jpackage CLI is: <code>--arguments args</code></p>
   */
  @Parameter
  private String arguments;

  /**
   * Specifies the options to pass to the Java runtime.
   *
   * <p>The jpackage CLI is: <code>--java-options opts</code></p>
   */
  @Parameter
  private String javaoptions;

  /**
   * Specifies options are added to, or used to overwrite, the original
   * command line options to build additional alternative launchers.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p><pre>
   * &lt;addlaunchers&gt;
   *   &lt;addlauncher&gt;
   *     &lt;name&gt;launcher1&lt;/name&gt;
   *     &lt;file&gt;config/jpackage/launcher1.properties&lt;/file&gt;
   *     &lt;module&gt;mainModule1Name/mainClass1Name&lt;/module&gt;
   *     &lt;mainjar&gt;mainJar1.jar&lt;/mainjar&gt;
   *     &lt;mainclass&gt;mainClass1Name&lt;/mainclass&gt;
   *     &lt;arguments&gt;--arg11 --arg12&lt;/arguments&gt;
   *     &lt;javaoptions&gt;-Xms128m -Xmx1024m&lt;/javaoptions&gt;
   *     &lt;appversion&gt;1.0.1&lt;/appversion&gt;
   *     &lt;icon&gt;config/jpackage/launcher1.ico&lt;/icon&gt;
   *     &lt;winconsole&gt;true&lt;/winconsole&gt;
   *   &lt;/addlauncher&gt;
   * &lt;/addlaunchers&gt;
   * </pre></p>
   *
   * <p>The jpackage CLI is: <code>--add-launcher name=path</code></p>
   */
  @Parameter
  private List<Launcher> addlaunchers;


  // platform dependent option for creating the application launcher


  /**
   * Enable creating a console launcher for the application, should be
   * specified for application which requires console interactions.
   *
   * <p>The jlink CLI is: <code>--win-console</code></p>
   */
  @Parameter(
      defaultValue = "false"
  )
  private boolean winconsole;


  // options for creating the application installable package


  /**
   * Specifies the location of the predefined application image that is used
   * to build an installable package.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--app-image path</code></p>
   */
  @Parameter
  private File appimage;

  /**
   * Specifies the location of a properties file that contains list of key,
   * value pairs. The keys "extension", "mime-type", "icon", and "description"
   * can be used to describe the association.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--file-associations path</code></p>
   */
  @Parameter
  private File fileassociations;

  /**
   * Specifies the relative sub-path under the default installation
   * location of the application for Windows, or absolute path of the
   * installation directory of the application for Mac or Linux.
   *
   * <p>The jpackage CLI is: <code>--install-dir name</code></p>
   */
  @Parameter
  private String installdir;

  /**
   * Specifies the location of a license file.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--license-file path</code></p>
   */
  @Parameter
  private File licensefile;

  /**
   * Specifies the location of a resources directory that override
   * jpackage resources. Icons, template files, and other resources
   * of jpackage can be overridden by adding replacement resources
   * to this directory.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--resource-dir path</code></p>
   */
  @Parameter
  private File resourcedir;


  // platform dependent options for creating the application
  // installable package (Windows)


  /**
   * Enable adding a dialog to choose a directory in which
   * the product is installed.
   *
   * <p>The jpackage CLI is: <code>--win-dir-chooser</code></p>
   */
  @Parameter(
      defaultValue = "false"
  )
  private boolean windirchooser;

  /**
   * Enable adding the application to the system menu.
   *
   * <p>The jpackage CLI is: <code>--win-menu</code></p>
   */
  @Parameter(
      defaultValue = "false"
  )
  private boolean winmenu;

  /**
   * Start menu group this application is placed in.
   *
   * <p>The jpackage CLI is: <code>--win-menu-group name</code></p>
   */
  @Parameter
  private String winmenugroup;

  /**
   * Enable requesting to perform an install on a per-user basis.
   *
   * <p>The jpackage CLI is: <code>--win-per-user-install</code></p>
   */
  @Parameter(
      defaultValue = "false"
  )
  private boolean winperuserinstall;

  /**
   * Enable creating a desktop shortcut for the application.
   *
   * <p>The jpackage CLI is: <code>--win-shortcut</code></p>
   */
  @Parameter(
      defaultValue = "false"
  )
  private boolean winshortcut;

  /**
   * UUID associated with upgrades for this package.
   *
   * <p>The jpackage CLI is: <code>--win-upgrade-uuid uuid</code></p>
   */
  @Parameter
  private String winupgradeuuid;


  // platform dependent options for creating the application
  // installable package (Mac)


  /**
   * An identifier that uniquely identifies the application for macOS.
   * Defaults to the main class name. May only use alphanumeric (A-Z,a-z,0-9),
   * hyphen (-), and period (.) characters.
   *
   * <p>The jpackage CLI is: <code>--mac-package-identifier id</code></p>
   */
  @Parameter
  private String macpackageidentifier;

  /**
   * Name of the application as it appears in the Menu Bar.
   * This can be different from the application name.
   * This name must be less than 16 characters long and be suitable for
   * displaying in the menu bar and the application Info window.
   * Defaults to the application name.
   *
   * <p>The jpackage CLI is: <code>--mac-package-name name</code></p>
   */
  @Parameter
  private String macpackagename;

  /**
   * When signing the application package, this value is prefixed
   * to all components that need to be signed that don't have
   * an existing package identifier.
   *
   * <p>The jpackage CLI is: <code>--mac-package-signing-prefix prefix</code>
   * </p>
   */
  @Parameter
  private String macpackagesigningprefix;

  /**
   * Request that the package be signed.
   *
   * <p>The jpackage CLI is: <code>--mac-sign</code></p>
   */
  @Parameter
  private boolean macsign;

  /**
   * Path of the keychain to search for the signing identity
   * (absolute path or relative to the current directory).
   * If not specified, the standard keychains are used.
   *
   * <em>BUG: A path cannot contain spaces and unicode characters.</em>
   *
   * <p>The jpackage CLI is: <code>--mac-signing-keychain path</code></p>
   */
  @Parameter
  private File macsigningkeychain;

  /**
   * Team name portion in Apple signing identities' names.
   * For example "Developer ID Application: ".
   *
   * <p>The jpackage CLI is: <code>--mac-signing-key-user-name name</code></p>
   */
  @Parameter
  private String macsigningkeyusername;


  // platform dependent options for creating the application
  // installable package (Linux)


  /**
   * Name for Linux package, defaults to the application name.
   *
   * <p>The jpackage CLI is: <code>--linux-package-name name</code></p>
   */
  @Parameter
  private String linuxpackagename;

  /**
   * Maintainer for .deb package.
   *
   * <p>The jpackage CLI is: <code>--linux-deb-maintainer email</code></p>
   */
  @Parameter
  private String linuxdebmaintainer;

  /**
   * Menu group this application is placed in.
   *
   * <p>The jpackage CLI is: <code>--linux-menu-group name</code></p>
   */
  @Parameter
  private String linuxmenugroup;

  /**
   * Required packages or capabilities for the application.
   *
   * <p>The jpackage CLI is: <code>--linux-package-deps</code></p>
   */
  @Parameter
  private boolean linuxpackagedeps;

  /**
   * Type of the license ("License: name" of the RPM .spec).
   *
   * <p>The jpackage CLI is: <code>--linux-rpm-license-type name</code></p>
   */
  @Parameter
  private String linuxrpmlicensetype;

  /**
   * Release value of the RPM name.spec file or Debian revision value
   * of the DEB control file.
   *
   * <p>The jpackage CLI is: <code>--linux-app-release name</code></p>
   */
  @Parameter
  private String linuxapprelease;

  /**
   * Group value of the RPM name.spec file or Section value
   * of DEB control file.
   *
   * <p>The jpackage CLI is: <code>--linux-app-category name</code></p>
   */
  @Parameter
  private String linuxappcategory;

  /**
   * Creates a shortcut for the application.
   *
   * <p>The jpackage CLI is: <code>--linux-shortcut</code></p>
   */
  @Parameter
  private boolean linuxshortcut;

  /**
   * Process options.
   *
   * @param cmdLine the command line builder
   *
   * @throws MojoExecutionException if any errors occurred
   */
  private void processOptions(final CommandLineBuilder cmdLine)
      throws MojoExecutionException {
    CommandLineOption opt = null;
    // dest
    opt = cmdLine.createOpt();
    opt.createArg().setValue("--dest");
    try {
      opt.createArg().setValue(dest.getCanonicalPath());
    } catch (IOException ex) {
      throw new MojoExecutionException(MessageFormat.format(
          ERROR_RESOLVE,
          "--dest",
          dest.toString()), ex);
    }
    // temp
    if (temp != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--temp");
      try {
        opt.createArg().setValue(temp.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--temp",
            temp.toString()), ex);
      }
    }
    // type
    if (type != null && !type.equals(PackageType.PLATFORM)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--type");
      switch (type) {
        case IMAGE:
          opt.createArg().setValue("app-image");
          break;
        case EXE:
          opt.createArg().setValue("exe");
          break;
        case MSI:
          opt.createArg().setValue("msi");
          break;
        default:
          // skip
      }
    }
    // verbose
    if (verbose) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--verbose");
    }
    // appversion
    if (!StringUtils.isBlank(appversion)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--app-version");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          appversion, WRAP_CHAR));
    }
    // copyright
    if (!StringUtils.isBlank(copyright)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--copyright");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          copyright, WRAP_CHAR));
    }
    // description
    if (!StringUtils.isBlank(description)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--description");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          description.replaceAll(SPACES_PATTERN, " "), WRAP_CHAR));
    }
    // name
    if (!StringUtils.isBlank(name)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--name");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          name, WRAP_CHAR));
    }
    // vendor
    if (!StringUtils.isBlank(vendor)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--vendor");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          vendor, WRAP_CHAR));
    }
    // icon
    if (icon != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--icon");
      try {
        opt.createArg().setValue(icon.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--icon",
            icon.toString()), ex);
      }
    }
    // input
    if (input != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--input");
      try {
        opt.createArg().setValue(input.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--input",
            input.toString()), ex);
      }
    }
    // runtimeimage
    if (runtimeimage != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--runtime-image");
      try {
        opt.createArg().setValue(runtimeimage.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--runtime-image",
            runtimeimage.toString()), ex);
      }
    }
    // module
    if (!StringUtils.isBlank(module)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--module");
      opt.createArg().setValue(StringUtils.stripToEmpty(
          module));
    }
    // mainjar
    if (!StringUtils.isBlank(mainjar)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--main-jar");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          mainjar, WRAP_CHAR));
    }
    // mainclass
    if (!StringUtils.isBlank(mainclass)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--main-class");
      opt.createArg().setValue(StringUtils.stripToEmpty(
          mainclass));
    }
    // arguments
    if (!StringUtils.isBlank(arguments)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--arguments");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          arguments, WRAP_CHAR));
    }
    // javaoptions
    if (!StringUtils.isBlank(javaoptions)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--java-options");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          javaoptions, WRAP_CHAR));
    }
    // addlaunchers
    if (addlaunchers != null && !addlaunchers.isEmpty()) {
      for (final Launcher addlauncher : addlaunchers) {
        final String name = StringUtils.stripToEmpty(addlauncher.getName());
        if (!StringUtils.isBlank(name)) {
          final File file = processLauncher(addlauncher);
          tempFiles.add(file);
          opt = cmdLine.createOpt();
          opt.createArg().setValue("--add-launcher");
          try {
            opt.createArg().setValue(name + "=" + file.getCanonicalPath());
          } catch (IOException ex) {
            throw new MojoExecutionException(MessageFormat.format(
                ERROR_RESOLVE,
                "--add-launcher",
                file.toString()), ex);
          }
        }
      }
    }
    // winconsole
    if (winconsole) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--win-console");
    }
    // appimage
    if (appimage != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--app-image");
      try {
        opt.createArg().setValue(appimage.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--app-image",
            appimage.toString()), ex);
      }
    }
    // fileassociations
    if (fileassociations != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--file-associations");
      try {
        opt.createArg().setValue(fileassociations.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--file-associations",
            fileassociations.toString()), ex);
      }
    }
    // installdir
    if (!StringUtils.isBlank(installdir)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--install-dir");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          installdir, WRAP_CHAR));
    }
    // licensefile
    if (licensefile != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--license-file");
      try {
        opt.createArg().setValue(licensefile.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--license-file",
            licensefile.toString()), ex);
      }
    }
    // resourcedir
    if (resourcedir != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--resource-dir");
      try {
        opt.createArg().setValue(resourcedir.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--resource-dir",
            resourcedir.toString()), ex);
      }
    }
    // windirchooser
    if (windirchooser) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--win-dir-chooser");
    }
    // winmenu
    if (winmenu) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--win-menu");
    }
    // winmenugroup
    if (!StringUtils.isBlank(winmenugroup)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--win-menu-group");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          winmenugroup, WRAP_CHAR));
    }
    // winperuserinstall
    if (winperuserinstall) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--win-per-user-install");
    }
    // winshortcut
    if (winshortcut) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--win-shortcut");
    }
    // winupgradeuuid
    if (!StringUtils.isBlank(winupgradeuuid)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--win-upgrade-uuid");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          winupgradeuuid, WRAP_CHAR));
    }
    // macpackageidentifier
    if (!StringUtils.isBlank(macpackageidentifier)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--mac-package-identifier");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          macpackageidentifier, WRAP_CHAR));
    }
    // macpackagename
    if (!StringUtils.isBlank(macpackagename)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--mac-package-name");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          macpackagename, WRAP_CHAR));
    }
    // macpackagesigningprefix
    if (!StringUtils.isBlank(macpackagesigningprefix)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--mac-package-signing-prefix");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          macpackagesigningprefix, WRAP_CHAR));
    }
    // macsign
    if (macsign) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--mac-sign");
    }
    // macsigningkeychain
    if (macsigningkeychain != null) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--mac-signing-keychain");
      try {
        opt.createArg().setValue(macsigningkeychain.getCanonicalPath());
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            ERROR_RESOLVE,
            "--mac-signing-keychain",
            macsigningkeychain.toString()), ex);
      }
    }
    // macsigningkeyusername
    if (!StringUtils.isBlank(macsigningkeyusername)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--mac-signing-key-user-name");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          macsigningkeyusername, WRAP_CHAR));
    }
    // linuxpackagename
    if (!StringUtils.isBlank(linuxpackagename)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--linux-package-name");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          linuxpackagename, WRAP_CHAR));
    }
    // linuxdebmaintainer
    if (!StringUtils.isBlank(linuxdebmaintainer)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--linux-deb-maintainer");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          linuxdebmaintainer, WRAP_CHAR));
    }
    // linuxmenugroup
    if (!StringUtils.isBlank(linuxmenugroup)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--linux-menu-group");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          linuxmenugroup, WRAP_CHAR));
    }
    // linuxpackagedeps
    if (linuxpackagedeps) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--linux-package-deps");
    }
    // linuxrpmlicensetype
    if (!StringUtils.isBlank(linuxrpmlicensetype)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--linux-rpm-license-type");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          linuxrpmlicensetype, WRAP_CHAR));
    }
    // linuxapprelease
    if (!StringUtils.isBlank(linuxapprelease)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--linux-app-release");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          linuxapprelease, WRAP_CHAR));
    }
    // linuxappcategory
    if (!StringUtils.isBlank(linuxappcategory)) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--linux-app-category");
      opt.createArg().setValue(PluginUtils.wrapOpt(
          linuxappcategory, WRAP_CHAR));
    }
    // linuxshortcut
    if (linuxshortcut) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--linux-shortcut");
    }
  }

  /**
   * Process additional launcher.
   * Create a temporary file contains the efective launcher properties.
   *
   * @param launcher the additional launcher
   *
   * @return the temporary file contains the efective launcher properties
   *
   * @throws MojoExecutionException if any errors occurred
   */
  private File processLauncher(final Launcher launcher)
      throws MojoExecutionException {
    final String name = launcher.getName();
    // get properties
    Properties props;
    try {
      props = launcher.getProperties(getCharset());
    } catch (IOException ex) {
      throw new MojoExecutionException(MessageFormat.format(
          "Error: Unable to read properties for launcher: [{0}]",
          name), ex);
    }
    // create a temporary properties file
    File file;
    try {
      file = Files.createTempFile(getBuildDir().toPath(),
          PROPS_PREFIX, PROPS_SUFFIX).toFile();
    } catch (IOException ex) {
      throw new MojoExecutionException(MessageFormat.format(
          "Error: Unable to create temporary file for launcher: [{0}]",
          name), ex);
    }
    // save properties to the temporary file
    try (BufferedWriter bw =
        Files.newBufferedWriter(file.toPath(), getCharset())) {
      props.store(bw, null);
    } catch (IOException ex) {
      throw new MojoExecutionException(MessageFormat.format(
          "Error: Unable to write temporary file for launcher: [{0}]",
          name), ex);
    }
    if (getLog().isDebugEnabled()) {
      getLog().debug(MessageFormat.format(
          "Found additional launcher: [{0}]", name)
          + System.lineSeparator()
          + props.toString());
    }
    return file;
  }

  /**
   * Resolve project dependencies.
   *
   * @return map of the resolved project dependencies
   *
   * @throws MojoExecutionException if any errors occurred while resolving
   *                                dependencies
   */
  private ResolvePathsResult<File> resolveDependencies()
      throws MojoExecutionException {

    // get project artifacts - all dependencies that this project has,
    // including transitive ones (depends on what phases have run)
    final Set<Artifact> artifacts = getProject().getArtifacts();
    if (getLog().isDebugEnabled()) {
      getLog().debug(PluginUtils.getArtifactSetDebugInfo(artifacts));
    }

    // create a list of the paths which will be resolved
    final List<File> paths = new ArrayList<>();

    // add the project output directory
    paths.add(getOutputDir());

    // SCOPE_COMPILE  - This is the default scope, used if none is specified.
    //                  Compile dependencies are available in all classpaths.
    //                  Furthermore, those dependencies are propagated to
    //                  dependent projects.
    // SCOPE_PROVIDED - This is much like compile, but indicates you expect
    //                  the JDK or a container to provide it at runtime.
    //                  It is only available on the compilation and
    //                  test classpath, and is not transitive.
    // SCOPE_SYSTEM   - This scope is similar to provided except that you
    //                  have to provide the JAR which contains it explicitly.
    //                  The artifact is always available and is not looked up
    //                  in a repository.    
    // SCOPE_RUNTIME  - This scope indicates that the dependency is not
    //                  required for compilation, but is for execution.
    //                  It is in the runtime and test classpaths, but not
    //                  the compile classpath.
    // SCOPE_TEST     - This scope indicates that the dependency is not
    //                  required for normal use of the application, and is
    //                  only available for the test compilation and execution
    //                  phases. It is not transitive.
    // SCOPE_IMPORT   - This scope indicates that the dependency is a managed
    //                  POM dependency i.e. only other POM into
    //                  the dependencyManagement section.

    // [ !SCOPE_TEST ] add the project artifacts files
    paths.addAll(artifacts.stream()
        .filter(a -> a != null && !Artifact.SCOPE_TEST.equals(a.getScope()))
        .map(a -> a.getFile())
        .collect(Collectors.toList()));

    // [ SCOPE_SYSTEM ] add the project system dependencies
    // getSystemPath() is used only if the dependency scope is system
    paths.addAll(getProject().getDependencies().stream()
        .filter(d -> d != null && !StringUtils.isBlank(d.getSystemPath()))
        .map(d -> new File(StringUtils.stripToEmpty(d.getSystemPath())))
        .collect(Collectors.toList()));

    // create request contains all information
    // required to analyze the project
    final ResolvePathsRequest<File> request =
        ResolvePathsRequest.ofFiles(paths);

    // this is used to resolve main module descriptor
    final File descriptorFile =
        getOutputDir().toPath().resolve(DESCRIPTOR_NAME).toFile();
    if (descriptorFile.exists() && !descriptorFile.isDirectory()) {
      request.setMainModuleDescriptor(descriptorFile);
    }

    // this is used to extract the module name
    if (getToolHomeDirectory() != null) {
      request.setJdkHome(getToolHomeDirectory());
    }

    // resolve project dependencies
    try {
      return locationManager.resolvePaths(request);
    } catch (IOException ex) {
      throw new MojoExecutionException(
          "Error: Unable to resolve project dependencies", ex);
    }

  }

  /**
   * Fetch the resolved main module descriptor.
   *
   * @return main module descriptor or null if it not exists
   */
  private JavaModuleDescriptor fetchMainModuleDescriptor() {
    final JavaModuleDescriptor descriptor =
        projectDependencies.getMainModuleDescriptor();
    if (descriptor == null) {
      // detected that the project is non modular
      if (getLog().isWarnEnabled()) {
        getLog().warn("The main module descriptor not found");
      }
    } else {
      if (getLog().isDebugEnabled()) {
        getLog().debug(MessageFormat.format(
            "Found the main module descriptor: [{0}]", descriptor.name()));
      }
    }
    return descriptor;
  }

  /**
   * Fetch path exceptions for every modulename which resolution failed.
   *
   * @return pairs of path exception file and cause
   */
  private Map<File, String> fetchPathExceptions() {
    return projectDependencies.getPathExceptions()
        .entrySet().stream()
        .filter(entry -> entry != null && entry.getKey() != null)
        .collect(Collectors.toMap(
            entry -> entry.getKey(),
            entry -> PluginUtils.getThrowableCause(entry.getValue())
        ));
  }

  /**
   * Fetch classpath elements.
   *
   * @return classpath elements
   */
  private List<File> fetchClasspathElements() {
    final List<File> result = projectDependencies.getClasspathElements()
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    if (getLog().isDebugEnabled()) {
      getLog().debug("Found classpath elements: " + result.size()
          + System.lineSeparator()
          + result.stream()
              .map(file -> file.toString())
              .collect(Collectors.joining(System.lineSeparator())));
    }
    return result;
  }

  /**
   * Fetch modulepath elements.
   *
   * @return modulepath elements
   */
  private List<File> fetchModulepathElements() {
    final List<File> result = projectDependencies.getModulepathElements()
        .keySet()
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    if (getLog().isDebugEnabled()) {
      getLog().debug("Found modulepath elements: " + result.size()
          + System.lineSeparator()
          + projectDependencies.getModulepathElements().entrySet().stream()
              .filter(entry -> entry != null && entry.getKey() != null)
              .map(entry -> entry.getKey().toString()
                  + (ModuleNameSource.FILENAME.equals(entry.getValue())
                      ? System.lineSeparator()
                          + "[!] Detected 'requires' filename based "
                          + "automatic module"
                          + System.lineSeparator()
                          + "[!] Please don't publish this project to "
                          + "a public artifact repository"
                          + System.lineSeparator()
                          + (mainModuleDescriptor != null
                              && mainModuleDescriptor.exports().isEmpty()
                                  ? "[!] APPLICATION"
                                  : "[!] LIBRARY")
                      : ""))
              .collect(Collectors.joining(System.lineSeparator())));
    }
    return result;
  }

  /**
   * Get path from the pathelements parameter.
   *
   * @return path contains parameter elements
   */
  private String getPathElements() {
    String result = null;
    if (modulepath != null) {
      final List<File> pathelements = modulepath.getPathElements();
      if (pathelements != null && !pathelements.isEmpty()) {
        result = pathelements.stream()
            .filter(Objects::nonNull)
            .map(file -> file.toString())
            .collect(Collectors.joining(File.pathSeparator));
        if (getLog().isDebugEnabled()) {
          getLog().debug(PluginUtils.getPathElementsDebugInfo("PATHELEMENTS",
              pathelements));
          getLog().debug(result);
        }
      }
    }
    return result;
  }

  /**
   * Get filesets from modulepath parameter.
   *
   * @return path contains filesets
   *
   * @throws MojoExecutionException if any errors occurred while resolving
   *                                a fileset
   */
  private String getFileSets() throws MojoExecutionException {
    String result = null;
    if (modulepath != null) {
      final List<FileSet> filesets = modulepath.getFileSets();
      if (filesets != null && !filesets.isEmpty()) {
        for (final FileSet fileSet : filesets) {
          final File fileSetDir;
          try {
            fileSetDir =
                PluginUtils.normalizeFileSetBaseDir(getBaseDir(), fileSet);
          } catch (IOException ex) {
            throw new MojoExecutionException(
                "Error: Unable to resolve fileset", ex);
          }
          result = Stream.of(getFileSetManager().getIncludedFiles(fileSet))
              .filter(fileName -> !StringUtils.isBlank(fileName))
              .map(fileName -> fileSetDir.toPath().resolve(
                  StringUtils.stripToEmpty(fileName)).toString())
              .collect(Collectors.joining(File.pathSeparator));
          if (getLog().isDebugEnabled()) {
            getLog().debug(PluginUtils.getFileSetDebugInfo("FILESET",
                fileSet, result));
          }
        }
      }
    }
    return result;
  }

  /**
   * Get dirsets from modulepath parameter.
   *
   * @return path contains dirsets
   *
   * @throws MojoExecutionException if any errors occurred while resolving
   *                                a dirset
   */
  private String getDirSets() throws MojoExecutionException {
    String result = null;
    if (modulepath != null) {
      final List<FileSet> dirsets = modulepath.getDirSets();
      if (dirsets != null && !dirsets.isEmpty()) {
        for (final FileSet dirSet : dirsets) {
          final File dirSetDir;
          try {
            dirSetDir =
                PluginUtils.normalizeFileSetBaseDir(getBaseDir(), dirSet);
          } catch (IOException ex) {
            throw new MojoExecutionException(
                "Error: Unable to resolve dirset", ex);
          }
          result = Stream.of(getFileSetManager().getIncludedDirectories(dirSet))
              .filter(dirName -> !StringUtils.isBlank(dirName))
              .map(dirName -> dirSetDir.toPath().resolve(
                  StringUtils.stripToEmpty(dirName)).toString())
              .collect(Collectors.joining(File.pathSeparator));
          if (getLog().isDebugEnabled()) {
            getLog().debug(PluginUtils.getFileSetDebugInfo("DIRSET",
                dirSet, result));
          }
        }
      }
    }
    return result;
  }

  /**
   * Get dependencysets from modulepath parameter.
   *
   * @return path contains dependencysets
   */
  private String getDependencySets() {
    String result = null;
    if (modulepath != null) {
      final List<DependencySet> dependencysets =
          modulepath.getDependencySets();
      if (dependencysets != null && !dependencysets.isEmpty()) {
        for (final DependencySet dependencySet : dependencysets) {
          result = getIncludedDependencies(dependencySet)
              .stream()
              .collect(Collectors.joining(File.pathSeparator));
          if (getLog().isDebugEnabled()) {
            getLog().debug(PluginUtils.getDependencySetDebugInfo(
                "DEPENDENCYSET", dependencySet, result));
          }
        }
      }
    }
    return result;
  }

  /**
   * Get the included project dependencies
   * defined in the specified dependencyset.
   *
   * @param depSet the dependencyset
   *
   * @return the set of the included project dependencies
   */
  private Set<String> getIncludedDependencies(final DependencySet depSet) {
    return projectDependencies.getPathElements().entrySet().stream()
        .filter(entry -> entry != null
            && entry.getKey() != null
            && filterDependency(depSet, entry.getKey(), entry.getValue()))
        .map(entry -> entry.getKey().toString())
        .collect(Collectors.toSet());
  }

  /**
   * Get the excluded project dependencies
   * defined in the specified dependencyset.
   *
   * @param depSet the dependencyset
   *
   * @return the set of the excluded project dependencies
   */
  private Set<String> getExcludedDependencies(final DependencySet depSet) {
    return projectDependencies.getPathElements().entrySet().stream()
        .filter(entry -> entry != null
            && entry.getKey() != null
            && !filterDependency(depSet, entry.getKey(), entry.getValue()))
        .map(entry -> entry.getKey().toString())
        .collect(Collectors.toSet());
  }

  /**
   * Checks whether the dependency defined by the file and
   * the module descriptor matches the rules defined in the dependencyset.
   * The dependency that matches at least one include pattern will be included,
   * but if the dependency matches at least one exclude pattern too,
   * then the dependency will not be included.
   *
   * @param depSet the dependencyset
   * @param file the dependency file
   * @param descriptor the dependency module descriptor
   *
   * @return will the dependency be accepted
   */
  private boolean filterDependency(final DependencySet depSet, final File file,
      final JavaModuleDescriptor descriptor) {

    if (descriptor == null) {
      if (getLog().isWarnEnabled()) {
        getLog().warn("Missing module descriptor: " + file);
      }
    } else {
      if (descriptor.isAutomatic() && getLog().isDebugEnabled()) {
        getLog().debug("Found automatic module: " + file);
      }
    }

    boolean isIncluded = false;

    if (depSet == null) {
      // include module by default
      isIncluded = true;
      // include automatic module by default
      if (descriptor != null && descriptor.isAutomatic()
          && getLog().isDebugEnabled()) {
        getLog().debug("Included automatic module: " + file);
      }
      // exclude output module by default
      if (file.compareTo(getOutputDir()) == 0) {
        isIncluded = false;
        if (getLog().isDebugEnabled()) {
          getLog().debug("Excluded output module: " + file);
        }
      }
    } else {
      if (descriptor != null && descriptor.isAutomatic()
          && depSet.isAutomaticExcluded()) {
        if (getLog().isDebugEnabled()) {
          getLog().debug("Excluded automatic module: " + file);
        }
      } else {
        if (file.compareTo(getOutputDir()) == 0) {
          if (depSet.isOutputIncluded()) {
            isIncluded = true;
            if (getLog().isDebugEnabled()) {
              getLog().debug("Included output module: " + file);
            }
          } else {
            if (getLog().isDebugEnabled()) {
              getLog().debug("Excluded output module: " + file);
            }
          }
        } else {
          isIncluded = matchesIncludes(depSet, file, descriptor)
              && !matchesExcludes(depSet, file, descriptor);
        }
      }
    }

    if (getLog().isDebugEnabled()) {
      getLog().debug(PluginUtils.getDependencyDebugInfo(file, descriptor,
          isIncluded));
    }

    return isIncluded;
  }

  /**
   * Checks whether the dependency defined by the file and
   * the module descriptor matches the include patterns
   * from the dependencyset.
   *
   * @param depSet the dependencyset
   * @param file the file
   * @param descriptor the module descriptor
   *
   * @return should the dependency be included
   */
  private boolean matchesIncludes(final DependencySet depSet, final File file,
      final JavaModuleDescriptor descriptor) {

    final String name = descriptor == null ? "" : descriptor.name();

    final List<String> includes = depSet.getIncludes();
    final List<String> includenames = depSet.getIncludeNames();

    boolean result = true;

    if (includenames == null || includenames.isEmpty()) {
      if (includes == null || includes.isEmpty()) {
        result = true;
      } else {
        result = pathMatches(includes, file.toPath());
      }
    } else {
      if (includes == null || includes.isEmpty()) {
        result = nameMatches(includenames, name);
      } else {
        result = pathMatches(includes, file.toPath())
            || nameMatches(includenames, name);
      }
    }
    return result;
  }

  /**
   * Checks whether the dependency defined by the file and
   * the module descriptor matches the exclude patterns
   * from the dependencyset.
   *
   * @param depSet the dependencyset
   * @param file the file
   * @param descriptor the module descriptor
   *
   * @return should the dependency be excluded
   */
  private boolean matchesExcludes(final DependencySet depSet, final File file,
      final JavaModuleDescriptor descriptor) {

    final String name = descriptor == null ? "" : descriptor.name();

    final List<String> excludes = depSet.getExcludes();
    final List<String> excludenames = depSet.getExcludeNames();

    boolean result = false;

    if (excludenames == null || excludenames.isEmpty()) {
      if (excludes == null || excludes.isEmpty()) {
        result = false;
      } else {
        result = pathMatches(excludes, file.toPath());
      }
    } else {
      if (excludes == null || excludes.isEmpty()) {
        result = nameMatches(excludenames, name);
      } else {
        result = pathMatches(excludes, file.toPath())
            || nameMatches(excludenames, name);
      }
    }
    return result;
  }

  /**
   * Checks if the path matches at least one of the patterns.
   * The pattern should be regex or glob, this is determined
   * by the prefix specified in the pattern.
   *
   * @param patterns the list of patterns
   * @param path the file path
   *
   * @return true if the path matches at least one of the patterns or
   *              if no patterns are specified
   */
  private boolean pathMatches(final List<String> patterns, final Path path) {
    for (final String pattern : patterns) {
      final PathMatcher pathMatcher =
          FileSystems.getDefault().getPathMatcher(pattern);
      if (pathMatcher.matches(path)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the name matches at least one of the patterns.
   * The pattern should be regex only.
   *
   * @param patterns the list of patterns
   * @param name the name
   *
   * @return true if the name matches at least one of the patterns or
   *              if no patterns are specified
   */
  private boolean nameMatches(final List<String> patterns, final String name) {
    for (final String pattern : patterns) {
      final Pattern regexPattern = Pattern.compile(pattern);
      final Matcher nameMatcher = regexPattern.matcher(name);
      if (nameMatcher.matches()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Process modules.
   *
   * @param cmdLine the command line builder
   *
   * @throws MojoExecutionException if any errors occurred
   */
  private void processModules(final CommandLineBuilder cmdLine)
      throws MojoExecutionException {
    CommandLineOption opt = null;
    // modulepath
    if (modulepath != null) {
      final StringBuilder path = new StringBuilder();
      final String pathElements = getPathElements();
      if (!StringUtils.isBlank(pathElements)) {
        path.append(StringUtils.stripToEmpty(pathElements));
      }
      final String fileSets = getFileSets();
      if (!StringUtils.isBlank(fileSets)) {
        if (path.length() != 0) {
          path.append(File.pathSeparator);
        }
        path.append(StringUtils.stripToEmpty(fileSets));
      }
      final String dirSets = getDirSets();
      if (!StringUtils.isBlank(dirSets)) {
        if (path.length() != 0) {
          path.append(File.pathSeparator);
        }
        path.append(StringUtils.stripToEmpty(dirSets));
      }
      final String dependencySets = getDependencySets();
      if (!StringUtils.isBlank(dependencySets)) {
        if (path.length() != 0) {
          path.append(File.pathSeparator);
        }
        path.append(StringUtils.stripToEmpty(dependencySets));
      }
      if (path.length() != 0) {
        opt = cmdLine.createOpt();
        opt.createArg().setValue("--module-path");
        opt.createArg().setValue(path.toString());
      }
    }
    // addmodules
    if (addmodules != null && !addmodules.isEmpty()) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--add-modules");
      opt.createArg().setValue(
          addmodules.stream().collect(Collectors.joining(",")));
    }
    // bindservices
    if (bindservices) {
      opt = cmdLine.createOpt();
      opt.createArg().setValue("--bind-services");
    }
  }

  /**
   * Execute goal.
   *
   * @throws MojoExecutionException if any errors occurred
   */
  @Override
  public void execute() throws MojoExecutionException {

    // Init
    init(TOOL_NAME, toolhome, TOOL_HOME_BIN); // from BaseToolMojo

    // Check version
    final JavaVersion toolJavaVersion = getToolJavaVersion();
    if (toolJavaVersion == null
        || !toolJavaVersion.atLeast(JavaVersion.JAVA_9)) {
      throw new MojoExecutionException(MessageFormat.format(
          "Error: At least {0} is required to use [{1}]", JavaVersion.JAVA_9,
          TOOL_NAME));
    }

    // Delete temporary directory if it exists
    if (temp != null) {
      if (getLog().isDebugEnabled()) {
        getLog().debug(MessageFormat.format(
            "Temporary directory: [{0}]", temp));
      }
      if (temp.exists() && temp.isDirectory()) {
        try {
          FileUtils.deleteDirectory(temp);
        } catch (IOException ex) {
          throw new MojoExecutionException(MessageFormat.format(
              "Error: Unable to delete temporary directory: [{0}]", temp), ex);
        }
      }
    }

    // Resolve and fetch project dependencies
    projectDependencies = resolveDependencies();
    mainModuleDescriptor = fetchMainModuleDescriptor();
    // final List<File> classpathElements = fetchClasspathElements();
    // final List<File> modulepathElements = fetchModulepathElements();
    final Map<File, String> pathExceptions = fetchPathExceptions();
    if (!pathExceptions.isEmpty() && getLog().isWarnEnabled()) {
      getLog().warn("Found path exceptions: " + pathExceptions.size()
          + System.lineSeparator()
          + pathExceptions.entrySet().stream()
              .map(entry -> entry.getKey().toString()
                  + System.lineSeparator()
                  + entry.getValue())
              .collect(Collectors.joining(System.lineSeparator())));
    }

    // Build command line and populate the list of the command options
    final CommandLineBuilder cmdLineBuilder = new CommandLineBuilder();
    cmdLineBuilder.setExecutable(getToolExecutable().toString());
    processOptions(cmdLineBuilder);
    processModules(cmdLineBuilder);
    final List<String> optsLines = new ArrayList<>();
    optsLines.add("# " + TOOL_NAME);
    optsLines.addAll(cmdLineBuilder.buildOptionList());
    if (getLog().isDebugEnabled()) {
      getLog().debug(optsLines.stream()
          .collect(Collectors.joining(System.lineSeparator(),
              System.lineSeparator(), "")));
    }

    // Save the list of command options to the file
    // will be used in the tool command line
    final Path cmdOptsPath = getBuildDir().toPath().resolve(OPTS_FILE);
    try {
      Files.write(cmdOptsPath, optsLines, getCharset());
    } catch (IOException ex) {
      throw new MojoExecutionException(MessageFormat.format(
          "Error: Unable to write command options to file: [{0}]",
          cmdOptsPath), ex);
    }
    tempFiles.add(cmdOptsPath.toFile());

    // Prepare command line with command options
    // specified in the file created early
    final Commandline cmdLine = new Commandline();
    cmdLine.setExecutable(getToolExecutable().toString());
    cmdLine.createArg().setValue("@" + cmdOptsPath.toString());

    // Execute command line
    int exitCode = 0;
    try {
      exitCode = execCmdLine(cmdLine); // from BaseToolMojo
    } catch (CommandLineException ex) {
      throw new MojoExecutionException(MessageFormat.format(
          "Error: Unable to execute [{0}] tool", TOOL_NAME), ex);
    }
    if (exitCode != 0) {
      if (getLog().isErrorEnabled()) {
        getLog().error(System.lineSeparator()
            + "Command options was: "
            + System.lineSeparator()
            + optsLines.stream()
                .collect(Collectors.joining(System.lineSeparator())));
      }
      throw new MojoExecutionException(MessageFormat.format(
          "Error: Tool execution failed [{0}] with exit code: {1}", TOOL_NAME,
          exitCode));
    }

    // Delete temporary files
    for (final File tempFile : tempFiles) {
      try {
        FileUtils.forceDelete(tempFile);
      } catch (IOException ex) {
        throw new MojoExecutionException(MessageFormat.format(
            "Error: Unable to delete temporary file: [{0}]", tempFile), ex);
      }
    }

  }

}
