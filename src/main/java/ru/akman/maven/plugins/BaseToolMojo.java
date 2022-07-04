/*
  Copyright (C) 2020 - 2022 Alexander Kapitman

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

package ru.akman.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Base class for creating a CLI tool Mojos.
 */
public abstract class BaseToolMojo extends AbstractMojo {

  /**
   * The name of JDK toolchain.
   */
  private static final String JDK = "jdk";

  /**
   * The name of the system environment JAVA_HOME variable.
   */
  private static final String JAVA_HOME = "JAVA_HOME";

  /**
   * The value for older major versions of Java.
   */
  private static final int OLD_MAJOR = 1;

  /**
   * The value from which new major versions of Java begin.
   */
  private static final int NEW_MAJOR = 9;

  /**
   * The value from which the most recent major versions of Java begin.
   */
  private static final int NEW_RECENT = 14;

  /**
   * The value for android major versions of Java.
   */
  private static final int ANDROID_MAJOR = 0;

  /**
   * The value for android minor versions of Java.
   */
  private static final int ANDROID_MINOR = 9;

  /**
   * The name of the subdirectory under JAVA_HOME where executables live.
   */
  private static final String JAVA_HOME_BIN = "bin";

  /**
   * The name of the system environment PATH variable.
   */
  private static final String PATH = "PATH";

  /**
   * The name of the system environment PATHEXT variable.
   */
  private static final String PATHEXT = "PATHEXT";

  /**
   * The version string pattern of CLI tool.
   */
  private static final String VERSION_PATTERN = "^(\\d+)(\\.(\\d+))?.*";

  /**
   * The version option of CLI tool.
   */
  private static final String VERSION_OPTION = "--version";

  /**
   * Project base directory (that containing the pom.xml file).
   */
  private File baseDir;
  
  /**
   * Project build directory (${project.basedir}/target).
   */
  private File buildDir;

  /**
   * Project output directory (${project.build.directory}/classes).
   */
  private File outputDir;

  /**
   * Project properties.
   */
  private Properties properties;

  /**
   * Default charset (${project.build.sourceEncoding}).
   */
  private Charset sourceEncoding = Charset.defaultCharset();

  /**
   * Fileset manager.
   */
  private FileSetManager fileSetManager;

  /**
   * All JDK toolchains available in user settings
   * independently from maven-toolchains-plugin.
   */
  private List<Toolchain> toolchains;

  /**
   * JDK toolchain from build context,
   * i.e. the toolchain selected by maven-toolchains-plugin.
   */
  private Toolchain toolchain;

  /**
   * Tool home directory.
   */
  private File toolHomeDirectory;

  /**
   * Tool executable.
   */
  private File toolExecutable;

  /**
   * Tool version.
   */
  private String toolVersion;

  /**
   * Tool corresponding java version.
   */
  private JavaVersion toolJavaVersion;

  /**
   * Toolchain manager.
   */
  @Component
  private ToolchainManager toolchainManager;

  /**
   * Build plugin manager.
   */
  @Component
  private BuildPluginManager pluginManager;

  /**
   * Maven project.
   */
  @Parameter(
      defaultValue = "${project}",
      readonly = true,
      required = true
  )
  private MavenProject project;

  /**
   * Maven session.
   */
  @Parameter(
      defaultValue = "${session}",
      readonly = true,
      required = true
  )
  private MavenSession session;

  /**
   * Get tool executable path from tool home.
   *
   * @param toolName the name of the tool (without extension)
   * @param toolHomeDir the tool home directory
   * @param toolBinDirName the name of subdirectory where the tool live
   *
   * @return tool executable path from tool home directory specified in
   *         configuration as toolhome parameter or null
   */
  private Path getExecutableFromToolHome(final String toolName,
      final File toolHomeDir, final String toolBinDirName) {
    Path executablePath = toolHomeDir == null
        ? null
        : resolveToolPath(toolName, toolHomeDir.toPath(), toolBinDirName);
    if (executablePath != null) {
      try {
        executablePath = executablePath.toRealPath();
        toolHomeDirectory = toolHomeDir;
        if (getLog().isDebugEnabled()) {
          getLog().debug(MessageFormat.format(
              "Executable (toolhome) for [{0}]: {1}", toolName,
              executablePath));
          getLog().debug(MessageFormat.format(
              "Home directory (toolhome) for [{0}]: {1}", toolName,
              toolHomeDirectory));
        }
      } catch (IOException ex) {
        if (getLog().isWarnEnabled()) {
          getLog().warn(MessageFormat.format(
              "Unable to resolve executable (toolhome) for [{0}]: {1}",
              toolName, executablePath), ex);
        }
      }
    }
    return executablePath;
  }

