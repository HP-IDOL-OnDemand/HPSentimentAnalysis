INSTALL
=======

`gradle installApp`

TEST AND RUNTIME REQUIREMENTS
=============================

Because this system connects with Twitter's REST API using a third-party library, you need to provide
your API key and secret during testing and at runtime.

`src/test/resources/twitter4j.properties.template` is a template file that you can use to facilitate
this during testing.

Rename (or make a copy) the file `twitter4j.properties` and write your consumer key and secret.

The template file includes additional properties, two of them are also mandatory to stablish a connection with Twitter.

RUN
===

Once installed,

    JAVA_OPTS="-Dtwitter4j.oauth.consumerKey=CONSUMER_KEY -Dtwitter4j.oauth.consumerSecret=CONSUMER_SECRET" \
    build/install/TwitterCollect/bin/TwitterCollect -h

Notice that you do not need to pass "-DenableApplicationOnlyAuth=true -Dhttp.useSSL=true" as Java options to
run the executable, even though this properties are defined in the template file.

During installations, these parameters are hardcoded into the executable so you only need to provide your
consumer key and secret.