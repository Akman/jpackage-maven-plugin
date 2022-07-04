/*
  Copyright (C) 2020 - 2022 Alexander Kapitman

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

package ru.akman.maven.plugins;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test helper class.
 */
public final class TestUtils {

  /**
   * Private constructor.
   */
  private TestUtils() {
    // not called
    throw new UnsupportedOperationException();
  }

  /**
   * Get the canonical path for specified file.
   * Caller test fails if IOException occures.
   *
   * @param file file
   *
   * @return canonical path
   */
  public static String getCanonicalPath(final File file) {
    String path = null;
    try {
      path = file.getCanonicalPath();
    } catch (IOException ex) {
      fail("Error: Unable to obtain canonical path [" + file.getPath() + "]."
          + System.lineSeparator()
          + ex.toString());
    }
    return path;
  }

  /**
   * Build the path contains all file paths from a list
   * separated by the default path separator.
   *
   * @param files list of files
   *
   * @return path contains all file paths from a list
   */
  public static String buildPathFromFiles(final List<File> files) {
    return buildPathFromFiles(files, File.pathSeparator);
  }

  /**
   * Build the path contains all file paths from a list
   * separated by the specified separator.
   *
   * @param files list of files
   * @param separator separator string
   *
   * @return path contains all file paths from a list
   */
  public static String buildPathFromFiles(final List<File> files,
      final String separator) {
    return files
      .stream()
      .map(TestUtils::getCanonicalPath)
      .collect(Collectors.joining(separator));
  }

  /**
   * Build the path contains all resolved file paths from a list
   * separated by the default path separator.
   *
   * @param base base path to resolve paths from the list
   * @param names list of relative paths
   *
   * @return path contains all resolved file paths from a list
   */
  public static String buildPathFromNames(final String base,
      final List<String> names) {
    return buildPathFromNames(base, names, File.pathSeparator);
  }

  /**
   * Build the path contains all resolved file paths from a list
   * separated by the specified separator.
   *
   * @param base base path to resolve paths from the list
   * @param names list of relative paths
   * @param separator separator string
   *
   * @return path contains all resolved file paths from a list
   */
  public static String buildPathFromNames(final String base,
      final List<String> names, final String separator) {
    return names
      .stream()
      .map(name -> {
        return getCanonicalPath(new File(base, name));
      })
      .collect(Collectors.joining(separator));
  }

  /**
   * Build the string contains all strings from a list separated by the
   * system line separator.
   *
   * @param names list of string
   *
   * @return one string contains all the specified strings from the list
   */
  public static String buildStringFromNames(final List<String> names) {
    return buildStringFromNames(names, System.lineSeparator());
  }

  /**
   * Build the string contains all strings from a list separated by the
   * specified separator.
   *
   * @param names list of string
   * @param separator separator string
   *
   * @return one string contains all the specified strings from the list
   */
  public static String buildStringFromNames(final List<String> names,
      final String separator) {
    return names
      .stream()
      .collect(Collectors.joining(separator));
  }

}
