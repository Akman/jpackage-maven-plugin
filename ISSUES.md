1) Use of default constructor, which does not provide a comment

2) Auto generates into ./doc 
   - plugin-info.md.vm
   - jpackage-mojo.md.vm
   - help-mojo.md.vm

3) jpackage (WINDOWS)

jpackage.dll - jpackage.cpp:130 (Java_jdk_jpackage_internal_ExecutableRebrander_versionSwap)
ERROR: Exception with message 'VersionInfo.cpp(154) at VersionInfo::apply()
Missing mandatory FILEVERSION property

Using custom package resource [icon] (loaded from file Launcher.ico).
Warning: Windows Defender may prevent jpackage from functioning.
If there is an issue, it can be addressed by either disabling realtime monitoring,
or adding an exclusion for the directory
"C:\Programs\msys\tmp\jdk.jpackage6043440129317178231".

Using custom package resource [Template for creating executable properties file] (loaded from Launcher.properties).
java.lang.RuntimeException: Failed to update version information for
C:\Programs\msys\tmp\jdk.jpackage6043440129317178231\images\win-msi.image\Launcher\Launcher.exe
at jdk.jpackage/jdk.jpackage.internal.ExecutableRebrander.versionSwapWrapper(ExecutableRebrander.java:248)

Command: jpackage @jpackage.opts
File: jpackage.opts

# jpackage
--dest F:\Workspace\Java\jpackage-maven-plugin\target\it\projects\all-in-one\target\jpackage\windows
--type msi
--verbose
--app-version '1.0.0'
--copyright 'Copyright (C) 2019 - 2024 Alexander Kapitman <akman.ru@gmail.com>'
--description 'JavaFX application boilerplate project using gradle and maven build tools. This project aims to cover best practices for JavaFX application development as a whole. It provides tool recommendations for linting, testing and packaging.'
--name 'Launcher'
--vendor 'Akman'
--icon F:\Workspace\Java\jpackage-maven-plugin\target\it\projects\all-in-one\package\windows\resources\Launcher.ico
--input F:\Workspace\Java\jpackage-maven-plugin\target\it\projects\all-in-one\package\windows\input
--module ru.akman.launcher/ru.akman.launcher.Launcher
--arguments '--debug --gui'
--java-options '-Xms256m -Xmx512m -Dfile.encoding=UTF-8 -splash:\$APPDIR/splash.png'
--add-launcher launcher-cli=F:\Workspace\Java\jpackage-maven-plugin\target\it\projects\all-in-one\target\launcher.5874560373828761859.properties
--file-associations F:\Workspace\Java\jpackage-maven-plugin\target\it\projects\all-in-one\package\windows\associations.properties
--install-dir 'Akman/Launcher'
--license-file F:\Workspace\Java\jpackage-maven-plugin\target\it\projects\all-in-one\LICENSE
--resource-dir F:\Workspace\Java\jpackage-maven-plugin\target\it\projects\all-in-one\package\windows\resources
--win-dir-chooser
--win-menu
--win-menu-group 'Akman/Launcher'
--win-per-user-install
--win-shortcut
--win-upgrade-uuid '8CF81762-0B19-46A6-875E-1F839A1700D0'
--module-path F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\org\openjfx\javafx-controls\23.0.1\javafx-controls-23.0.1-win.jar;F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\org\openjfx\javafx-fxml\23.0.1\javafx-fxml-23.0.1-win.jar;F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\ch\qos\logback\logback-classic\1.5.11\logback-classic-1.5.11.jar;F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\org\openjfx\javafx-graphics\23.0.1\javafx-graphics-23.0.1-win.jar;F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\org\slf4j\slf4j-api\2.0.16\slf4j-api-2.0.16.jar;F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\ch\qos\logback\logback-core\1.5.11\logback-core-1.5.11.jar;F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\org\openjfx\javafx-swing\23.0.1\javafx-swing-23.0.1-win.jar;F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\org\openjfx\javafx-base\23.0.1\javafx-base-23.0.1-win.jar;F:\Workspace\Java\jpackage-maven-plugin\target\it\projects\all-in-one\target\classes;F:\Workspace\Java\jpackage-maven-plugin\target\local-repo\info\picocli\picocli\4.7.6\picocli-4.7.6.jar
