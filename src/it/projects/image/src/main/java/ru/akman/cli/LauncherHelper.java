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

package ru.akman.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.akman.launcher.Launcher;

/**
 * CLI interface.
 */
public final class LauncherHelper {

  /**
   * Default logger.
   */
  private static final Logger LOG =
      LoggerFactory.getLogger(LauncherHelper.class);

  private LauncherHelper() {
    // not called
    throw new UnsupportedOperationException();
  }

  /**
   * CLI Application launcher.
   */
  public static void run() {
    if (LOG.isInfoEnabled()) {
      LOG.info(Launcher.getString("app.starting"));
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        System.in, Charset.defaultCharset()))) {
      while (true) {
        final String line = reader.readLine();
        if (line == null) {
          break;
        }
        final int length = line.length();
        if (length == 0) {
          break;
        }
        if (LOG.isDebugEnabled()) {
          LOG.debug("length: {}", length);
          LOG.debug("string: '{}'", line);
          LOG.debug("bytes: {}", Arrays.toString(
              line.getBytes(Charset.defaultCharset())));
        }
        System.out.println(line);
      }
    } catch (IOException ex) {
      if (LOG.isErrorEnabled()) {
        LOG.error("IO Exception", ex);
      }
    }
  }

}
