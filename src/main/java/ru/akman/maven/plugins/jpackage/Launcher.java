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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 * Additional launcher.
 */
public class Launcher {

  /**
   * Launcher command (script) name.
   */
  private String name;

  /**
   * Launcher properties file.
   */
  private File file;

  /**
   * Main module name.
   */
  private String module;

  /**
   * Main jar filename.
   */
  private String mainjar;

  /**
   * Main class name.
   */
  private String mainclass;
  
  /**
   * Command arguments.
   */
  private String arguments;

  /**
   * Java runtime options.
   */
  private String javaoptions;

  /**
   * Launcher command (script) version.
   */
  private String appversion;

  /**
   * Launcher command (script) icon.
   */
  private File icon;

  /**
   * Enable creating a console launcher (which requires console interactions).
   */
  private String winconsole;

  /**
   * Get command (script) name.
   *
   * @return the command (script) name without extension
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Set command (script) name.
   *
   * @param name the name of command (script)
   */
  public void setName(final String name) {
    this.name = name;
  }
  
  /**
   * Get main module name.
   *
   * @return the main module name
   */
  public String getModule() {
    return this.module;
  }
  
  /**
   * Set main module name.
   *
   * @param module the name of main module
   */
  public void setModule(final String module) {
    this.module = module;
  }

  /**
   * Get main jar filename.
   *
   * @return the main jar filename
   */
  public String getMainJar() {
    return this.mainjar;
  }
  
  /**
   * Set main jar filename.
   *
   * @param mainjar the main jar filename
   */
  public void setMainJar(final String mainjar) {
    this.mainjar = mainjar;
  }
  
  /**
   * Get main class name.
   *
   * @return the main class name
   */
  public String getMainClass() {
    return this.mainclass;
  }
  
  /**
   * Set main class name.
   *
   * @param mainclass the name of main class
   */
  public void setMainClass(final String mainclass) {
    this.mainclass = mainclass;
  }

  /**
   * Get command (script) arguments.
   *
   * @return the command (script) arguments
   */
  public String getArguments() {
    return this.arguments;
  }
  
  /**
   * Set command (script) arguments.
   *
   * @param arguments the command (script) arguments
   */
  public void setArguments(final String arguments) {
    this.arguments = arguments;
  }

  /**
   * Get Java runtime options.
   *
   * @return the Java runtime options
   */
  public String getJavaOptions() {
    return this.javaoptions;
  }
  
  /**
   * Set Java runtime options.
   *
   * @param javaoptions Java runtime options
   */
  public void setJavaOptions(final String javaoptions) {
    this.javaoptions = javaoptions;
  }

  /**
   * Get launcher command (script) version.
   *
   * @return the launcher command (script) version
   */
  public String getAppVersion() {
    return this.appversion;
  }
  
  /**
   * Set launcher command (script) version.
   *
   * @param appversion Launcher command (script) version.
   */
  public void setAppVersion(final String appversion) {
    this.appversion = appversion;
  }

  /**
   * Get launcher command (script) icon.
   *
   * @return the launcher command (script) icon
   */
  public File getIcon() {
    return this.icon;
  }
  
  /**
   * Set launcher command (script) icon.
   *
   * @param icon Launcher command (script) icon.
   */
  public void setIcon(final File icon) {
    this.icon = icon;
  }

  /**
   * Is enabled creating a console launcher.
   *
   * @return true if creating a console launcher is enabled
   */
  public boolean isWinConsole() {
    return String.valueOf(true).equals(this.winconsole);
  }
  
  /**
   * Enable or disable creating a console launcher.
   *
   * @param winconsole is creating a console launcher enabled
   */
  public void setWinConsole(final String winconsole) {
    this.winconsole = winconsole;
  }

  /**
   * Get launcher properties file.
   *
   * @return the launcher properties file
   */
  public File getFile() {
    return this.file;
  }
  
  /**
   * Set launcher properties file.
   *
   * @param file Launcher properties file.
   */
  public void setFile(final File file) {
    this.file = file;
  }

  /**
   * Get the launcher properties.
   *
   * @param charset The charset using to read properties file.
   *
   * @return the launcher properties
   *
   * @throws IOException if IO errors occured
   */
  public Properties getProperties(final Charset charset) throws IOException {
    final Properties props = new Properties();
    if (file != null) {
      try (BufferedReader br =
          Files.newBufferedReader(file.toPath(), charset)) {
        props.load(br);
      }
    }
    if (!StringUtils.isBlank(module)) {
      props.setProperty("module", module);
    }
    if (!StringUtils.isBlank(mainjar)) {
      props.setProperty("main-jar", mainjar);
    }
    if (!StringUtils.isBlank(mainclass)) {
      props.setProperty("main-class", mainclass);
    }
    if (!StringUtils.isBlank(arguments)) {
      props.setProperty("arguments", arguments);
    }
    if (!StringUtils.isBlank(javaoptions)) {
      props.setProperty("java-options", javaoptions);
    }
    if (!StringUtils.isBlank(appversion)) {
      props.setProperty("app-version", appversion);
    }
    if (icon != null) {
      props.setProperty("icon", icon.toString());
    }
    if (winconsole != null) {
      props.setProperty("win-console", String.valueOf(isWinConsole()));
    }
    return props;
  }

}
