DESCRIPTION
===========

IdolSentimentAnalysis provides a library and a CLI application to perform sentiment analysis using
HP IdolOnDemand's API.

The standalone application expects input with the following format:

    0|the text you want to analyse
    1|another text
    2|I like this format
    3|but I don't like that the example is just four lines long

It analyses each line independently. The output produced has the following format:

    0|neutral|0.0
    
    2|positive|0.75
    2|like|format|0.75
    3|negative|-0.4
    3|don't like|null|-0.4

The sentiment analysis tool provides a summary (if it is a neutral, positive or negative text and a score) and,
in case it wasn't a neutral opinion, it provides the sentiments related with it with the form
`id|sentiment|topic|score`.

In the previous example, the first line was a neutral opinion. There was an error processing the second line and it
produced a blank line (this is the expected output on error). The third line was a positive opinion and the summary
is included with a detailed sentiment in the next line. The last example was a negative opinion where the topic
could not be detected and null was written instead.

SOURCE DESCRIPTION
==================

The main class with the cli interface is `com.lagunex.nlp.Main.java`.

`SentimentAnalysis` is the service we use to encapsulate the calls to IdolOnDemand's API.
To connect with the external REST API, we use Spring's `RestTemplate` with `jackson-databind` to parse response.
 
`Sentiment`, `Aggregate` and `SentimentResult` are Java class that match the JSON schema of the IdolOnDemand's response.

REQUIREMENTS
============

Because this system connects with HP IdolOnDemand's API, you need to provide your API key during testing and at
runtime.

This system depends on `spring-web` and `jackson-databind` to compile. Check `build.gradle` for details

RUN
===

Once installed,

    JAVA_OPTS="-DidolOnDemand.apiKey=your-api-key" \
    build/install/IdolSentimentAnalysis/bin/IdolSentimentAnalysis -h

On Windows

    set JAVA_OPTS=-DidolOnDemand.apiKey=your-api-key
    build\install\IdolSentimentAnalysis\bin\IdolSentimentAnalysis.bat -h

This projected is licensed under the terms of the MIT license.
