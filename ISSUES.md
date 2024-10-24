0) Добавить инфу как собирать и тестировать плагин
   ./mvnw integration-test -P run-its

1) plugin-info.md.vm


src/main/java/ru/akman/maven/plugins/BaseToolMojo.java:53:
warning: use of default constructor, which does not provide a comment
public abstract class BaseToolMojo extends AbstractMojo {

src/main/java/ru/akman/maven/plugins/jpackage/DependencySet.java:24:
warning: use of default constructor, which does not provide a comment
public class DependencySet {

target/generated-sources/plugin/com/github/akman/jpackage_maven_plugin/HelpMojo.java:28:
warning: use of default constructor, which does not provide a comment
public class HelpMojo

src/main/java/ru/akman/maven/plugins/jpackage/JPackageMojo.java:97:
warning: use of default constructor, which does not provide a comment
public class JPackageMojo extends BaseToolMojo {

src/main/java/ru/akman/maven/plugins/jpackage/Launcher.java:30:
warning: use of default constructor, which does not provide a comment
public class Launcher {

src/main/java/ru/akman/maven/plugins/jpackage/ModulePath.java:26:
warning: use of default constructor, which does not provide a comment
public class ModulePath {

src/main/java/ru/akman/maven/plugins/jpackage/PackageType.java:28:
warning: no comment
DEB,
DMG
EXE,
IMAGE,
MSI,
PKG,
PLATFORM,
RPM,



src/main/java/ru/akman/maven/plugins/jpackage/JPackageMojo.java:97:14:
[AbbreviationAsWordInName]
Аббревиатура в имени 'JPackageMojo' должна содержать
не более '1' последовательных заглавных букв. 

src/test/java/ru/akman/maven/plugins/jpackage/JPackageMojoTest.java:43:14:
[AbbreviationAsWordInName]
Аббревиатура в имени 'JPackageMojoTest' должна содержать
не более '1' последовательных заглавных букв.


======================================================================


[WARNING] The POM for com.github.akman:jlink-maven-plugin:jar:0.1.12 is missing, no dependency information available

[ERROR] Plugin com.github.akman:jlink-maven-plugin:0.1.12 or one of its dependencies could not be resolved:
[ERROR] 	com.github.akman:jlink-maven-plugin:jar:0.1.12 was not found in file:///home/akman/.m2/repository during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of local.central has elapsed or updates are forced
[ERROR] 	com.github.akman:jlink-maven-plugin:jar:0.1.12 was not found in https://repo.maven.apache.org/maven2 during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of central has elapsed or updates are forced: The following artifacts could not be resolved: com.github.akman:jlink-maven-plugin:jar:0.1.12 (absent): com.github.akman:jlink-maven-plugin:jar:0.1.12 was not found in file:///home/akman/.m2/repository during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of local.central has elapsed or updates are forced


assert appImage.isDirectory()
       |        |
       |        false
       /mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/step-by-step/target/jpackage/hello


======================================================================



# jpackage
--dest /mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/target/jpackage
--verbose
--app-version '1.0'
--copyright 'Copyright (C) 2020-2024 Alexander Kapitman'
--description 'Simple JavaFX application using slf4j, logback-classic and picocli libraries'
--name 'hello'
--vendor 'Akman'
--icon /mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/config/jpackage/linux/resources/hello.png
--module hello/ru.akman.hello.Main
--arguments '--gui'
--java-options '-Dfile.encoding=UTF-8 -Xms256m -Xmx512m'
--add-launcher hello-cli=/mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/target/launcher.16780215514025656526.properties
--file-associations /mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/config/jpackage/linux/associations.properties
--install-dir 'Akman/hello'
--license-file /mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/config/jpackage/LICENSE
--resource-dir /mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/config/jpackage/linux/resources
--win-dir-chooser
--win-menu
--win-menu-group 'Akman/hello'
--win-per-user-install
--win-shortcut
--win-upgrade-uuid '8CF81762-0B19-46A6-875E-1F839A1700D0'
--module-path /mnt/wsl/workspace/java/jpackage-maven-plugin/target/local-repo/ch/qos/logback/logback-classic/1.5.11/logback-classic-1.5.11.jar:/mnt/wsl/workspace/java/jpackage-maven-plugin/target/local-repo/info/picocli/picocli/4.7.6/picocli-4.7.6.jar:/mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/target/classes:/mnt/wsl/workspace/java/jpackage-maven-plugin/target/local-repo/org/slf4j/slf4j-api/2.0.15/slf4j-api-2.0.15.jar:/mnt/wsl/workspace/java/jpackage-maven-plugin/target/local-repo/ch/qos/logback/logback-core/1.5.11/logback-core-1.5.11.jar:/mnt/wsl/workspace/java/jpackage-maven-plugin/target/local-repo/org/openjfx/javafx-graphics/21.0.5/javafx-graphics-21.0.5-linux.jar:/mnt/wsl/workspace/java/jpackage-maven-plugin/target/local-repo/org/openjfx/javafx-base/21.0.5/javafx-base-21.0.5-linux.jar
--add-modules hello

/home/akman/.sdkman/candidates/java/23.0.1-tem/bin/jpackage @/mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/target/jpackage.opts
[21:13:12.759] jdk.jpackage.internal.PackagerException: Error: Option [--win-dir-chooser] is not valid on this platform
	at jdk.jpackage/jdk.jpackage.internal.Arguments.validateArguments(Arguments.java:597)
	at jdk.jpackage/jdk.jpackage.internal.Arguments.processArguments(Arguments.java:518)
	at jdk.jpackage/jdk.jpackage.main.Main.execute(Main.java:92)
	at jdk.jpackage/jdk.jpackage.main.Main.main(Main.java:53)



assert !appInstaller.isDirectory() && appInstaller.isFile()
       ||            |             |  |            |
       ||            false         |  |            false
       ||                          |  /mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/target/jpackage/hello-1.0.exe
       ||                          false
       |/mnt/wsl/workspace/java/jpackage-maven-plugin/target/it/projects/all-in-one/target/jpackage/hello-1.0.exe
       true
