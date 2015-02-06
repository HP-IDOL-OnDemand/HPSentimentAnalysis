DESCRIPTION
===========

TwitterCollect provides a library and a CLI application to search Twitter data using Twitter's REST API.

The standalone application expects the query to be passed as a command line argument, e.g.

`TwitterCollect -n 3 '$HPQ'` will produce the following output:

    562959546118049794|Guide to Option Spreads http://t.co/K3CmFY9Ft9 $IBM $MSFT $HPQ|en|2015-02-04T13:03:09
    562958665461035010|RT @oleaheg: 3D Printing Industry Won't Be 'Tested' Until Hewlett-Packard, Others Arrive $ADSK $DDD $HPQ  http://t.co/eDjGj5KDNQ via @benzi…|en|2015-02-04T12:59:39
    562957797399490560|RT @ACInvestorBlog: $AAPL $GOOG $NVDA $QCOM $MSFT $HPQ $INTC Global tablet shipments to tumble 30% sequentially in 1Q15 http://t.co/coVYkcG…|en|2015-02-04T12:56:12

Each line includes the following information `id|message|lang|created_at`

If the query is not provided, it will search for '$HPQ' by default.

SOURCE DESCRIPTION
==================

The main class with the cli interface is `com.lagunex.nlp.Main.java`.

To connect with Twitter, we use the Java library Twitter4j.

`Tweet` is a JavaBean like class describes the content of a Tweet.
 
`TwitterClient` is the entry point of our library. It encapsulates the use of Twitter4j and provides basic functionality
to use Twitter's search REST API.

REQUIREMENTS
============

Because this system connects with Twitter's API, you need to provide your consumer key and secret. The connection
with Twitter is app based.

This system depends on `twitter4j`. Check `build.gradle` for details

RUN
===

Once installed,

    JAVA_OPTS="-Dtwitter4j.oauth.consumerKey=CONSUMER_KEY -Dtwitter4j.oauth.consumerSecret=CONSUMER_SECRET" \
    build/install/TwitterCollect/bin/TwitterCollect -h

On Windows

    set JAVA_OPTS=-Dtwitter4j.oauth.consumerKey=CONSUMER_KEY -Dtwitter4j.oauth.consumerSecret=CONSUMER_SECRET
    build\install\TwitterCollect\bin\TwitterCollect.bat -h

This projected is licensed under the terms of the MIT license.