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

package ru.akman.maven.plugins.jpackage;

import java.io.File;
import java.util.List;
import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Module path.
 */
public class ModulePath {

  /**
   * Path elements.
   */
  private List<File> pathelements;
  
  /**
   * File sets.
   */
  private List<FileSet> filesets;

  /**
   * Directory sets.
   */
  private List<FileSet> dirsets;

  /**
   * Dependency sets.
   */
  private List<DependencySet> dependencysets;

  /**
   * Get path elements.
   *
   * @return the list of path elements
   */
  public List<File> getPathElements() {
    return this.pathelements;
  }

  /**
   * Set path elements.
   *
   * @param pathelements the list of path elements
   */
  public void setPathElements(final List<File> pathelements) {
    this.pathelements = pathelements;
  }

  /**
   * Get file sets.
   *
   * @return the list of file sets
   */
  public List<FileSet> getFileSets() {
    return this.filesets;
  }

  /**
   * Set file sets.
   *
   * @param filesets the list of file sets
   */
  public void setFileSets(final List<FileSet> filesets) {
    this.filesets = filesets;
  }

  /**
   * Get directory sets.
   *
   * @return the list of directory sets
   */
  public List<FileSet> getDirSets() {
    return this.dirsets;
  }

  /**
   * Set directory sets.
   *
   * @param dirsets the list of directory sets
   */
  public void setDirSets(final List<FileSet> dirsets) {
    this.dirsets = dirsets;
  }

  /**
   * Get dependency sets.
   *
   * @return the list of dependency sets
   */
  public List<DependencySet> getDependencySets() {
    return this.dependencysets;
  }

  /**
   * Set dependency sets.
   *
   * @param dependencysets the list of dependency sets
   */
  public void setDependencySets(final List<DependencySet> dependencysets) {
    this.dependencysets = dependencysets;
  }

}
