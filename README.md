DESCRIPTION
===========

HPSentimentAnalysis is a Java project with four independent subsystems:

1. TwitterCollect: A system to collect Tweets using Twitter's REST API for search,
   given a search query and some basic parameters.
2. IdolSentimentAnalysis: A system to perform Sentiment Analysis over text using
   HP IdolOnDemand's Sentiment Analysis API.
3. VerticaConnection: A system to connect to a HP Vertica database and perform queries to explore the data.
   This system also:
   3.1 Creates tbl files from output generated by the previous two subsystems to insert the results in a database
   3.2 Provide sql scripts to create tables and load data into a HP Vertica database system
4. DataVisualization: A standalone java system that uses VerticaConnection to create visual representations of the data.

For further information, each subsystem include the following files:

- README.md: Describes the subsystem functionality
- INSTALL.md: Describes the main gradle tasks to run the subsystem.
- LICENCE.md

REQUIREMENTS
============

In order to run this project you need the following software in your machine.

- Java 8 SE http://www.java.com/es/
- Gradle http://gradle.org/
- Maven http://maven.apache.org/

This projected is licensed under the terms of the MIT license.
