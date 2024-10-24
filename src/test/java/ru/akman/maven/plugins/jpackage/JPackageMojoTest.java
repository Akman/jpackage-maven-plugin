/*
  Copyright (C) 2020 - 2024 Alexander Kapitman

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.PlexusContainer;
import org.junit.Rule;
import org.junit.Test;
import ru.akman.maven.plugins.TestUtils;

/**
 * JPackageMojo Test Class.
 */
public class JPackageMojoTest {

  /**
   * Relative path to the base directory of tested project.
   */
  private static final String PROJECT_DIR = "target/test-classes/project/";

  /**
   * Executed goal.
   */
  private static final String MOJO_EXECUTION = "jpackage";

  /**
   * Plexus DI container.
   */
  private PlexusContainer container;

  /**
   * Toolchain manager.
   */
  private ToolchainManager toolchainManager;

  /**
   * Maven project.
   */
  private MavenProject project;

  /**
   * Maven session.
   */
  private MavenSession session;

  /**
   * Mojo execution.
   */
  private MojoExecution execution;

  /**
   * JPackage Mojo.
   */
  private JPackageMojo mojo;

  /**
   * AbstractMojoTestCase wrapper.
   * All protected methods of the TestCase are exhibited as public in the rule.
   */
  @Rule
  public MojoRule rule = new MojoRule() {

    @Override
    protected void before() throws Throwable {
      // Plexus container
      container = getContainer();
      assertNotNull("Has access to the plexus container", container);
      // Toolchain manager
      toolchainManager = (ToolchainManager) container.lookup(
          ToolchainManager.class.getName());
      assertNotNull("Can get the toolchain manager", toolchainManager);
      // Project directory
      final File pom = new File(PROJECT_DIR);
      assertNotNull("Project directory path is valid", pom);
      assertTrue("Project directory exists", pom.exists());
      // Maven project
      project = readMavenProject(pom);
      assertNotNull("Can read the project", project);
      // Maven session
      session = newMavenSession(project);
      assertNotNull("Can create new session", session);
      // Mojo execution
      execution = newMojoExecution(MOJO_EXECUTION);
      assertNotNull("Can create new execution", execution);
      // Mojo
      mojo = (JPackageMojo) lookupConfiguredMojo(session, execution);
      assertNotNull("Can lookup configured mojo", mojo);
    }

    @Override
    protected void after() {
      // skip
    }

  };

