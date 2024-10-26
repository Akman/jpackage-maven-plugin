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

String appImageName = "Launcher"

String platformName = "linux"
String appInstallerName = "launcher"
String appImageVersion = "1.0.0"
String archName = "amd64"
String appInstallerExtension = "deb"

String delimiter = "_"

File appInstaller = new File(basedir, "target/jpackage/" + platformName
    + "/" + appInstallerName + delimiter + appImageVersion
    + delimiter + archName
    + "." + appInstallerExtension)
assert !appInstaller.isDirectory() && appInstaller.isFile()
