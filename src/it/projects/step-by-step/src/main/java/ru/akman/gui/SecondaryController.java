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

import javafx.fxml.FXML;

/**
 * Secondary controller.
 */
public class SecondaryController {

  /**
   * Primary FXML.
   */
  private static final String PRIMARY_FXML = "primary.fxml";

  /**
   * Default constructor.
   */
  public SecondaryController() {
    // nop
  }

  @FXML
  private void switchToPrimary() {
    // TODO: Private method 'switchToPrimary()' is never called.
    // [SpotBugs] Perfomance.
    // This private method is never called. Although it is possible that
    // the method will be invoked through reflection, it is more likely that
    // the method is never used, and should be removed.
    LauncherHelper.setRoot(PRIMARY_FXML);
  }

}
