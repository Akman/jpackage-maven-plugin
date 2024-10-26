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

// platformName ( linux | windows | mac )
// System.properties['os.name'].toLowerCase().contains('windows')
String platformName = "linux"

// archName ( amd64 | ?)
String archName = "amd64"

// application
String appImageName = ""
String appInstallerName = ""
String appImageVersion = ""
String delimiter = ""
String appInstallerExtension = ""

switch (platformName) {
    case 'windows':
        appImageName = "Launcher"
        appInstallerName = "launcher"
        appImageVersion = "1.0.0"
        delimiter = "_"
        appInstallerExtension = ".msi"
        break
    case 'linux':
        appImageName = "Launcher"
        appInstallerName = "launcher"
        appImageVersion = "1.0.0"
        delimiter = "_"
        appInstallerExtension = ".deb"
        break
    case 'mac':
        appImageName = "Launcher"
        appInstallerName = "launcher"
        appImageVersion = "1.0.0"
        delimiter = "_"
        appInstallerExtension = ".dmg"
        break
    default:
        assert false : "Unknown platform!"
        break
}

File appImage = new File(basedir, "target/jpackage/" + platformName
    + "/" + appImageName)
assert appImage.isDirectory()

File appInstaller = new File(basedir, "target/jpackage/" + platformName
    + "/" + appInstallerName + delimiter + appImageVersion
    + delimiter + archName
    + appInstallerExtension)
assert !appInstaller.isDirectory() && appInstaller.isFile()
