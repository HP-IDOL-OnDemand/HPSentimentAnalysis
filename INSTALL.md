DESCRIPTION
===========

HPSentimentAnalysis uses Gradle as the build automation system. Each subsystem is organized using 
Maven's standard directory layout.

REQUIREMENTS
============

In order to run this project you need the following softwares in your machine.

- Java 8 SE http://www.java.com/es/
- Gradle http://gradle.org/
- Maven http://maven.apache.org/

INSTALL
=======

To compile and create executables, run `gradle installApp`.

This will install executables in `subproject_dir/build/install` for each subproject. 

To run the data visualization system

`
JAVA_OPTS="-Dvertica.hostname=192.168.1.17 -Dvertica.database=topcoder -Dvertica.username=dbadmin -Dvertica.password=password" \
DataVisualization/build/install/DataVisualization/bin/DataVisualization
`

Test requirements
-----------------

Projects TwitterCollect, IdolSentimentAnalysis and VerticaConnections required some system properties to be
defined in order to run their tests and executables.

This variables can be defined in properties files located at `subprject_dir/src/test/resources`.
The project provides `.template` files in those directories to guide you.

Please refer to each project's INSTALL.md for further info on how to run the tests.

RUN
===

Do not use `gradle run` to run a project, instead use the executable produced by `gradle installApp` which
include all dependencies and receives system properties through the JAVA_OPTS enviroment variable. Refer to each
subprject's README.md for further info