  /**
   * Get tool executable path from default JDK toolchain.
   *
   * @param toolName the name of the tool (without extension)
   *
   * @return tool executable path from JDK toolchain specified in
   *         configuration by toolchain plugin or null
   */
  @SuppressWarnings("deprecation") // DefaultJavaToolChain
  private Path getExecutableFromToolchain(final String toolName) {
    final String tcJavaHome = toolchain == null
        ? null
        : org.apache.maven.toolchain.java.DefaultJavaToolChain.class.cast(
            toolchain).getJavaHome();
    final String tcToolExecutable = toolchain == null
        ? null
        : toolchain.findTool(toolName);
    Path executablePath = null;
    if (!StringUtils.isBlank(tcJavaHome)
        && !StringUtils.isBlank(tcToolExecutable)) {
      try {
        executablePath = Paths.get(tcToolExecutable).toRealPath();
        toolHomeDirectory = new File(tcJavaHome);
        if (getLog().isDebugEnabled()) {
          getLog().debug(MessageFormat.format(
              "Executable (toolchain) for [{0}]: {1}", toolName,
              executablePath));
          getLog().debug(MessageFormat.format(
              "Home directory (toolchain) for [{0}]: {1}", toolName,
              toolHomeDirectory));
        }
      } catch (IOException ex) {
        if (getLog().isWarnEnabled()) {
          getLog().warn(MessageFormat.format(
              "Unable to resolve executable (toolchain) for [{0}]: {1}",
              toolName, executablePath), ex);
        }
      }
    }
    return executablePath;
  }

  /**
   * Get tool executable path from java home.
   *
   * @param toolName the name of the tool (without extension)
   *
   * @return tool executable path from JDK home directory specified in
   *              the system environment variable JAVA_HOME or null
   */
  private Path getExecutableFromJavaHome(final String toolName) {
    final File javaHomeDir = getJavaHome();
    Path executablePath = javaHomeDir == null
        ? null
        : resolveToolPath(toolName, javaHomeDir.toPath(), JAVA_HOME_BIN);
    if (executablePath != null) {
      try {
        executablePath = executablePath.toRealPath();
        toolHomeDirectory = javaHomeDir;
        if (getLog().isDebugEnabled()) {
          getLog().debug(MessageFormat.format(
              "Executable (javahome) for [{0}]: {1}", toolName,
              executablePath));
          getLog().debug(MessageFormat.format(
              "Home directory (javahome) for [{0}]: {1}", toolName,
              toolHomeDirectory));
        }
      } catch (IOException ex) {
        if (getLog().isWarnEnabled()) {
          getLog().warn(MessageFormat.format(
              "Unable to resolve executable (javahome) for [{0}]: {1}",
              toolName, executablePath), ex);
        }
      }
    }
    return executablePath;
  }

  /**
   * Get tool executable path from system path.
   *
   * @param toolName the name of the tool (without extension)
   *
   * @return tool executable path from paths specified in
   *              the system environment variable PATH or null
   */
  private Path getExecutableFromSystemPath(final String toolName) {
    final List<Path> systemPath = getSystemPath();
    Path executablePath = null;
    for (final Path path : systemPath) {
      executablePath = resolveToolPath(toolName, path, null);
      if (executablePath != null) {
        break;
      }
    }
    if (executablePath != null) {
      try {
        final Path toolHomePath = executablePath.getParent();
        toolHomeDirectory = toolHomePath == null
            ? null : toolHomePath.toRealPath().toFile();
        executablePath = executablePath.toRealPath();
        if (getLog().isDebugEnabled()) {
          getLog().debug(MessageFormat.format(
              "Executable (systempath) for [{0}]: {1}", toolName,
              executablePath));
          getLog().debug(MessageFormat.format(
              "Home directory (systempath) for [{0}]: {1}", toolName,
              toolHomeDirectory));
        }
      } catch (IOException ex) {
        if (getLog().isWarnEnabled()) {
          getLog().warn(MessageFormat.format(
              "Unable to resolve executable (systempath) for [{0}]: {1}",
              toolName, executablePath), ex);
        }
      }
    }
    return executablePath;
  }

