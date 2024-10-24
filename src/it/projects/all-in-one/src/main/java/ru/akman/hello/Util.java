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

import org.slf4j.LoggerFactory;

/**
 * Helper class for utilities
 */
public final class Util {

  /**
   * Private constructor.
   */
  private Util() {
    // not called
    throw new UnsupportedOperationException();
  }

  /**
   * Set logback classic root logger level to DEBUG.
   */
  public static void setRootLoggerLevelToDebug() {
    ch.qos.logback.classic.Logger rootLogger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
            ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
  }

}