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

/**
 * This module provided application launcher.
 */
module ru.akman.launcher {

  // picocli
  requires info.picocli;
  opens ru.akman.launcher to info.picocli;

  // slf4j + logback
  requires org.slf4j;
  requires java.naming;
  requires ch.qos.logback.classic;

  // javafx
  requires javafx.base;
  requires javafx.controls;
  requires javafx.graphics;
  requires javafx.fxml;
  requires javafx.swing;

  exports ru.akman.gui to javafx.graphics;

  opens ru.akman.gui to javafx.fxml;

}
