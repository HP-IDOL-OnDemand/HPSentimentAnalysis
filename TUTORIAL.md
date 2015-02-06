<!-- {{{ Introduction -->
[Sentiment Analysis](http://en.wikipedia.org/wiki/Sentiment_analysis) allows us to determine the "attitude" of a speaker. For instance, we would like to read tweets from an event of our interest to identify which tweets are positive opinions, which are negative opinions and moreover, what's the specific sentiment they are expressing. Is it love, hate, gratefulness?

In this tutorial, we are going to build a Java system with the following functionalities:

1. [Extracts Tweets](#twitter-collect) from a specific search using Twitter Search API.
2. [Analyse them](#sentiment-analysis) using a Sentiment Analysis API.
3. [Store them](#vertica) in a database specialized in big data and analytics.
4. [Provide a GUI](#data-visualization) application to visualize the data analysed.

The source code for this project is available as open source. You can download it from its [Bitbucket repository](https://bitbucket.org/lagunex/hpsentimentanalysis).

<!--more-->

The Sentiment Analysis API is developed by [IBM IdolOnDemand](http://www.idolondemand.com), a platform that offers developers different tools to extract meaning from unstructured data like tweets, videos and pictures.

We use [IBM Vertica Analytics Platform](http://www.vertica.com/about/) as our database. This platform is specially designed to store more data and run queries faster than our traditional solutions.

### Technologies used

- [Java 8](http://www.java.com/)
- [Twitter Search API](https://dev.twitter.com/rest/public/search)
- [twitter4j](http://twitter4j.org/en/index.html)
- [IdolOnDemand](http://www.idolondemand.com/)
- [Spring](http://spring.io/)
- [HP Vertica](http://www.vertica.com/)
- [Java FX](http://docs.oracle.com/javase/8/javase-clienttechnologies.htm)
- [Gradle](http://gradle.org/)
- [Maven](http://maven.apache.org/)

<!-- }}} -->

<!-- {{{ # Set up gradle -->
# Setting up our project

To keep our system organized, we are going to work with a project composed of four subprojects, one for each of our functionalities.

We use Gradle as our build automation system. It allows to work with subproject and takes control over the building process, handling dependencies, compiling, testing and building executables for us. You can install it following [their instructions](http://gradle.org/installation) or, if you have a Mac, just run `brew install gradle`.

The following sections assume that the Gradle multiproject is already set up and that you are in the root directory. We explain this in detail [at the end of the tutorial](#detailed-setup) but right now we want to focus on our system functionalities.
<!-- }}} -->

<!-- {{{ # TwitterCollect -->
# <a name="twitter-collect"></a> Twitter Search

We are going to implement a [Singleton](http://en.wikipedia.org/wiki/Singleton_pattern) service `TwitterClient` that receives a [query string](https://dev.twitter.com/rest/public/search) and a method reference using [lambda expressions](http://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) to consume our tweets.

If you downloaded the source code, go to the `TwitterCollect` project before continuing.

<!-- {{{ ## Defining our interface -->
## Defining our interface

Following [TDD](http://en.wikipedia.org/wiki/Test-driven_development) principles, let's build a [JUnit](http://junit.org/) test for this scenario. In our test, the lambda expression will add our tweets to a list.

[code collapse="true" language="java" title="src/test/java/com/lagunex/twitter/TwitterClientTest.java"]

    // imports are remove for brevity
    
    public class TwitterClientTest {
        @Test
        public void search() {
            TwitterClient client = TwitterClient.getInstance();
            String sampleQuery = "lagunex -filter:retweets";
            ArrayList<Tweet> result = new ArrayList<>();
            client.search(sampleQuery, tweet -> result.add(tweet));
            assertTrue(result.size() > 0);
        }
    }

[/code]
<!-- }}} -->

<!-- {{{ ## Implementing the search method -->
## Implementing the search method

Before we connect with Twitter, we need to create a new app in their [Application Manager](https://apps.twitter.com/). This will give us a "Consumer Key (API Key)" and a "Consumer Secret (API Secret)", which are the credential we will use to authenticate ourselves.

<!-- {{{ ### Using twitter4j to interact with Twitter -->
### Using twitter4j to interact with Twitter

If we wanted to implement the connection from scratch, we would have to handle OAuth authentication, HTTP connections, JSON parsing and so on. Luckily, there is a java library which handle all this low level operation for us, [twitter4j](http://twitter4j.org/en/index.html), so we add it as a dependency in our `build.gradle` script and we are ready to go:

[code collapse="true" title="build.gradle"]

    dependencies {
        compile group: 'org.twitter4j', name: 'twitter4j-core', version: '4.0.2'
    }

[/code]
 
The next step is to tell twitter4j our app credentials so it can connect with Twitter. The library offers different ways to [configure](http://twitter4j.org/en/configuration.html) this. For this tutorial, we are going to create a file named `twitter4j.properties` in `src/test/resources`.

[code collapse="true" title="src/test/resources/twitter4j.properties"]

    oauth.consumerKey=YOUR_CONSUMER_KEY
    oauth.consumerSecret=YOUR_CONSUMER_SECRET
    enableApplicationOnlyAuth=true
    http.useSSL=true

[/code]

That's it! twitter4j is ready to work. Let's write some code.
<!-- }}} -->

<!-- {{{ ### Writing our Client -->
### Writing our Client

twitter4j offers a Singleton to interact with the REST API. We create the class `TwitterClient` to encapsulate this Singleton and expose only the functionality we are interested in.

[code collapse="true" language="java" title="src/main/java/com/lagunex/twitter/TwitterClient.java" highlight="14,15,22,24,25"]

    // imports are remove for brevity
    
    public class TwitterClient {
        private static TwitterClient instance;
        public static TwitterClient getInstance() {
            if (instance == null) {
                instance = new TwitterClient();
            }
            return instance;
        }
        
        private TwitterClient() {
            try {
                twitter4j = TwitterFactory.getSingleton(); // singleton configure with twitter4j.properties
                twitter4j.getOAuth2Token(); // this line is mandatory for twitter4j to connect with Twitter
            } catch (TwitterException e) {
                // handle exception
            }
        }
    
        public void search(String query, Consumer<Tweet> consumer) {
            Query q = new Query(query);
            try {
                QueryResult qr = twitter4j.search(q);
                for(Status s: qr.getTweets()) {
                    consumer.accept(new Tweet(s));
                }
            } catch (TwitterException e) {
                // handle exception
            }
        }
    }

[/code]

The use of twitter4j is highlighted. Our method `search` takes advantage of lambda expressions from Java 8. The `consumer` parameter, which can be defined as a lambda by the caller, will handle each tweet.

For this application, we only use four parameters from a tweet: `id`, `message`, `language` and `createdAt`. We defined a class `Tweet` that extracts this attributes from a `twitter4j.Status` object.

[code collapse="true" language="java" title="src/main/java/com/lagunex/twitter/Tweet.java"]

    // imports are remove for brevity
    
    public class Tweet {
        private final long id;
        private final String message;
        private final LocalDateTime createdAt;
        private final String language;
    
        public Tweet(Status s) {
            id = s.getId();
            message = s.getText();
            language = s.getLang();
            createdAt = LocalDateTime.ofInstant(s.getCreatedAt().toInstant(),ZoneOffset.UTC);
        }
    
        @Override
        public String toString() {
            return String.format("%d|%s|%s|%s",id,message,language,createdAt);
        }
    
        // getter and setters are not shown for brevity
    }

[/code]

Now we run our test to see the result:

[code collapse="true" light="true" title="command line"]

    $ gradle test
    :TwitterCollect:compileJava
    :TwitterCollect:processResources
    :TwitterCollect:classes
    :TwitterCollect:compileTestJava
    :TwitterCollect:processTestResources
    :TwitterCollect:testClasses
    :TwitterCollect:test
    
    BUILD SUCCESSFUL
    
    Total time: 6.107 secs
    $

[/code]

To finish this part, let's write a small `main` application to be able to search for tweets from the command line:

[code collapse="true" language="java" title="src/main/java/com/lagunex/twitter/Main.java"]

    public class Main {
        public static void main(String[] args) {
            TwitterClient.getInstance().search(args[0], tweet -> System.out.println(tweet));
        }
    }

[/code]

We need to copy `src/test/resources/twitter4j.properties` into the `src/main/resources` directory to configure twitter4j at runtime. Finally, run `gradle installApp` to build the executable. It will be saved in `build/install/TwitterCollect` with all its dependencies.

[code collapse="true" light="true" title="command line"]

    $ cd build/install/TwitterCollect
    $ ./bin/TwitterCollect "lagunex -filter:retweets"
    563023115459260416|@sirlordt @ninfarave  ¡Oh! He ganado en #LagunexDomino para Android 103-0 https://t.co/xSpcq551FV|es|2015-02-04T17:15:45
    560501402016186368|Yes! I've just won in #LagunexDomino for Android https://t.co/ZiLHPNdqe8|en|2015-01-28T18:15:22
    $

[/code]

The first functionality of our system is complete. Let's perform Sentiment Analysis on those tweets.
<!-- }}} -->
<!-- }}} -->
<!-- }}} -->

<!-- {{{ # IdolSentimentAnalysis -->
# <a name="sentiment-analysis"></a> IdolOnDemand Sentiment Analysis

[IdolOnDemand](https://www.idolondemand.com) offers the API [analyzesentiment](https://www.idolondemand.com/developer/apis/analyzesentiment) to perform Sentiment Analysis over text. We need to implement a library that encapsulates this service and exposes a Java interface to perform the analysis.

As we can see in its documentation, the service [receives](https://www.idolondemand.com/developer/apis/analyzesentiment#request) the text to analyse and an optional parameter to specify the text's language, in case it is not English. Its [JSON response](https://www.idolondemand.com/developer/apis/analyzesentiment#response) includes the results of the analysis. You can [try the API](https://www.idolondemand.com/developer/apis/analyzesentiment#try) to get familiar with its behaviour. 

If you downloaded the source code, go to the `IdolSentimentAnalysis` project before continuing.

<!-- {{{ ## Defining our interface -->
## Defining our interface

We want a Singleton with a method `analyse` that receives the text and language and returns the result in a [POJO](http://es.wikipedia.org/wiki/Plain_Old_Java_Object) that matches the interesting attribute of the JSON response. Our test for this interface looks like this:

[code collapse="true" language="java" title="src/test/java/com/lagunex/nlp/SentimentAnalysisTest.java"]

    // imports are remove for brevity
    
    public class SentimentAnalysisTest {
        @Test
        public void analyse() {
            SentimentAnalysis engine = SentimentAnalysis.getInstance(); 
            SentimentResult t = engine.analyse("This is a good day", "eng");
            assertNotNull(t);
            assertNotNull(t.getPositive());
            assertNotNull(t.getNegative());
            assertNotNull(t.getAggregate());
        }
    }

[/code]
<!-- }}} -->

<!-- {{{ ## Implementing the analyse method -->
## Implementing the analyse method

To connect with the IdolOnDemand platform, we need an API key. Create an [IdolOnDemand Account](https://www.idolondemand.com/account/account.html) (it's free), go to [Manage your API Keys](https://www.idolondemand.com/account/api-keys.html) and generate a new one.

<!-- {{{ ### Consuming a REST service -->
### Consuming a REST service

Following the same strategy we used for `TwitterClient`, we are going to rely on third-party libraries to stablish the HTTP connection with the IdolOnDemand platform and parse its JSON into a POJO. In this case we will use [spring-web](http://spring.io/guides/gs/consuming-rest/) and its `RestTemplate` to consume the REST service and [jackson-databind](https://github.com/FasterXML/jackson-databind/) (used internally by `RestTemplate`) to parse the response. Therefore we need to add these dependencies to our `build.gradle` script:

[code collapse="true" light="yes" title="build.gradle"]

    dependencies {
        compile 'org.springframework:spring-web:4.1.4.RELEASE'
        compile 'com.fasterxml.jackson.core:jackson-databind:2.5.0'
    }

[/code]
<!-- }}} -->

<!-- {{{ ### Writing our SentimentAnalysis service -->
### Writing our SentimentAnalysis service

Our service needs to access the following information: the API endpoint, the API key, the text to analyse and its language. The API endpoint is constant, so we can define it directly in the class. The API key will be passed at runtime as a [System Property](http://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html) and the text and language will be our method parameters. This four requirements are highlighted in our code. Because IdolOnDemand receives its parameters as GET parameters, we need to encode the text we want to analyse.

[code collapse="true" language="java" title="src/main/java/com/lagunex/nlp/SentimentAnalysis.java" highlight="4,17,20,25,26,27"]

    // imports are remove for brevity
    
    public class SentimentAnalysis {
        private static final String URL = "https://api.idolondemand.com/1/api/sync/analyzesentiment/v1";
        
        private static SentimentAnalysis instance;
        private final String API_KEY;
    
        public static SentimentAnalysis getInstance() {
            if (instance == null) {
                instance = new SentimentAnalysis();
            }
            return instance;
        }
        
        private SentimentAnalysis(){
            API_KEY = System.getProperty("idolOnDemand.apiKey");
        }
    
        public SentimentResult analyse(String opinion, String language) {
            opinion = encode(opinion);
            RestTemplate rest = new RestTemplate();
            
            // calls the API and parse the JSON response into a Java object
            return rest.getForObject(
                    String.format("%s?apikey=%s&text=%s&language=%s", URL, API_KEY, opinion, language),
                    SentimentResult.class
            ); 
        }
    
        private String encode(String opinion) {
            String encoded = null;
            try {
                encoded = URLEncoder.encode(opinion, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                // handle exception 
            } 
            return encoded;
        }
    }

[/code]

The interesting part of this code is that we do not have to know anything about HTTP connections, status code or JSON parsing. It is all handled automatically by the highlighted method `rest.getForObject`.
This method receives the URL we want to call and a Java class specifying how we want to parse the result. This Java class must match the [response](https://www.idolondemand.com/developer/apis/analyzesentiment#response) of the REST API to work properly. Therefore, we need to define the classes `SentimentResult`, `Sentiment` and `Aggregate` to comply to the JSON schema:

[code collapse="true" language="java" title="src/main/java/com/lagunex/nlp/SentimentResult.java"]

    // imports are remove for brevity
    
    public class SentimentResult {
        private List<Sentiment> positive;
        private List<Sentiment> negative;
        private Aggregate aggregate;
    
        // getters and setters omitted for brevity
    }

[/code]

[code collapse="true" language="java" title="src/main/java/com/lagunex/nlp/Sentiment.java" highlight="3"]

    // imports are remove for brevity
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Sentiment {
        private String sentiment;
        private String topic;
        private double score;
    
        @Override
        public String toString() {
            return String.format("%s|%s|%s", sentiment, topic, score);
        }
    
        // getters and setters omitted for brevity
    }

[/code]

The `@JsonIgnoreProperties` annotation allow us skip the definition of the attributes we are not interested in. In this case `original_text`, `original_length`, `normalized_text` and `normalized_length`. `jackson-databind` will ignore this JSON parameters during parsing.

[code collapse="true" language="java" title="src/main/java/com/lagunex/nlp/Aggregate.java"]

    public class Aggregate {
    
        private String sentiment;
        private double score;
    
        @Override
        public String toString() {
            return String.format("%s|%s", sentiment, score);
        }
    
        // getters and setters omitted for brevity
    }

[/code]

Our service is ready to use, now we run our tests:

[code collapse="true" light="true" title="command line"]

    $ gradle test
    :IdolSentimentAnalysis:compileJava
    :IdolSentimentAnalysis:processResources
    :IdolSentimentAnalysis:classes
    :IdolSentimentAnalysis:compileTestJava
    :IdolSentimentAnalysis:processTestResources
    :IdolSentimentAnalysis:testClasses
    :IdolSentimentAnalysis:test
    
    BUILD SUCCESSFUL
    
    Total time: 17.232 secs
    
    $

[/code]

To conclude this part, let's write a small `main` application, just like we did with our TwitterClient:

[code collapse="true" language="java" title="src/main/java/com/lagunex/nlp/Main.java"]

    public class Main {
        public static void main(String[] args) {
            SentimentResult result = SentimentAnalysis.getInstance().analyse(args[0], args[1]);
            System.out.println(result.getAggregate());
            System.out.println(result.getPositive());
            System.out.println(result.getNegative());
        }
    }

[/code]

Finally, we install and run the executable, passing our `idolOnDemand.apiKey` at runtime using the JAVA_OPTS system variable.

[code collapse="true" light="true" title="command line" highlight="3"]

    $ gradle installApp
    $ cd build/install/IdolSentimentAnalysis
    $ JAVA_OPTS="-DidolOnDemand.apiKey=YOUR_KEY" ./bin/IdolSentimentAnalysis "I like cats" "eng"
    
    positive|0.7176687736973063
    [like|cats|0.7176687736973063]
    []
    
    $

[/code]
<!-- }}} -->
<!-- }}} -->
<!-- }}} -->

<!-- {{{ # VerticaConnection -->
# <a name="vertica"></a> Create a Vertica database

We are halfway through with our system. It can search for tweets and analyse them to extract their attitude. Now we need a database to store them and library to connect with the database and query its data.

[HP Vertica Analytics Platform](http://www.vertica.com/) offers a database system adapted to the requirements of Big Data: more storage and faster queries. You can try [HP Vertica Community Edition](https://my.vertica.com/community/) for free. Look at its [documentation center](http://www.vertica.com/hp-vertica-documentation/), [Started Guide](http://my.vertica.com/docs/6.0.x/HTML/index.htm#17488.htm) and [Reference Manual](https://my.vertica.com/docs/7.0.x/PDF/HP_Vertica_7.0.x_SQL_Reference_Manual.pdf) for further information.

One of the advantage of Vertica is that we interact with it using SQL just like we do with a regular database, making it very easy to create our tables and perform our queries.

If you downloaded the source code, go to the `VerticaConnection` project before continuing.

<!-- {{{ ## What data do we need to store -->
## What data do we need to store?

From our Twitter search we retrieved `id`, `message`, `language` and `createdAt`, and for each tweet the Sentiment Analysis tool returned an aggregate sentiment and score and a list of positive and negative sentiments found in each tweet.

It can be seen that the relation between a tweet and its aggregate sentiment information (sentiment and score) is 1:1 so we can use only one table to store both. We need an addition table for the list of sentiments as it relates 1:n with a tweet (a single message can hold more that one sentiment, e.g. "I love pizza and ice cream but I don't like chocolate").

The following SQL script will create those tables for us:

[code collapse="true" language="sql" title="scripts/create_tables.sql"]

    create table tweet
    (   tweetid             integer         not null primary key,
        message             varchar(140)    not null,
        lang                char(2)         not null,
        created_at          timestamp       not null,
        aggregate_sentiment varchar(10),
        aggregate_score     float
    );
    
    create table sentiment
    (   id        auto_increment primary key,
        tweet_id  integer not null,
        sentiment varchar(140),
        topic     varchar(140),
        score     float
    );
    
    alter table sentiment 
        add constraint fk_sentiment_tweet foreign key (tweet_id)
             references tweet (id)
    ;

[/code]

To populate our tables, we can use plain text [tbl files](http://file.org/extension/tbl). This files represent one entry per row and its attributes are separated by pipes "|", similar to "csv" files. The `toString()` methods we have defined so far help us with this. We only need to be careful to remove the "|" character from our tweet's message to avoid conflicts.

However, a `Tweet` object does not include aggregate information. This is gathered only after the Sentiment Analysis returns and it is stored in `Aggregate` objects so we need a way to combine this information. Our final tbl files, one for each table, should look like this:

[code collapse="true" light="true" title="db/tweet.tbl"]

    562113053282426880|When you have #MarshawnLynch...why the hell wouldn't you just run it in? #SB49 #SuperBowl|en|2015-02-02T04:59:30|neutral|0.0
    562112884223000576|Best moment of #sb15 #sb49! @waegn @deremann @RSherman_25 http://t.co/h13J07itIE via @9GAG|en|2015-02-02T04:58:49|positive|0.5787074952096031
    562112806552485888|Congrats to the Pats, @LG_Blount and @PatrickChung23 !!!!! #producks #SB49|en|2015-02-02T04:58:31|positive|0.8294462782412093

[/code]

Notice that the first four fields correspond to a `Tweet` while the last two correspond to its `Aggregate`.

[code collapse="true" light="true" title="db/sentiment.tbl"]

    562112884223000576|Best|null|0.5787074952096031
    562112806552485888|Congrats|team|0.8294462782412093
    562112694262562816|DEVASTATED|city|-0.5787074952096031
    562109760238256128|Congratulations|#Patriots|0.8294462782412093
    562109466716692480|Sad|fanatics|-0.40209773927084985

[/code] 

With this two files created, we only need a SQL script to load them:

[code collapse="true" language="sql" title="scripts/load_data.sql"]

    \set t_pwd `pwd`
    
    \set input_file '''':t_pwd'/tweet.tbl'''
    COPY tweet
    FROM :input_file DELIMITER AS '|' NULL AS 'null'
    DIRECT;
    
    \set input_file '''':t_pwd'/sentiment.tbl'''
    COPY sentiment (tweet_id, sentiment, topic, score) 
    FROM :input_file DELIMITER AS '|' NULL AS 'null'

[/code] 

The last step is to copy both SQL scripts and both tbl files into our Vertica server, connect to our database using the adminTools and run the scripts.

[caption id="attachment_2526" align="aligncenter" width="300"]<a href="http://lagunex.com/wp-content/uploads/2015/02/vertica.gif"><img src="http://lagunex.com/wp-content/uploads/2015/02/vertica-300x224.gif" alt="Creating a Vertica database" width="300" height="224" class="size-medium wp-image-2526" /></a> Creating a Vertica database[/caption]

<!-- }}} -->

<!-- {{{ ## Connecting Java with Vertica -->
Connecting Java with Vertica
----------------------------

Our database is created and populated. The next step is to write a Java library that communicates with it. For that, we need to create a Vertica account and download the [Vertica JDBC driver](https://my.vertica.com/download-community-edition/#drivers). Once downloaded, install it in your local Maven repository to facilitate its use.

[code collapse="true" light="true" title="command line"]

    mvn install:install-file \
            -Dfile=vertica-jdbc-7.1.1-0.jar \
            -DgroupId=com.vertica \
            -DartifactId=vertica-jdbc \
            -Dversion=7.1.1.0 \
            -Dpackaging=jar

[/code]

To stablish the connection and perform the queries, we are going to use [spring-jdbc](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/jdbc.html). Let's add its dependency and the JDBC driver dependency to `build.gradle`.

[code collapse="true" light="true" title="build.gradle"]

    dependencies {
        compile group: 'com.vertica', name: 'vertica-jdbc', version:'7.1.1.0'
        compile 'org.springframework:spring-jdbc:4.1.4.RELEASE'
    }

[/code]

Now we can define a Singleton class to query the database

[code collapse="true" language="java" title="src/main/java/com/lagunex/vertica/Vertica.java" highlight="16,19,20,24,25,26,27,28,29,30,43"]

    // imports are not show for brevity
    public class Vertica {
        private static final String HOSTNAME = "vertica.hostname";
        private static final String DATABASE = "vertica.database";
        private static final String USERNAME = "vertica.username";
        private static final String PASSWORD = "vertica.password";
    
        private static Vertica instance;
        public static Vertica getInstance() {
            if (instance == null) {
                instance = new Vertica();
            }
            return instance;
        }
    
        private final JdbcTemplate jdbcTemplate;
    
        private Vertica() {
            DataSource dataSource = createDataSource();
            jdbcTemplate = new JdbcTemplate(dataSource); 
        }
    
        private DataSource createDataSource() {
            SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
            dataSource.setDriverClass(com.vertica.jdbc.Driver.class);
            dataSource.setUrl(String.format("jdbc:vertica://%s:5433/%s",
                System.getProperty(HOSTNAME), System.getProperty(DATABASE)
            ));
            dataSource.setUsername(System.getProperty(USERNAME));
            dataSource.setPassword(System.getProperty(PASSWORD));       
            return dataSource;
        }
    
        public List<Map<String,Object>> getAggregateTotal(LocalDateTime start, LocalDateTime end) {
            StringBuilder query = new StringBuilder(100);
                query.append("select aggregate_sentiment as label, count(*) as total ")
                     .append("from tweet ")
                     .append("where created_at >= ? and created_at < ? ")
                     .append("  and aggregate_sentiment is not null ")
                     .append("group by label ")
                     .append("order by total desc ");
            
            return jdbcTemplate.queryForList(query.toString(), Timestamp.valueOf(start), Timestamp.valueOf(end));
        }
    }

[/code]

The use of `spring-jdbc` is highlighted on the code. The connection is stablished during `createDataSource()` and the query is executed in `jdbcTemplate.queryForList()`.

<!-- }}} -->

<!-- {{{ ## Testing our Vertica service -->
## Testing our Vertica service

We just defined a service that returns the total number of aggregate sentiments (neutral, positive and negative). Now we write a test class to verify it. `Vertica` reads the connection parameters `hostname`, `database`, `username` and `password` from System Properties as highlighted in lines 24-30. During test, we created a `vertica.properties` file where we add this information. Notice the `loadSystemProperties()` method that reads the file and save its info as System Properties.

[code collapse="true" light="true" title="src/test/resources/vertica.properties"]

    vertica.hostname=192.168.1.17
    vertica.database=topcoder
    vertica.username=dbadmin
    vertica.password=password

[/code]

[code collapse="true" language="java" title="src/test/java/com/lagunex/vertica/VerticaTest.java"]

    // imports are remove for brevity
    public class VerticaTest {
        Vertica vertica;
        
        @Before
        public void setUp() throws Exception {
            loadSystemProperties();
            vertica = Vertica.getInstance();
        }
    
        private void loadSystemProperties() throws Exception {
            Properties system = System.getProperties();
            InputStream is = VerticaTest.class.getResourceAsStream("/vertica.properties");
            system.load(is);
        }
    
        @Test
        public void getAggregateTotal() {
            LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 0);
            LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 8, 0);
            List<Map<String, Object>> result = vertica.getAggregateTotal(begin, end);
    
            assertEquals(3, result.size());
            List<String> labels = Arrays.asList(new String[]{"negative", "neutral", "positive"});
            result.stream().forEach((sample) -> {
                labels.contains(sample.get("label").toString());
            });
        }
    }

[/code]
 
Finally, we can run the test to check our implementation is correct.

[code collapse="true" light="true" title="command line"]

    $ gradle test
    :VerticaConnection:clean
    :VerticaConnection:compileJava
    :VerticaConnection:processResources
    :VerticaConnection:classes
    :VerticaConnection:compileTestJava
    :VerticaConnection:processTestResources
    :VerticaConnection:testClasses
    :VerticaConnection:test
    
    BUILD SUCCESSFUL
    
    Total time: 19.387 secs
    
    $

[/code]

Our test passed. We can now add more tests and public method to `Vertica.java` to gathered different analytics from our tweets.
<!-- }}} -->
<!-- }}} -->

<!-- {{{ # Data Visualization -->
# <a name="data-visualization"></a> Data Visualization using JavaFX

On the last part of this tutorial, we create a standalone [JavaFX](http://docs.oracle.com/javase/8/javase-clienttechnologies.htm) application to visualize the data from our database.

If you downloaded the source code, go to the `DataVisualization` project before continuing.

First of all, we need to add a dependency to our VerticaConnection in `build.gradle`.

[code collapse="true" light="true" title="build.gradle"]

    dependencies {
        compile project(":VerticaConnection")
    }

[/code]

The GUI application has the following functionality:

- Set a time range to collect anylitics.
- See the number of tweets group by aggregate sentiment (neutral, negative or positive) in a pie chart.
- See the number of tweets from the top 15 sentiments in a pie chart.
- See how the average aggregate score evolved during the given time with a line chart.
- See the evolution of the number of tweets from the top 15 sentiments during the given time with a line chart.
- See all tweets that correspond to a given sentiment (aggregate or specific) or in a given time window. To do this, locate the data table on the left side of any chart, right click on a time window or sentiment cell and select "view tweets with..." to open a popup window with the related tweets.

To create the charts, we use [JavaFX Charts](http://docs.oracle.com/javase/8/javafx/user-interface-tutorial/charts.htm#JFXUI577) which are UI components already available to display the data.

A GUI application usually involves a lot of code to set up the different components and bind them to their corresponding behaviour. Giving a detailed explanation of how to create one goes beyond the scope of this tutorial. Therefore, we will only describe the components used and point to additional resources that can help you with the details.

- The application shows the data using a [TableView](http://docs.oracle.com/javafx/2/ui_controls/table-view.htm).
- The data seen in the table is also displayed as charts using [Pie Charts](http://docs.oracle.com/javase/8/javafx/user-interface-tutorial/pie-chart.htm#CIHFDADD) and [Line Charts](http://docs.oracle.com/javase/8/javafx/user-interface-tutorial/line-chart.htm#CIHGBCFI), each representing a different analytic from the data.
- The user select the time range he wants to analyse and the application update the charts accordingly. Each chart is generated with data coming from a specific method of our `Vertica` service. This method are similar to the one we defined before, adapting the SQL query to each chart requirement.
- An additional method that returns the tweet message is implemented in `Vertica` and is used in the GUI to display all tweets from a given time or sentiment. This functionality is implemented as a [ContextMenu](http://docs.oracle.com/javafx/2/ui_controls/menu_controls.htm)

The following GIF image illustrates the main functionality of our application.

<img src="http://lagunex.com/wp-content/uploads/2015/02/app.gif" alt="Data Visualization with JavaFX" width="797" height="620" class="aligncenter size-full wp-image-2525" />

<!-- }}} -->

<!-- {{{ Multiproject setup -->
# <a name="detailed-setup"></a>Setting up our gradle multiproject

We decided to split each functionality in its own subproject. After all, they are independent and can be decoupled easily. To work with a multiproject, we create a subdirectory for each subproject and add a `settings.gradle` file in our root directory.

[code collapse="true" light="true" title="settings.gradle"]

    include 'TwitterCollect',
            'IdolSentimentAnalysis',
            'DataVisualization',
            'VerticaConnection'

[/code]

This is the final layout for our system:

[code collapse="true" light="true" title="directory layout"]

    HPSentimentAnalysis
    | common.gradle
    | build.gradle
    | settings.gradle
    | TwitterCollect
    | | build.gradle
    | | src
    | IdolSentimentAnalysis
    | | build.gradle
    | | src
    | VerticaConnection
    | | build.gradle
    | | src
    | | db
    | | scripts
    | DataVisualization
    | | build.gradle
    | | src

[/code]

The file `common.gradle` includes information that is shared among all subprojects and it's referenced in the root `build.gradle`.

[code collapse="true" light="true" title="build.gradle" highlight="2"]

    subprojects {
        apply from: rootProject.file('common.gradle')
    }

[/code]

[code collapse="true" light="true" title="common.gradle" highlight="2"]

    apply plugin: 'java'
    apply plugin: 'application'
    
    repositories {
        mavenCentral();
        mavenLocal(); // needed for vertica jdbc
    }
    
    dependencies {
        testCompile group: 'junit', name: 'junit', version: '4.10'
    }

[/code]

The [application plugin](http://gradle.org/docs/current/userguide/application_plugin.html) adds the task `installApp` which build executables for our projects that are easier to run than calling `java` and setting the classpath and system properties manually or `gradle run`, which makes it hard to pass system properties or arguments.

Each subproject has its own `build.gradle` script and a `src` directory that follows the [Maven standard directory layout](http://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html).
<!-- }}} -->

# The end

This is a long tutorial and if you got this far, congratulations. I hope you find it useful. If you have any questions about it, feel free to leave a comment.

Remember you can download the source code to follow this tutorial from its [Bitbucket repository](https://bitbucket.org/lagunex/hpsentimentanalysis). The source code contains full CLI applications for `TwitterCollect` and `IdolSentimentAnalysis`.