  /**
   * Get tool executable path.
   *
   * <p>
   * Find tool executable in following order:
   * - toolhome (user specified tool home directory in configuration)
   * - toolchain (user specified JDK home directory by toolchains-plugin)
   * - javahome (JDK home directory specified by system variable JAVA_HOME)
   * - systempath (system path)
   * </p>
   *
   * @param toolName the name of the tool (without extension)
   * @param toolHomeDir the tool home directory
   * @param toolBinDirName the name of subdirectory where the tool live
   *
   * @return tool executable path from tool home directory specified in
   *         configuration or by toolchain plugin or by system variable
   *         JAVA_HOME or null
   */
  private Path getToolExecutablePath(final String toolName,
      final File toolHomeDir, final String toolBinDirName) {
    Path executablePath =
        getExecutableFromToolHome(toolName, toolHomeDir, toolBinDirName);
    if (executablePath != null) {
      return executablePath;
    }
    if (getLog().isDebugEnabled()) {
      getLog().debug(MessageFormat.format(
          "Executable (toolhome) for [{0}] not found", toolName));
    }
    executablePath = getExecutableFromToolchain(toolName);
    if (executablePath != null) {
      return executablePath;
    }
    if (getLog().isDebugEnabled()) {
      getLog().debug(MessageFormat.format(
          "Executable (toolchain) for [{0}] not found", toolName));
    }
    executablePath = getExecutableFromJavaHome(toolName);
    if (executablePath != null) {
      return executablePath;
    }
    if (getLog().isDebugEnabled()) {
      getLog().debug(MessageFormat.format(
          "Executable (javahome) for [{0}] not found", toolName));
    }
    executablePath = getExecutableFromSystemPath(toolName);
    if (executablePath != null) {
      return executablePath;
    }
    if (getLog().isDebugEnabled()) {
      getLog().debug(MessageFormat.format(
          "Executable (systempath) for [{0}] not found", toolName));
    }
    return executablePath;
  }

  /**
   * Resolve the tool path against the specified home dir.
   *
   * @param toolName the name of the tool (without extension)
   * @param toolHomeDir the home path of the tool
   * @param toolBinDirName the name of subdirectory where the tool live
   *
   * @return tool executable path or null
   */
  private Path resolveToolPath(final String toolName, final Path toolHomeDir,
      final String toolBinDirName) {
    if (toolHomeDir == null || StringUtils.isBlank(toolName)) {
      return null;
    }
    Path toolBinDir = toolHomeDir;
    if (!StringUtils.isBlank(toolBinDirName)) {
      toolBinDir = toolHomeDir.resolve(toolBinDirName);
    }
    if (!Files.exists(toolBinDir) || !Files.isDirectory(toolBinDir)) {
      return null;
    }
    return findToolExecutable(toolName, List.of(toolBinDir));
  }

  /**
   * Find tool executable under specified paths.
   *
   * @param toolName the name of the tool (without extension)
   * @param paths the list of path under which the tool will be find
   *
   * @return tool executable path or null if it not found
   */
  private Path findToolExecutable(final String toolName,
      final List<Path> paths) {
    Path executablePath = null;
    Path toolFile = null;
    final List<String> exts = getPathExt();
    for (final Path path : paths) {
      if (SystemUtils.IS_OS_WINDOWS) {
        for (final String ext : exts) {
          toolFile = path.resolve(toolName.concat(ext));
          if (Files.isExecutable(toolFile)
              && !Files.isDirectory(toolFile)) {
            executablePath = toolFile;
            break;
          }
        }
      } else {
        toolFile = path.resolve(toolName);
        if (Files.isExecutable(toolFile)
            && !Files.isDirectory(toolFile)) {
          executablePath = toolFile;
          break;
        }
      }
    }
    return executablePath;
  }

  /**
   * Get path from the system environment variable JAVA_HOME.
   *
   * @return path from the system environment variable JAVA_HOME
   */
  private File getJavaHome() {
    final String javaHome = StringUtils.stripToEmpty(System.getenv(JAVA_HOME));
    return StringUtils.isBlank(javaHome) ? null : new File(javaHome);
  }

  /**
   * Get list of the paths registered in the system environment variable PATH.
   *
   * @return list of the paths registered in the system
   *         environment variable PATH.
   */
  private List<Path> getSystemPath() {
    final String systemPath = StringUtils.stripToEmpty(System.getenv(PATH));
    if (StringUtils.isBlank(systemPath)) {
      return new ArrayList<Path>();
    }
    return Stream.of(systemPath.split(File.pathSeparator))
        .filter(s -> !StringUtils.isBlank(s))
        .map(s -> Paths.get(StringUtils.stripToEmpty(s)))
        .collect(Collectors.toList());
  }

