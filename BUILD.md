# How to build JPackage Maven Plugin

It is recommended to always execute a build with the wrappers to ensure
a reliable, controlled and standardized execution of the build.
Using the wrappers looks almost exactly like running the build with
the Maven installation. Depending on the operating system you either
run `mvnw` or `mvnw.cmd` command.

## Generate or update maven wrapper

Generating the wrapper files requires an installed version of the Maven
on your machine.

```console
mvn wrapper:wrapper
```

To switch the version of the Maven used to build a project simply pass in
the new version.

```console
mvn wrapper:wrapper -Dmaven=3.9.9
```

## Setup maven properties

You can set settings by export environment variable `MAVEN_OPTS` for example:

```console
export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8"
```

## Update dependencies

Discover dependency updates

```console
./mvnw versions:display-dependency-updates
```

Displays all dependencies

```console
./mvnw dependency:tree
```

## Check and fix header consistency

Check for header consistency.

```console
./mvnw validate license:check
```

Fix header consistency.

```console
./mvnw validate license:format
```

## Build and install into local repository

```console
./mvnw clean verify install
```

## Run integration tests

Run all tests:
```console
./mvnw integration-test -P run-its
```

Run selected tests:
```console
./mvnw integration-test -P run-its -Dinvoker.test='image','step-by-step'
```

## Build site and documentation

```console
./mvnw validate site
```

## Release plugin

```console
./mvnw release:clean release:prepare release:perform
```
