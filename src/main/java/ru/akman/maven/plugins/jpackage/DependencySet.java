/*
  Copyright (C) 2020 - 2021 Alexander Kapitman

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

import java.util.List;

/**
 * Set of dependencies.
 */
public class DependencySet {

  /**
   * Should output directory be included into dependencyset.
   *
   * <p>Default value: false</p>
   */
  private boolean includeoutput;

  /**
   * Should automatic modules be excluded from dependencyset.
   *
   * <p>Default value: false</p>
   */
  private boolean excludeautomatic;

  /**
   * List of included dependencies (filename patterns).
   */
  private List<String> includes;

  /**
   * List of included dependencies (module name patterns).
   */
  private List<String> includenames;

  /**
   * List of excluded dependencies (filename patterns).
   */
  private List<String> excludes;

  /**
   * List of excluded dependencies (module name patterns).
   */
  private List<String> excludenames;

  /**
   * Should output directory be included into dependencyset.
   *
   * @return true if output directory be included into dependencyset
   */
  public boolean isOutputIncluded() {
    return this.includeoutput;
  }

  /**
   * Setter for includeoutput.
   *
   * @param includeoutput should output directory be included
   */
  public void setOutputIncluded(final boolean includeoutput) {
    this.includeoutput = includeoutput;
  }

  /**
   * Should automatic modules be excluded from dependencyset.
   *
   * @return true if automatic modules be excluded from dependencyset
   */
  public boolean isAutomaticExcluded() {
    return this.excludeautomatic;
  }

  /**
   * Setter for excludeautomatic.
   *
   * @param excludeautomatic should automatic modules be excluded
   */
  public void setAutomaticExcluded(final boolean excludeautomatic) {
    this.excludeautomatic = excludeautomatic;
  }

  /**
   * Get list of included dependencies by filename.
   *
   * @return the list of included dependency filenames (patterns)
   */
  public List<String> getIncludes() {
    return this.includes;
  }

  /**
   * Set the list of included dependencies by filename.
   *
   * @param includes included dependency filenames list (patterns)
   */
  public void setIncludes(final List<String> includes) {
    this.includes = includes;
  }

  /**
   * Get list of excluded dependencies by filename.
   *
   * @return the list of excluded dependency filenames (patterns)
   */
  public List<String> getExcludes() {
    return this.excludes;
  }

  /**
   * Set the list of excluded dependencies by filename.
   *
   * @param excludes excluded dependency filenames list (patterns)
   */
  public void setExcludes(final List<String> excludes) {
    this.excludes = excludes;
  }

  /**
   * Get list of included dependencies by name.
   *
   * @return the list of included dependency names (patterns)
   */
  public List<String> getIncludeNames() {
    return this.includenames;
  }

  /**
   * Set the list of included dependencies by name.
   *
   * @param includenames included dependency names list (patterns)
   */
  public void setIncludeNames(final List<String> includenames) {
    this.includenames = includenames;
  }

  /**
   * Get list of excluded dependencies by name.
   *
   * @return the list of excluded dependency names (patterns)
   */
  public List<String> getExcludeNames() {
    return this.excludenames;
  }

  /**
   * Set the list of excluded dependencies by name.
   *
   * @param excludenames excluded dependency names list (patterns)
   */
  public void setExcludeNames(final List<String> excludenames) {
    this.excludenames = excludenames;
  }

}
