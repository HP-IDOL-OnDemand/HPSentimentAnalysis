/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.twitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 *
 * @author carloshq
 */
public class TwitterClient {
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
    
    public List<Tweet> search(String query) {
        ArrayList<Tweet> result = new ArrayList<>();
        this.search(query, tweet -> result.add(tweet));
        return result;
    }
    
    public void search(String query, Consumer<Tweet> consumer) {
        Query q = new Query(query);
        int tweetsFound = 0;
        while (q != null && tweetsFound < this.getMaxResults()) {
            QueryResult qr = search(q);
            tweetsFound += consumeResult(qr,consumer,this.getMaxResults()-tweetsFound);
            q = qr.nextQuery();
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
        Tweet t;
        int tweetsAdded = 0;
        for(Status s: qr.getTweets()) {
            t = createTweet(s);
            consumer.accept(t);
            if (++tweetsAdded == maxTweets) break;
        }
        return tweetsAdded;
    }

    private Tweet createTweet(Status s) {
        Tweet t = new Tweet();
        t.setId(s.getId());
        t.setMessage(s.getText());
        t.setLanguage(s.getLang());
        t.setCreatedAt(s.getCreatedAt());
        t.setJsonInfo(TwitterObjectFactory.getRawJSON(s));
        return t;
    }
}
