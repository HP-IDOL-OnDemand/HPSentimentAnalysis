package com.lagunex.twitter;

import java.util.function.Consumer;
import java.util.logging.Logger;

// External dependencies that interacts with Twitter's REST API
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Singleton Service that encapsulates twitter4j.Twitter interface and provides
 * methods to search for tweets with optional pagination attributes.
 * 
 * This class assumes that the following system properties are defined:
 * 
 * oauth.consumerKey=CONSUMER_KEY
 * oauth.consumerSecret=CONSUMER_SECRET
 * enableApplicationOnlyAuth=true
 * http.useSSL=true
 * 
 * This properties can be passed at runtime or with a twitter4j.properties files accessible from the 
 * CLASSPATH. For further info see: http://twitter4j.org/en/configuration.html
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class TwitterClient {
    private static final Logger LOGGER = Logger.getLogger(TwitterClient.class.getName());
    
    private int maxResults = 100;
    final private Twitter twitter4j;

    private static TwitterClient instance;
    public static TwitterClient getInstance() {
        if (instance == null) {
            instance = new TwitterClient();
        }
        return instance;
    }
    
    private TwitterClient() {
        try {
            twitter4j = TwitterFactory.getSingleton(); // singleton configure with system properties
            twitter4j.getOAuth2Token(); // this line is mandatory for twitter4j to connect with Twitter
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Search Twitter for tweets that satisfy the given query and pass them
     * to consumer to execute the desired action.
     * 
     * The number of tweet to retrieve would always be lower or equal than this.getMaxResults()
     * 
     * @param query
     * @param consumer action to perform with each tweet found 
     */
    public void search(String query, Consumer<Tweet> consumer) {
        Query q = new Query(query);
        search(q, consumer);
    }
    
    /**
     * Search Twitter for tweets that satisfy the given query and pass them
     * to consumer to execute the desired action.
     * 
     * The tweets retrieved will have an id higher (newer tweets) than tweetId
     * The number of tweet to retrieve would always be lower or equal than this.getMaxResults()
     * 
     * @param query
     * @param tweetId
     * @param consumer action to perform with each tweet found  
     */
    public void searchSince(String query, long tweetId, Consumer<Tweet> consumer) {
		Query q = new Query(query);
		q.setSinceId(tweetId);
		search(q, consumer);
    }
    
    /**
     * Search Twitter for tweets that satisfy the given query and pass them
     * to consumer to execute the desired action.
     * 
     * The tweets retrieved will have an id lower or equal (older tweets) than tweetId
     * The number of tweet to retrieve would always be lower or equal than this.getMaxResults()
     * 
     * @param query
     * @param tweetId
     * @param consumer action to perform with each tweet found  
     */
    public void searchUntil(String query, long tweetId, Consumer<Tweet> consumer) {
		Query q = new Query(query);
		q.setMaxId(tweetId);
		search(q, consumer);
    }
    
    /**
     * Performs the search until this.getMaxResults() tweets are received or
     * Twitter doesn't return more results
     * 
     * @param q
     * @param consumer action to perform with each tweet received
     */
    private void search(Query q, Consumer<Tweet> consumer) {
	    int tweetsFound = 0;
        while (q != null && tweetsFound < this.getMaxResults()) {
            QueryResult qr = search(q);

            int maximumTweetsToRetrieve = this.getMaxResults()-tweetsFound; 
            tweetsFound += consumeResult(qr,consumer,maximumTweetsToRetrieve);
            
            q = qr.nextQuery();
			LOGGER.info(String.format("%d/%d tweets processed hasNext? %b", 
				tweetsFound, this.getMaxResults(), q != null));
        } 
    }

    /**
     * Encapsulates the twitter4j call for clarity 
     * @param q
     * @return results from the search 
     */
    private QueryResult search(Query q) {
        q.setCount(100); // get a maximum of 100 tweets per call (limited by Twitter's REST API)
        QueryResult qr = null;
        try {
            qr = twitter4j.search(q);
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
        return qr;
    }

    /**
     * 
     * @param qr results with the tweets found
     * @param consumer action to perform with each tweet found
     * @param maxTweets criteria to stop consuming tweets
     * @return 
     */
    private int consumeResult(QueryResult qr, Consumer<Tweet> consumer, int maxTweets) {
        int tweetsAdded = 0;
        for(Status s: qr.getTweets()) {
            consumer.accept(new Tweet(s));
            if (++tweetsAdded == maxTweets) break;
        }
        return tweetsAdded;
    }

    public int getMaxResults() {
        return this.maxResults;
    }
    
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}
