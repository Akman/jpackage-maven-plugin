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

import java.awt.SplashScreen;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.akman.commons.CommonUtils;

/**
 * Application launcher command.
 */
@Command
class LauncherCommand implements Callable<Integer> {

  /**
   * Logger level for debug mode.
   */
  private static final String LOG_LEVEL_DEBUG = "DEBUG";
  
  /**
   * Application propertie for greeting.
   */
  private static final String PROP_APP_GREETING = "app.greeting";

  /**
   * Application propertie for CLI mode.
   */
  private static final String PROP_APP_MODE_CLI = "app.mode.cli";

  /**
   * Application propertie for GUI mode.
   */
  private static final String PROP_APP_MODE_GUI = "app.mode.gui";

  /**
   * Application propertie for parting.
   */
  private static final String PROP_APP_PARTING = "app.parting";

  /**
   * Default logger.
   */
  private static final Logger LOG =
      LoggerFactory.getLogger(LauncherCommand.class);
  
  /**
   * Debug mode.
   */
  @Option(
      names = {"-d", "--debug"},
      descriptionKey = "debug"
  )
  private boolean debugEnabled;

  /**
   * GUI mode.
   */
  @Option(
      names = {"-g", "--gui"},
      descriptionKey = "gui"
  )
  private boolean guiEnabled;

  /**
   * Is DEBUG mode enabled.
   *
   * @return true if DEBUG mode is enabled
   */
  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  /**
   * Set DEBUG mode enabled or disabled.
   *
   * @param isEnabled is DEBUG mode enabled
   */
  public void setDebugEnabled(final boolean isEnabled) {
    debugEnabled = isEnabled;
  }

  /**
   * Is GUI mode enabled.
   *
   * @return true if GUI mode is enabled
   */
  public boolean isGuiEnabled() {
    return guiEnabled;
  }

  /**
   * Set GUI mode enabled or disabled.
   *
   * @param isEnabled is GUI mode enabled
   */
  public void setGuiEnabled(final boolean isEnabled) {
    guiEnabled = isEnabled;
  }

  /**
   * Command line user interface.
   *
   * @return Exit code
   */
  @Override
  public Integer call() throws Exception {
    final SplashScreen splash = SplashScreen.getSplashScreen();
    if (splash != null) {
      splash.close();
    }
    if (isDebugEnabled()) {
      CommonUtils.setLoggerLevel(LOG_LEVEL_DEBUG);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(Launcher.getString(PROP_APP_GREETING));
    }
    CommonUtils.dumpProperties(Launcher.getProperties());
    if (isGuiEnabled()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(Launcher.getString(PROP_APP_MODE_GUI));
      }
      ru.akman.gui.LauncherHelper.run();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug(Launcher.getString(PROP_APP_MODE_CLI));
      }
      ru.akman.cli.LauncherHelper.run();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(Launcher.getString(PROP_APP_PARTING));
    }
    return 0;
  }

}
