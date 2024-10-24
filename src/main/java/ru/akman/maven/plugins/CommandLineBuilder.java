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

package ru.akman.maven.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * CommandLine builder is a wrapper added group of agruments to an option.
 */
public class CommandLineBuilder {

  /**
   * Building command line.
   */
  private final Commandline cmdLine;

  /**
   * List of command line options.
   */
  private final List<CommandLineOption> options;

  /**
   * Default constructor.
   */
  public CommandLineBuilder() {
    cmdLine = new Commandline();
    options = new ArrayList<>();
  }

  /**
   * Set executable.
   *
   * @param executable the executable path
   */
  public void setExecutable(final String executable) {
    cmdLine.setExecutable(executable);
  }

  /**
   * Create a new option (group of arguments).
   *
   * @return created empty option
   */
  public CommandLineOption createOpt() {
    final CommandLineOption opt = new CommandLineOption(cmdLine);
    options.add(opt);
    return opt;
  }

  /**
   * Create a new agrument and a new option for it, then
   * add created argument to this option.
   *
   * @return created option with added argument
   */
  public Arg createArg() {
    return createOpt().createArg();
  }

  /**
   * Build command line.
   *
   * @return builded command line
   */
  public Commandline buildCommandLine() {
    return cmdLine;
  }

  /**
   * Build list of options.
   *
   * @return builded option list
   */
  public List<String> buildOptionList() {
    return options.stream()
        .map(opt -> opt.toString())
        .collect(Collectors.toList());
  }

}
