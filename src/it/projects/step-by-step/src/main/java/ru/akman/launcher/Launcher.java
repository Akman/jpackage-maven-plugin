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

package ru.akman.launcher;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import ru.akman.commons.CommonUtils;

/**
 * Application launcher.
 */
public final class Launcher {

  /**
   * Messages resource bundle name.
   */
  private static final String MESSAGES_NAME = "messages";

  /**
   * CLI resource bundle name.
   */
  private static final String CLI_NAME = "cli";

  /**
   * Application properties name.
   */
  private static final String PROPS_NAME = "/application.properties";

  /**
   * Application properties charset.
   */
  private static final String PROPS_CHARSET = "UTF-8";

  /**
   * Application propertie for application name.
   */
  private static final String PROP_APP_NAME = "application.launcher.name";

  /**
   * Application propertie for application version.
   */
  private static final String PROP_APP_VERSION = "application.version";

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

  /**
   * Message strings from resource bundle.
   */
  private static final ResourceBundle MESSAGES = ResourceBundle.getBundle(
      MESSAGES_NAME, Locale.getDefault());

  /**
   * Application properties from properties file.
   */
  private static final Properties PROPERTIES = CommonUtils.loadResource(
      PROPS_NAME, Charset.forName(PROPS_CHARSET));

  /**
   * Application command.
   */
  private static final LauncherCommand COMMAND = new LauncherCommand();

  /**
   * Get string from the application resources.
   *
   * @param key resource string key
   *
   * @return resource string value
   */
  public static String getString(final String key) {
    return MESSAGES.getString(key);
  }

  /**
   * Get string from the application properties.
   *
   * @param key property string key
   *
   * @return property string value
   */
  public static String getProperty(final String key) {
    return PROPERTIES.getProperty(key);
  }

  /**
   * Get application properties.
   *
   * @return application properties
   */
  public static Properties getProperties() {
    final Properties properties = new Properties();
    properties.putAll(PROPERTIES);
    return properties;
  }

  /**
   * Get application command.
   *
   * @return application command
   */
  public static LauncherCommand getCommand() {
    return COMMAND;
  }

  /**
   * Private constructor.
   * All methods are static.
   */
  private Launcher() {
  }

  /**
   * Main entry point of application.
   *
   * @param args system CLI arguments
   */
  public static void main(final String[] args) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(getProperty(PROP_APP_NAME) + " - "
          + getProperty(PROP_APP_VERSION));
    }
    CommonUtils.setupSystemStreams();
    final CommandLine cmdLine = new CommandLine(COMMAND);
    cmdLine
        .setResourceBundle(ResourceBundle.getBundle(
            CLI_NAME, Locale.getDefault()))
        .getCommandSpec()
        .mixinStandardHelpOptions(true)
        .name(getProperty(PROP_APP_NAME))
        .version(new String[] {
          "%n@|bold " + getProperty(PROP_APP_NAME) + "|@ @|bold,yellow v"
              + getProperty(PROP_APP_VERSION) + "|@"
        });
    System.exit(cmdLine.execute(args));
  }

}
