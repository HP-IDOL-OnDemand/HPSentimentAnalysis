#!/bin/bash
mvn install:install-file -Dfile=libs/vertica-jdbc-7.1.1-0.jar -DgroupId=com.vertica -DartifactId=vertica-jdbc -Dversion=7.1.1.0 -Dpackaging=jar