  /**
   * Get list of the registered path extensions from
   * the system environment variable PATHEXT.
   *
   * @return list of the registered path extensions from the system
   *         environment variable PATHEXT
   */
  private List<String> getPathExt() {
    if (SystemUtils.IS_OS_WINDOWS) {
      final String systemPathExt =
          StringUtils.stripToEmpty(System.getenv(PATHEXT));
      if (!StringUtils.isBlank(systemPathExt)) {
        return Stream.of(systemPathExt.split(File.pathSeparator))
            .filter(s -> !StringUtils.isBlank(s))
            .map(s -> StringUtils.stripToEmpty(s))
            .collect(Collectors.toList());
      }
    }
    return new ArrayList<String>();
  }

  /**
   * Obtain the tool version.
   *
   * @return the tool version or null
   *
   * @throws CommandLineException if any errors occurred while processing
   *                              command line
   */
  private String obtainToolVersion(final Path executablePath)
      throws CommandLineException {
    final Commandline cmdLine = new Commandline();
    cmdLine.setExecutable(executablePath.toString());
    cmdLine.createArg().setValue(VERSION_OPTION);
    final CommandLineUtils.StringStreamConsumer out =
        new CommandLineUtils.StringStreamConsumer();
    final CommandLineUtils.StringStreamConsumer err =
        new CommandLineUtils.StringStreamConsumer();
    return execCmdLine(cmdLine, out, err) == 0
        ? StringUtils.stripToEmpty(out.getOutput())
        : null;
  }

  /**
   * Get Java version corresponding to the tool version passed in.
   *
   * @param version the tool version, not null
   *
   * @return the corresponding Java version matching the tool version
   */
  private JavaVersion getCorrespondingJavaVersion(final String version) {
    JavaVersion resolvedVersion = null;
    if (version != null) {
      final Matcher versionMatcher = Pattern.compile(VERSION_PATTERN)
          .matcher(version);
      if (versionMatcher.matches()) {
        // always present
        final String majorVersionPart = versionMatcher.group(1);
        final int majorVersion = Integer.parseInt(majorVersionPart);
        // optional part
        final String minorVersionPart = versionMatcher.group(3);
        final int minorVersion = StringUtils.isBlank(minorVersionPart)
            ? 0 : Integer.parseInt(minorVersionPart);
        if (majorVersion >= NEW_MAJOR) {
          if (majorVersion >= NEW_RECENT) {
            resolvedVersion = JavaVersion.JAVA_RECENT;
          } else {
            resolvedVersion = JavaVersion.valueOf("JAVA_" + majorVersion);
          }
        } else {
          // JAVA_1_1 - JAVA_1_9 || JAVA_0_9 (android)
          if (majorVersion == OLD_MAJOR
              && minorVersion > 0
              && minorVersion <= NEW_MAJOR
              || majorVersion == ANDROID_MAJOR
              && minorVersion == ANDROID_MINOR) {
            resolvedVersion = JavaVersion.valueOf("JAVA_" + majorVersion
                + "_" + minorVersion);
          }
        }
      }
    }
    return resolvedVersion;
  }

  /**
   * Get JDK toolchain specified in toolchains-plugin for
   * current build context.
   *
   * @return JDK toolchain
   */
  @SuppressWarnings("deprecation") // DefaultJavaToolChain
  private Toolchain getDefaultJavaToolchain() {
    final Toolchain ctxToolchain =
        getToolchainManager().getToolchainFromBuildContext(JDK, getSession());
    return ctxToolchain == null || !(ctxToolchain
        instanceof org.apache.maven.toolchain.java.DefaultJavaToolChain)
        ? null : ctxToolchain;
  }

  /**
   * Log result of the commandline execution.
   *
   * @param cmdLine the command line
   * @param exitCode the exit code
   * @param stdout the standard output
   * @param stderr the standard error
   */
  private void logCommandLineExecution(final Commandline cmdLine,
      final int exitCode, final String stdout, final String stderr) {
    if (exitCode == 0) {
      if (getLog().isDebugEnabled()) {
        if (!StringUtils.isBlank(stdout)) {
          getLog().debug(System.lineSeparator() + stdout);
        }
        if (!StringUtils.isBlank(stderr)) {
          getLog().debug(System.lineSeparator() + stderr);
        }
      }
    } else {
      if (getLog().isErrorEnabled()) {
        getLog().error(System.lineSeparator() + "Exit code: " + exitCode);
        if (!StringUtils.isBlank(stdout)) {
          getLog().error(System.lineSeparator() + stdout);
        }
        if (!StringUtils.isBlank(stderr)) {
          getLog().error(System.lineSeparator() + stderr);
        }
        getLog().error(System.lineSeparator()
            + "Command line was: "
            + CommandLineUtils.toString(cmdLine.getCommandline()));
      }
    }
  }

