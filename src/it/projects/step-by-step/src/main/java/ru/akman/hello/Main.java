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

package ru.akman.hello;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static ru.akman.hello.Util.*;

/**
 * Main application class
 */
@Command(
  name = "hello",
  mixinStandardHelpOptions = true,
  version = "Version 1.0",
  description = "Simple JavaFX application"
)
public class Main extends Application implements Callable<Integer> {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  /**
   * Debug mode.
   */
  @Option(
    names = {"-d", "--debug"},
    description = "Log in debug mode."
  )
  private boolean isDebugEnabled = false;

  /**
   * GUI mode.
   */
  @Option(
    names = {"-g", "--gui"},
    description = "Run application in GUI mode."
  )
  private boolean isGUIEnabled = false;
  
  /**
   * Entry point.
   */
  public static void main(String... args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);    
  }

  /**
   * Command line user interface.
   *
   * @return Exit code
   */
  @Override
  public Integer call() throws Exception {
    if (isDebugEnabled) {
      setRootLoggerLevelToDebug();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("BEGIN");
    }
    if (isGUIEnabled) {
      Application.launch();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("END");
    }
    return 0;
  }

  /**
   * Graphic user interface start.
   *
   * @param stage Main stage
   */
  @Override
  public void start(final Stage stage) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("GUI start");
    }
    stage.setTitle("Hello Modular Application");
    stage.setScene(new Scene(new Group(), 800, 600));
    stage.centerOnScreen();
    stage.show();
  }

  /**
   * Graphic user interface stop.
   */
  @Override
  public void stop() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("GUI stop");
    }
  }

}
