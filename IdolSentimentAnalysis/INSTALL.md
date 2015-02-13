INSTALL
=======

`gradle installApp`

TEST AND RUNTIME REQUIREMENTS
=============================

Because this system connects with HP IdolOnDemand's API, you need to provide your API key during testing and at
runtime.

`src/test/resources/idol.properties.template` is a template file that you can use to facilitate this during testing.
Rename (or make a copy) the file `idol.properties` and write your API key.

You can also copy this file in `src/main/resources` to build the project including this information
or pass it at runtime as a `-D` system property.

RUN
===

    JAVA_OPTS="-DidolOnDemand.apiKey=your-api-key" \
    build/install/IdolSentimentAnalysis/bin/IdolSentimentAnalysis -h

On Windows

    set JAVA_OPTS=-DidolOnDemand.apiKey=your-api-key
    build\install\IdolSentimentAnalysis\bin\IdolSentimentAnalysis.bat -h