  /**
   * Get project base directory.
   *
   * @return project base directory (that containing the pom.xml file)
   */
  protected File getBaseDir() {
    return baseDir;
  }

  /**
   * Get project build directory.
   *
   * @return project build directory (${project.basedir}/target)
   */
  protected File getBuildDir() {
    return buildDir;
  }

  /**
   * Get project output directory.
   *
   * @return project output directory (${project.build.directory}/classes)
   */
  protected File getOutputDir() {
    return outputDir;
  }

  /**
   * Get project properties.
   *
   * @return project properties
   */
  protected Properties getProperties() {
    return properties;
  }

  /**
   * Get default charset.
   *
   * @return default charset (${project.build.sourceEncoding})
   */
  protected Charset getCharset() {
    return sourceEncoding;
  }

  /**
   * Get fileset manager.
   *
   * @return fileset manager
   */
  protected FileSetManager getFileSetManager() {
    return fileSetManager;
  }

  /**
   * Get list of all JDK toolchains available in user settings
   * independently from maven-toolchains-plugin.
   *
   * @return list of all JDK toolchains available in user settings
   */
  protected List<Toolchain> getToolchains() {
    return toolchains;
  }

  /**
   * Get JDK toolchain from build context,
   * i.e. the toolchain selected by maven-toolchains-plugin.
   *
   * @return JDK toolchain from build context
   */
  protected Toolchain getToolchain() {
    return toolchain;
  }

  /**
   * Get tool home directory.
   *
   * @return tool home directory
   */
  protected File getToolHomeDirectory() {
    return toolHomeDirectory;
  }

  /**
   * Get tool executable.
   *
   * @return tool executable
   */
  protected File getToolExecutable() {
    return toolExecutable;
  }

  /**
   * Get tool version.
   *
   * @return tool version
   */
  protected String getToolVersion() {
    return toolVersion;
  }

  /**
   * Get tool corresponding java version.
   *
   * @return tool corresponding java version
   */
  protected JavaVersion getToolJavaVersion() {
    return toolJavaVersion;
  }

  /**
   * Get toolchain manager.
   *
   * @return toolchain manager
   */
  protected ToolchainManager getToolchainManager() {
    return toolchainManager;
  }

  /**
   * Get plugin manager.
   *
   * @return plugin manager
   */
  protected BuildPluginManager getPluginManager() {
    return pluginManager;
  }

  /**
   * Get maven project.
   *
   * @return maven project
   */
  protected MavenProject getProject() {
    return project;
  }

  /**
   * Get maven session.
   *
   * @return maven session
   */
  protected MavenSession getSession() {
    return session;
  }

  /**
   * Execute command line.
   *
   * @param cmdLine command line
   *
   * @return exit code
   *
   * @throws CommandLineException if any errors occurred while processing
   *                              command line
   */
  protected int execCmdLine(final Commandline cmdLine)
      throws CommandLineException {
    return execCmdLine(cmdLine, null, null);
  }

  /**
   * Execute command line with defined standard output/error streams.
   *
   * @param cmdLine command line
   * @param out standard output, can be null
   * @param err standard error, can be null
   *
   * @return exit code
   *
   * @throws CommandLineException if any errors occurred while processing
   *                              command line
   */
  protected int execCmdLine(final Commandline cmdLine,
      final CommandLineUtils.StringStreamConsumer out,
      final CommandLineUtils.StringStreamConsumer err)
      throws CommandLineException {
    if (getLog().isDebugEnabled()) {
      getLog().debug(CommandLineUtils.toString(cmdLine.getCommandline()));
    }
    final CommandLineUtils.StringStreamConsumer stdout = out == null
        ? new CommandLineUtils.StringStreamConsumer()
        : out;
    final CommandLineUtils.StringStreamConsumer stderr = err == null
        ? new CommandLineUtils.StringStreamConsumer()
        : err;
    final int exitCode =
        CommandLineUtils.executeCommandLine(cmdLine, stdout, err);
    logCommandLineExecution(cmdLine, exitCode, stdout.getOutput(),
        stderr.getOutput());
    return exitCode;
  }

