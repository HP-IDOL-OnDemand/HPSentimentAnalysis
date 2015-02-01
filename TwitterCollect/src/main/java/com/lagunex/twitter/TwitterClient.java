/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 *
 * @author carloshq
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
            twitter4j = TwitterFactory.getSingleton();
            twitter4j.getOAuth2Token();
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    
    public int getMaxResults() {
        return this.maxResults;
    }
    
    public void search(String query, Consumer<Tweet> consumer) {
        Query q = new Query(query);
        search(q, consumer);
    }
    
    public void searchSince(String query, long tweetId, Consumer<Tweet> consumer) {
		Query q = new Query(query);
		q.setSinceId(tweetId);
		search(q, consumer);
    }
    
    public void searchUntil(String query, long tweetId, Consumer<Tweet> consumer) {
		Query q = new Query(query);
		q.setMaxId(tweetId);
		search(q, consumer);
    }
    
    private void search(Query q, Consumer<Tweet> consumer) {
	    int tweetsFound = 0;
        while (q != null && tweetsFound < this.getMaxResults()) {
            QueryResult qr = search(q);
            tweetsFound += consumeResult(qr,consumer,this.getMaxResults()-tweetsFound);
            q = qr.nextQuery();
			LOGGER.info(String.format("%d/%d tweets processed hasNext? %b", 
				tweetsFound, this.getMaxResults(), q != null));
        } 
    }

    private QueryResult search(Query q) {
        QueryResult qr = null;
        try {
            qr = twitter4j.search(q);
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
        return qr;
    }

    private int consumeResult(QueryResult qr, Consumer<Tweet> consumer, int maxTweets) {
        int tweetsAdded = 0;
        for(Status s: qr.getTweets()) {
            consumer.accept(new Tweet(s));
            if (++tweetsAdded == maxTweets) break;
        }
        return tweetsAdded;
    }
}