  /**
   * Parameter 'toolhome' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasToolHome() throws Exception {
    final File toolhome =
        (File) rule.getVariableValueFromObject(mojo, "toolhome");
    assertEquals("toolhome",
        TestUtils.getCanonicalPath(toolhome),
        TestUtils.getCanonicalPath(new File(project.getBasedir(),
            "path/to/jpackage/home"))
    );
  }

  /**
   * Parameter 'dest' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDest() throws Exception {
    final File dest =
        (File) rule.getVariableValueFromObject(mojo, "dest");
    assertEquals("dest",
        TestUtils.getCanonicalPath(dest),
        TestUtils.getCanonicalPath(new File(project.getBuild().getDirectory(),
            "jpackage"))
    );
  }

  /**
   * Parameter 'temp' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasTemp() throws Exception {
    final File temp =
        (File) rule.getVariableValueFromObject(mojo, "temp");
    assertEquals("temp",
        TestUtils.getCanonicalPath(temp),
        TestUtils.getCanonicalPath(new File(project.getBuild().getDirectory(),
            "jpackage/temp"))
    );
  }

  /**
   * Parameter 'type' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasType() throws Exception {
    final PackageType type =
        (PackageType) rule.getVariableValueFromObject(mojo, "type");
    assertEquals("type",
        type,
        PackageType.IMAGE
    );
  }

  /**
   * Parameter 'verbose' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasVerbose() throws Exception {
    final boolean verbose =
        (boolean) rule.getVariableValueFromObject(mojo, "verbose");
    assertTrue("verbose",
        verbose);
  }

  /**
   * Parameter 'appversion' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasAppVersion() throws Exception {
    final String appversion =
        (String) rule.getVariableValueFromObject(mojo, "appversion");
    assertEquals("appversion",
        appversion,
        "1.0"
    );
  }

  /**
   * Parameter 'copyright' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasCopyright() throws Exception {
    final String copyright =
        (String) rule.getVariableValueFromObject(mojo, "copyright");
    assertEquals("copyright",
        copyright,
        "Copyright (C) 2020 Alexander Kapitman"
    );
  }

  /**
   * Parameter 'description' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDescription() throws Exception {
    final String description =
        (String) rule.getVariableValueFromObject(mojo, "description");
    assertEquals("description",
        description,
        "JPackage Maven Plugin Unit Tests"
    );
  }

  /**
   * Parameter 'name' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasName() throws Exception {
    final String name =
        (String) rule.getVariableValueFromObject(mojo, "name");
    assertEquals("name",
        name,
        "appimage"
    );
  }

  /**
   * Parameter 'vendor' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasVendor() throws Exception {
    final String vendor =
        (String) rule.getVariableValueFromObject(mojo, "vendor");
    assertEquals("vendor",
        vendor,
        "Akman"
    );
  }

  /**
   * Parameter 'icon' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasIcon() throws Exception {
    final File icon =
        (File) rule.getVariableValueFromObject(mojo, "icon");
    assertEquals("icon",
        TestUtils.getCanonicalPath(icon),
        TestUtils.getCanonicalPath(new File(project.getBasedir(),
            "config/jpackage/icon.ico"))
    );
  }

  /**
   * Parameter 'input' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasInput() throws Exception {
    final File input =
        (File) rule.getVariableValueFromObject(mojo, "input");
    assertEquals("input",
        TestUtils.getCanonicalPath(input),
        TestUtils.getCanonicalPath(new File(project.getBuild().getDirectory(),
            "jlink/libs"))
    );
  }

  /**
   * Parameter 'runtimeimage' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasRuntimeImage() throws Exception {
    final File runtimeimage =
        (File) rule.getVariableValueFromObject(mojo, "runtimeimage");
    assertEquals("runtimeimage",
        TestUtils.getCanonicalPath(runtimeimage),
        TestUtils.getCanonicalPath(new File(project.getBuild().getDirectory(),
            "jlink/image"))
    );
  }

  /**
   * Parameter 'modulepath' exists.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasModulePath() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    assertNotNull("modulepath",
        modulepath);
  }

  /**
   * Parameter 'modulepath/pathelements' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasPathElements() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<File> pathelements = modulepath.getPathElements();
    assertEquals("modulepath/pathelements",
        TestUtils.buildPathFromFiles(pathelements),
        TestUtils.buildPathFromNames(PROJECT_DIR, Arrays.asList(
            "mod.jar",
            "mod.jmod",
            "mods/exploded/mod"
        ))
    );
  }

  /**
   * Parameter 'modulepath/filesets' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasFilesets() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> filesets = modulepath.getFileSets();
    assertEquals("modulepath/filesets",
        filesets.size(), 1);
  }

  /**
   * Parameter 'modulepath/filesets/fileset/isfollowsymlinks' exists
   * and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasFilesetFollowSymlinks() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> filesets = modulepath.getFileSets();
    final FileSet fileset = filesets.get(0);
    assertFalse("modulepath/filesets/fileset/isfollowsymlinks",
        fileset.isFollowSymlinks());
  }

  /**
   * Parameter 'modulepath/filesets/fileset/includes' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasFilesetIncludes() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> filesets = modulepath.getFileSets();
    final FileSet fileset = filesets.get(0);
    assertEquals("modulepath/filesets/fileset/includes",
        TestUtils.buildStringFromNames(fileset.getIncludes()),
        TestUtils.buildStringFromNames(Arrays.asList("**/*"))
    );
  }

  /**
   * Parameter 'modulepath/filesets/fileset/excludes' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasFilesetExcludes() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> filesets = modulepath.getFileSets();
    final FileSet fileset = filesets.get(0);
    assertEquals("modulepath/filesets/fileset/excludes",
        TestUtils.buildStringFromNames(fileset.getExcludes()),
        TestUtils.buildStringFromNames(Arrays.asList(
            "**/*Empty.jar",
            "jlink.opts",
            "jlink-opts"
        ))
    );
  }

  /**
   * Parameter 'modulepath/filesets/fileset/directory' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasFilesetDirectory() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> filesets = modulepath.getFileSets();
    final FileSet fileset = filesets.get(0);
    try {
      PluginUtils.normalizeFileSetBaseDir(project.getBasedir(), fileset);
    } catch (IOException ex) {
      fail("Error: Unable to resolve fileset base directory: ["
          + project.getBasedir() + "]."
          + System.lineSeparator()
          + ex.toString()
      );
    }
    assertEquals("modulepath/filesets/fileset/directory",
        TestUtils.getCanonicalPath(new File(fileset.getDirectory())),
        TestUtils.getCanonicalPath(new File(project.getBuild().getDirectory()))
    );
  }

  /**
   * Parameter 'modulepath/dirsets' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDirsets() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> dirsets = modulepath.getDirSets();
    assertEquals("modulepath/dirsets",
        dirsets.size(), 1);
  }

  /**
   * Parameter 'modulepath/dirsets/dirset/isfollowsymlinks' exists
   * and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDirsetFollowSymlinks() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> dirsets = modulepath.getDirSets();
    final FileSet dirset = dirsets.get(0);
    assertTrue("modulepath/dirsets/dirset/isfollowsymlinks",
        dirset.isFollowSymlinks());
  }

  /**
   * Parameter 'modulepath/dirsets/dirset/includes' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDirsetIncludes() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> dirsets = modulepath.getDirSets();
    final FileSet dirset = dirsets.get(0);
    assertEquals("modulepath/dirsets/dirset/includes",
        TestUtils.buildStringFromNames(dirset.getIncludes()),
        TestUtils.buildStringFromNames(Arrays.asList("**/*"))
    );
  }

  /**
   * Parameter 'modulepath/dirsets/dirset/excludes' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDirsetExcludes() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> dirsets = modulepath.getDirSets();
    final FileSet dirset = dirsets.get(0);
    assertEquals("modulepath/dirsets/dirset/excludes",
        TestUtils.buildStringFromNames(dirset.getExcludes()),
        TestUtils.buildStringFromNames(Arrays.asList("**/*Test"))
    );
  }

  /**
   * Parameter 'modulepath/dirsets/dirset/directory' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDirsetDirectory() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<FileSet> dirsets = modulepath.getDirSets();
    final FileSet dirset = dirsets.get(0);
    try {
      PluginUtils.normalizeFileSetBaseDir(project.getBasedir(), dirset);
    } catch (IOException ex) {
      fail("Error: Unable to resolve fileset base directory: ["
          + project.getBasedir() + "]."
          + System.lineSeparator()
          + ex.toString()
      );
    }
    assertEquals("modulepath/dirsets/dirset/directory",
        TestUtils.getCanonicalPath(new File(dirset.getDirectory())),
        TestUtils.getCanonicalPath(new File(project.getBuild().getDirectory()))
    );
  }

  /**
   * Parameter 'modulepath/dependencysets' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDependencysets() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<DependencySet> dependencysets = modulepath.getDependencySets();
    assertEquals("modulepath/dependencysets",
        dependencysets.size(), 1);
  }

  /**
   * Parameter 'modulepath/dependencysets/dependencyset/outputincluded' exists
   * and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDependencysetOutputIncluded() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<DependencySet> dependencysets = modulepath.getDependencySets();
    final DependencySet depset = dependencysets.get(0);
    assertFalse("modulepath/dependencysets/dependencyset/outputincluded",
        depset.isOutputIncluded());
  }

  /**
   * Parameter 'modulepath/dependencysets/dependencyset/automaticexcluded'
   * exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDependencysetAutomaticExcluded() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<DependencySet> dependencysets = modulepath.getDependencySets();
    final DependencySet depset = dependencysets.get(0);
    assertFalse("modulepath/dependencysets/dependencyset/automaticexcluded",
        depset.isAutomaticExcluded());
  }

  /**
   * Parameter 'modulepath/dependencysets/dependencyset/includes' exists and
   * has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDependencysetIncludes() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<DependencySet> dependencysets = modulepath.getDependencySets();
    final DependencySet depset = dependencysets.get(0);
    assertEquals("modulepath/dependencysets/dependencyset/includes",
        TestUtils.buildStringFromNames(depset.getIncludes()),
        TestUtils.buildStringFromNames(Arrays.asList(
            "glob:**/*.jar",
            "regex:foo-(bar|baz)-.*?\\.jar"
        ))
    );
  }

  /**
   * Parameter 'modulepath/dependencysets/dependencyset/excludes' exists and
   * has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDependencysetExcludes() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<DependencySet> dependencysets = modulepath.getDependencySets();
    final DependencySet depset = dependencysets.get(0);
    assertEquals("modulepath/dependencysets/dependencyset/excludes",
        TestUtils.buildStringFromNames(depset.getExcludes()),
        TestUtils.buildStringFromNames(Arrays.asList("glob:**/javafx.*Empty"))
    );
  }

  /**
   * Parameter 'modulepath/dependencysets/dependencyset/includenames' exists
   * and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDependencysetIncludeNames() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<DependencySet> dependencysets = modulepath.getDependencySets();
    final DependencySet depset = dependencysets.get(0);
    assertEquals("modulepath/dependencysets/dependencyset/includenames",
        TestUtils.buildStringFromNames(depset.getIncludeNames()),
        TestUtils.buildStringFromNames(Arrays.asList(".*"))
    );
  }

  /**
   * Parameter 'modulepath/dependencysets/dependencyset/excludenames' exists
   * and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasDependencysetExcludeNames() throws Exception {
    final ModulePath modulepath =
        (ModulePath) rule.getVariableValueFromObject(mojo, "modulepath");
    final List<DependencySet> dependencysets = modulepath.getDependencySets();
    final DependencySet depset = dependencysets.get(0);
    assertEquals("modulepath/dependencysets/dependencyset/excludenames",
        TestUtils.buildStringFromNames(depset.getExcludeNames()),
        TestUtils.buildStringFromNames(Arrays.asList("javafx\\..+Empty"))
    );
  }

  /**
   * Parameter 'addmodules' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddModules() throws Exception {
    final List<String> addmodules =
        (List<String>) rule.getVariableValueFromObject(mojo, "addmodules");
    assertEquals("addmodules",
        TestUtils.buildStringFromNames(addmodules),
        TestUtils.buildStringFromNames(Arrays.asList(
            "java.base", "org.example.rootmodule"))
    );
  }

  /**
   * Parameter 'bindservices' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasBindServices() throws Exception {
    final boolean bindservices =
        (boolean) rule.getVariableValueFromObject(mojo, "bindservices");
    assertTrue("bindservices",
        bindservices);
  }

  /**
   * Parameter 'module' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasModule() throws Exception {
    final String module =
        (String) rule.getVariableValueFromObject(mojo, "module");
    assertEquals("module",
        module,
        "mainModuleName/mainClassName"
    );
  }

  /**
   * Parameter 'mainjar' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasMainJar() throws Exception {
    final String mainjar =
        (String) rule.getVariableValueFromObject(mojo, "mainjar");
    assertEquals("mainjar",
        mainjar,
        "mainJar.jar"
    );
  }

  /**
   * Parameter 'mainclass' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasMainClass() throws Exception {
    final String mainclass =
        (String) rule.getVariableValueFromObject(mojo, "mainclass");
    assertEquals("mainclass",
        mainclass,
        "mainClassName"
    );
  }

  /**
   * Parameter 'arguments' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasArguments() throws Exception {
    final String arguments =
        (String) rule.getVariableValueFromObject(mojo, "arguments");
    assertEquals("arguments",
        arguments,
        "--gui"
    );
  }

  /**
   * Parameter 'javaoptions' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasJavaOptions() throws Exception {
    final String javaoptions =
        (String) rule.getVariableValueFromObject(mojo, "javaoptions");
    assertEquals("javaoptions",
        javaoptions,
        "-Dfile.encoding=UTF-8 -Xms256m -Xmx512m"
    );
  }

  /**
   * Parameter 'addlaunchers' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchers() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertNotNull("addlaunchers",
        addlaunchers);
  }

  /**
   * Parameter 'addlaunchers/addlauncher' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncher() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertNotNull("addlaunchers/addlauncher",
        addlaunchers.get(0));
  }

  /**
   * Parameter 'addlaunchers/addlauncher/name' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherName() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/name",
        addlaunchers.get(0).getName(),
        "launcher1"
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/file' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherFile() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/file",
        TestUtils.getCanonicalPath(addlaunchers.get(0).getFile()),
        TestUtils.getCanonicalPath(new File(project.getBasedir(),
            "config/jpackage/launcher1.properties"))
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/module' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherModule() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/module",
        addlaunchers.get(0).getModule(),
        "mainModule1Name/mainClass1Name"
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/mainjar' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherMainJar() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/mainjar",
        addlaunchers.get(0).getMainJar(),
        "mainJar1.jar"
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/mainclass' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherMainClass() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/mainclass",
        addlaunchers.get(0).getMainClass(),
        "mainClass1Name"
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/arguments' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherArguments() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/arguments",
        addlaunchers.get(0).getArguments(),
        "--arg11 --arg12"
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/javaoptions' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherJavaOptions() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/javaoptions",
        addlaunchers.get(0).getJavaOptions(),
        "-Xms128m -Xmx1024m"
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/appversion' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherAppVersion() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/appversion",
        addlaunchers.get(0).getAppVersion(),
        "1.0.1"
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/icon' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherIcon() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertEquals("addlaunchers/addlauncher/icon",
        TestUtils.getCanonicalPath(addlaunchers.get(0).getIcon()),
        TestUtils.getCanonicalPath(new File(project.getBasedir(),
            "config/jpackage/launcher1.ico"))
    );
  }

  /**
   * Parameter 'addlaunchers/addlauncher/winconsole' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasAddLaunchersAddLauncherWinConsole() throws Exception {
    final List<Launcher> addlaunchers =
        (List<Launcher>) rule.getVariableValueFromObject(mojo, "addlaunchers");
    assertTrue("addlaunchers/addlauncher/winconsole",
        addlaunchers.get(0).isWinConsole());
  }

  /**
   * Parameter 'winconsole' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasWinConsole() throws Exception {
    final boolean winconsole =
        (boolean) rule.getVariableValueFromObject(mojo, "winconsole");
    assertFalse("winconsole",
        winconsole);
  }

  /**
   * Parameter 'appimage' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasAppImage() throws Exception {
    final File appimage =
        (File) rule.getVariableValueFromObject(mojo, "appimage");
    assertEquals("appimage",
        TestUtils.getCanonicalPath(appimage),
        TestUtils.getCanonicalPath(new File(project.getBuild().getDirectory(),
            "jpackage/appimage"))
    );
  }

  /**
   * Parameter 'fileassociations' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  @SuppressWarnings("unchecked") // unchecked cast
  public void testMojoHasFileAssociations() throws Exception {
    final List<File> fileassociations =
        (List<File>) rule.getVariableValueFromObject(mojo, "fileassociations");
    assertEquals("fileassociations",
        TestUtils.buildPathFromFiles(fileassociations),
        TestUtils.buildPathFromNames(PROJECT_DIR, Arrays.asList(
            "config/jpackage/associations1.properties",
            "config/jpackage/associations2.properties"
        ))
    );
  }

  /**
   * Parameter 'installdir' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasInstallDir() throws Exception {
    final String installdir =
        (String) rule.getVariableValueFromObject(mojo, "installdir");
    assertEquals("installdir",
        installdir,
        "Akman/My Application"
    );
  }

  /**
   * Parameter 'licensefile' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLicenseFile() throws Exception {
    final File licensefile =
        (File) rule.getVariableValueFromObject(mojo, "licensefile");
    assertEquals("licensefile",
        TestUtils.getCanonicalPath(licensefile),
        TestUtils.getCanonicalPath(new File(project.getBasedir(),
            "config/jpackage/LICENSE"))
    );
  }

  /**
   * Parameter 'resourcedir' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasResourceDir() throws Exception {
    final File resourcedir =
        (File) rule.getVariableValueFromObject(mojo, "resourcedir");
    assertEquals("resourcedir",
        TestUtils.getCanonicalPath(resourcedir),
        TestUtils.getCanonicalPath(new File(project.getBasedir(),
            "config/jpackage/resources"))
    );
  }

  /**
   * Parameter 'windirchooser' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasWinDirChooser() throws Exception {
    final boolean windirchooser =
        (boolean) rule.getVariableValueFromObject(mojo, "windirchooser");
    assertTrue("windirchooser",
        windirchooser);
  }

  /**
   * Parameter 'winmenu' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasWinMenu() throws Exception {
    final boolean winmenu =
        (boolean) rule.getVariableValueFromObject(mojo, "winmenu");
    assertTrue("winmenu",
        winmenu);
  }

  /**
   * Parameter 'winmenugroup' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasWinMenuGroup() throws Exception {
    final String winmenugroup =
        (String) rule.getVariableValueFromObject(mojo, "winmenugroup");
    assertEquals("winmenugroup",
        winmenugroup,
        "JPackage"
    );
  }

  /**
   * Parameter 'winperuserinstall' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasWinPerUserInstall() throws Exception {
    final boolean winperuserinstall =
        (boolean) rule.getVariableValueFromObject(mojo, "winperuserinstall");
    assertTrue("winperuserinstall",
        winperuserinstall);
  }

  /**
   * Parameter 'winshortcut' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasWinShortcut() throws Exception {
    final boolean winshortcut =
        (boolean) rule.getVariableValueFromObject(mojo, "winshortcut");
    assertTrue("winshortcut",
        winshortcut);
  }

  /**
   * Parameter 'winupgradeuuid' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasWinUpgradeUuid() throws Exception {
    final String winupgradeuuid =
        (String) rule.getVariableValueFromObject(mojo, "winupgradeuuid");
    assertEquals("winupgradeuuid",
        winupgradeuuid,
        "8CF81762-0B19-46A6-875E-1F839A1700D0"
    );
  }

  /**
   * Parameter 'macpackageidentifier' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasMacPackageIdentifier() throws Exception {
    final String macpackageidentifier =
        (String) rule.getVariableValueFromObject(mojo, "macpackageidentifier");
    assertEquals("macpackageidentifier",
        macpackageidentifier,
        "macPackageIdentifier"
    );
  }

  /**
   * Parameter 'macpackagename' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasMacPackageName() throws Exception {
    final String macpackagename =
        (String) rule.getVariableValueFromObject(mojo, "macpackagename");
    assertEquals("macpackagename",
        macpackagename,
        "macPackageName"
    );
  }

  /**
   * Parameter 'macpackagesigningprefix' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasMacPackageSigningPrefix() throws Exception {
    final String macpackagesigningprefix =
        (String) rule.getVariableValueFromObject(mojo, "macpackagesigningprefix");
    assertEquals("macpackagesigningprefix",
        macpackagesigningprefix,
        "macPackageSigningPrefix"
    );
  }

  /**
   * Parameter 'macsign' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasMacSign() throws Exception {
    final boolean macsign =
        (boolean) rule.getVariableValueFromObject(mojo, "macsign");
    assertFalse("macsign",
        macsign);
  }

  /**
   * Parameter 'macsigningkeychain' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasMacSigningKeyChain() throws Exception {
    final File macsigningkeychain =
        (File) rule.getVariableValueFromObject(mojo, "macsigningkeychain");
    assertEquals("macsigningkeychain",
        TestUtils.getCanonicalPath(macsigningkeychain),
        TestUtils.getCanonicalPath(new File(project.getBasedir(),
            "macSigningKeyChain"))
    );
  }

  /**
   * Parameter 'macsigningkeyusername' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasMacSigningKeyUserName() throws Exception {
    final String macsigningkeyusername =
        (String) rule.getVariableValueFromObject(mojo, "macsigningkeyusername");
    assertEquals("macsigningkeyusername",
        macsigningkeyusername,
        "macSigningKeyUserName"
    );
  }

  /**
   * Parameter 'linuxpackagename' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLinuxPackageName() throws Exception {
    final String linuxpackagename =
        (String) rule.getVariableValueFromObject(mojo, "linuxpackagename");
    assertEquals("linuxpackagename",
        linuxpackagename,
        "linuxPackageName"
    );
  }

  /**
   * Parameter 'linuxdebmaintainer' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLinuxDebMaintainer() throws Exception {
    final String linuxdebmaintainer =
        (String) rule.getVariableValueFromObject(mojo, "linuxdebmaintainer");
    assertEquals("linuxdebmaintainer",
        linuxdebmaintainer,
        "linuxDebMaintainer"
    );
  }

  /**
   * Parameter 'linuxmenugroup' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLinuxMenuGroup() throws Exception {
    final String linuxmenugroup =
        (String) rule.getVariableValueFromObject(mojo, "linuxmenugroup");
    assertEquals("linuxmenugroup",
        linuxmenugroup,
        "linuxMenuGroup"
    );
  }

  /**
   * Parameter 'linuxpackagedeps' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLinuxPackageDeps() throws Exception {
    final boolean linuxpackagedeps =
        (boolean) rule.getVariableValueFromObject(mojo, "linuxpackagedeps");
    assertFalse("linuxpackagedeps",
        linuxpackagedeps);
  }

  /**
   * Parameter 'linuxrpmlicensetype' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLinuxRpmLicenseType() throws Exception {
    final String linuxrpmlicensetype =
        (String) rule.getVariableValueFromObject(mojo, "linuxrpmlicensetype");
    assertEquals("linuxrpmlicensetype",
        linuxrpmlicensetype,
        "MIT"
    );
  }

  /**
   * Parameter 'linuxapprelease' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLinuxAppRelease() throws Exception {
    final String linuxapprelease =
        (String) rule.getVariableValueFromObject(mojo, "linuxapprelease");
    assertEquals("linuxapprelease",
        linuxapprelease,
        "linuxAppRelease"
    );
  }

  /**
   * Parameter 'linuxappcategory' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLinuxAppCategory() throws Exception {
    final String linuxappcategory =
        (String) rule.getVariableValueFromObject(mojo, "linuxappcategory");
    assertEquals("linuxappcategory",
        linuxappcategory,
        "linuxAppCategory"
    );
  }

  /**
   * Parameter 'linuxshortcut' exists and has a value.
   *
   * @throws Exception if any errors occurred
   */
  @Test
  public void testMojoHasLinuxShortcut() throws Exception {
    final boolean linuxshortcut =
        (boolean) rule.getVariableValueFromObject(mojo, "linuxshortcut");
    assertTrue("linuxshortcut",
        linuxshortcut);
  }

}