  /**
   * Init Mojo.
   *
   * @param toolName the name of the tool (without extension)
   * @param toolHomeDir the tool home directory
   * @param toolBinDirName the name of subdirectory where the tool live
   *                       relative to the tool home directory
   *
   * @throws MojoExecutionException if any errors occurred while processing
   *                                configuration parameters
   */
  protected void init(final String toolName, final File toolHomeDir,
      final String toolBinDirName) throws MojoExecutionException {
    if (getProject() == null) {
      throw new MojoExecutionException(
          "Error: The predefined variable ${project} is not defined");
    }

    if (getSession() == null) {
      throw new MojoExecutionException(
          "Error: The predefined variable ${session} is not defined");
    }

    baseDir = getProject().getBasedir();
    if (baseDir == null) {
      throw new MojoExecutionException(
          "Error: The predefined variable ${project.basedir} is not defined");
    }

    buildDir = new File(getProject().getBuild().getDirectory());
    if (buildDir == null) {
      throw new MojoExecutionException(
          "Error: The predefined variable ${project.build.directory} is not defined");
    }

    outputDir = new File(getProject().getBuild().getOutputDirectory());
    if (outputDir == null) {
      throw new MojoExecutionException(
          "Error: The predefined variable ${project.build.outputDirectory} is not defined");
    }

    properties = getProject().getProperties();
    if (properties == null) {
      throw new MojoExecutionException(
          "Error: Unable to read project properties");
    }

    fileSetManager = new FileSetManager();
    if (fileSetManager == null) {
      throw new MojoExecutionException(
          "Error: Unable to create file set manager");
    }

    // Get charset to write files
    final String encoding =
        properties.getProperty("project.build.sourceEncoding");
    try {
      sourceEncoding = Charset.forName(encoding);
    } catch (IllegalArgumentException ex) {
      if (getLog().isWarnEnabled()) {
        getLog().warn("Unable to read ${project.build.sourceEncoding}");
      }
    }
    if (getLog().isDebugEnabled()) {
      getLog().debug(MessageFormat.format(
          "Using source encoding: [{0}] to write files", sourceEncoding));
    }

    // Resolve all available jdk toolchains
    toolchains = getToolchainManager().getToolchains(getSession(), JDK, null);
    if (toolchains == null) {
      if (getLog().isDebugEnabled()) {
        getLog().debug("No toolchains found");
      }
    } else {
      toolchains.forEach(tc -> {
        if (getLog().isDebugEnabled()) {
          getLog().debug("Found toolchain: " + tc);
        }
      });
    }

    // Retrieve jdk toolchain from build context,
    // i.e. the toolchain selected by maven-toolchains-plugin
    toolchain = getDefaultJavaToolchain();
    if (toolchain == null) {
      if (getLog().isDebugEnabled()) {
        getLog().debug("Toolchain not specified");
      }
    } else {
      if (getLog().isInfoEnabled()) {
        getLog().info("Using toolchain: " + toolchain);
      }
    }

    // Resolve the tool home directory and executable file
    final Path executablePath =
        getToolExecutablePath(toolName, toolHomeDir, toolBinDirName);
    if (executablePath == null) {
      throw new MojoExecutionException(MessageFormat.format(
          "Error: Executable for [{0}] not found", toolName));
    }
    toolExecutable = executablePath.toFile();

    // Obtain the tool version
    try {
      toolVersion = obtainToolVersion(executablePath);
    } catch (CommandLineException ex) {
      throw new MojoExecutionException(MessageFormat.format(
          "Error: Unable to obtain version of [{0}]", toolName), ex);
    }
    if (toolVersion == null) {
      if (getLog().isWarnEnabled()) {
        getLog().warn(MessageFormat.format(
            "Unable to resolve version of [{0}]", toolName));
      }
    } else {
      if (getLog().isInfoEnabled()) {
        getLog().info(MessageFormat.format("Version of [{0}]: {1}", toolName,
            toolVersion));
      }
    }

    // Obtain the corresponding java version matching the tool version
    toolJavaVersion = getCorrespondingJavaVersion(toolVersion);
    if (toolJavaVersion == null) {
      if (getLog().isWarnEnabled()) {
        getLog().warn(MessageFormat.format(
            "Unable to resolve corresponding java version of [{0}]",
            toolName));
      }
    } else {
      if (getLog().isDebugEnabled()) {
        getLog().debug(MessageFormat.format(
            "Version (corresponding java version) of [{0}]: {1}", toolName,
            toolJavaVersion));
      }
    }

  }

}
