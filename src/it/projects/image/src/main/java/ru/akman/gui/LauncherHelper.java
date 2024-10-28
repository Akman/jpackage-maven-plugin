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

package ru.akman.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.akman.launcher.Launcher;

/**
 * Main GUI class.
 */
public final class LauncherHelper extends Application {

  /**
   * Default stage width.
   */
  private static final int WIDTH = 640;

  /**
   * Default stage height.
   */
  private static final int HEIGHT = 480;

  /**
   * Default stage opacity.
   */
  private static final double OPACITY = 0.85;

  /**
   * Double click count.
   */
  private static final int DBLCLICK_COUNT = 2;

  /**
   * Default stage icon.
   */
  private static final String ICON = "/icon.png";

  /**
   * Primary FXML.
   */
  private static final String PRIMARY_FXML = "primary.fxml";

  /**
   * Default logger.
   */
  private static final Logger LOG =
      LoggerFactory.getLogger(LauncherHelper.class);

  /**
   * Default scene.
   */
  private static Scene scene;

  /**
   * Default stage.
   */
  private static Stage stage;

  /**
   * Default tray.
   */
  private static java.awt.SystemTray tray;

  /**
   * Default tray icon.
   */
  private static java.awt.TrayIcon trayIcon;

  /**
   * Default constructor.
   */
  public LauncherHelper() {
    // nop
  }

  /**
   * Run the application.
   */
  public static void run() {
    if (LOG.isInfoEnabled()) {
      LOG.info(Launcher.getString("app.starting"));
    }
    Application.launch(LauncherHelper.class);
  }

  /**
   * Load FXML document into default scene.
   *
   * @param fxml name of FXML document without extension
   */
  public static void setRoot(final String fxml) {
    try {
      scene.setRoot(loadFxml(fxml));
    } catch (IOException ex) {
      if (LOG.isErrorEnabled()) {
        LOG.error("IO Exception", ex);
      }
    }
  }

  @Override
  public void start(final Stage defaultStage) {
    // TODO: Write to static field 'stage' from instance method 'start()'.
    // [SpotBugs] Dodgy code. Misuse of static fields.
    // This instance method writes to a static field. This is tricky to get
    // correct if multiple instances are being manipulated, and generally
    // bad practice.
    stage = defaultStage;
    setupScene();
    setupStage();
    setupTray();
    showStage();
  }

  @Override
  public void stop() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Application stop.");
    }
  }

  private static Parent loadFxml(final String fxml) throws IOException {
    final FXMLLoader fxmlLoader = new FXMLLoader(
        LauncherHelper.class.getResource(fxml));

    // MyController controller = (MyController)fxmlLoader.getController();
    // controller.setStage(this.stage);

    return fxmlLoader.load();
  }

  private static void setupScene() {
    try {
      scene = new Scene(loadFxml(PRIMARY_FXML), WIDTH, HEIGHT);
    } catch (IOException ex) {
      if (LOG.isErrorEnabled()) {
        LOG.error("IO Exception", ex);
      }
      scene = new Scene(new Group(), WIDTH, HEIGHT);
    }
  }

  private static void setupStage() {
    final Properties properties = Launcher.getProperties();
    final String applicationTitle =
        properties.getProperty("application.fullname");
    stage.setTitle(applicationTitle);
    try (InputStream iconAsStream =
        LauncherHelper.class.getResourceAsStream(ICON)) {
      if (iconAsStream == null) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Can't load application icon: '{}'", ICON);
        }
      } else {
        stage.getIcons().add(new Image(iconAsStream));
      }
    } catch (IOException ex) {
      if (LOG.isErrorEnabled()) {
        LOG.error("IO Exception", ex);
      }
    }
    stage.setScene(scene);
    // stage.sizeToScene();
    stage.setOpacity(OPACITY);
    stage.centerOnScreen();
    // stage.setFullScreen(true);
    stage.iconifiedProperty().addListener((ov, oldProp, newProp) -> {
      if (newProp.booleanValue()) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Stage iconified.");
        }
        hideStage();
      }
    });
    stage.setOnCloseRequest(we -> {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Close request.");
      }
      we.consume();
      hideStage();
    });
  }

  private static void hideStage() {
    javax.swing.SwingUtilities.invokeLater(() -> {
      if (trayIcon != null) {
        trayIcon.displayMessage(
            stage.getTitle(),
            "Java version: " + Runtime.version(),
            // ERROR, WARNING, INFO, NONE
            java.awt.TrayIcon.MessageType.INFO
        );
      }
    });
    stage.close();
  }

  private static void showStage() {
    if (stage != null) {
      if (stage.isIconified()) {
        stage.setIconified(false);
      }
      stage.show();
      stage.toFront();
    }
  }

  private static void setupTray() {
    javax.swing.SwingUtilities.invokeLater(() -> {
      try {
        java.awt.Toolkit.getDefaultToolkit();
        if (!java.awt.SystemTray.isSupported()) {
          throw new java.awt.AWTException("System tray not supported");
        }
        tray = java.awt.SystemTray.getSystemTray();

        trayIcon = new java.awt.TrayIcon(SwingFXUtils.fromFXImage(
            stage.getIcons().get(0), null));
        trayIcon.setImageAutoSize(true);

        // trayIcon.addActionListener(event -> {
        //   Platform.runLater(LauncherHelper::showStage);
        // });
        trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
          @Override
          public void mousePressed(final java.awt.event.MouseEvent event) {
            if (event.getClickCount() >= DBLCLICK_COUNT) {
              Platform.runLater(LauncherHelper::showStage);
            }
          }
        });

        final java.awt.Font plainFont = java.awt.Font.decode(null);
        final java.awt.Font boldFont = plainFont.deriveFont(java.awt.Font.BOLD);

        final java.awt.MenuItem openItem = new java.awt.MenuItem("Open");
        openItem.setFont(boldFont);
        openItem.addActionListener(event -> {
          Platform.runLater(LauncherHelper::showStage);
        });

        final java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
        exitItem.setFont(plainFont);
        exitItem.addActionListener(event -> {
          tray.remove(trayIcon);
          Platform.exit();
        });

        final java.awt.PopupMenu popup = new java.awt.PopupMenu();
        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.setToolTip(stage.getTitle());

        tray.add(trayIcon);

        Platform.setImplicitExit(false);
      } catch (java.awt.AWTException ex) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Unable to init system tray!", ex);
        }
        Platform.setImplicitExit(true);
      }
    });
  }

}
