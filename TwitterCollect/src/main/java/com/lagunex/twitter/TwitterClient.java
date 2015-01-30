/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.twitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import java.util.Properties;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;

/**
 *
 * @author carloshq
 */
public class TwitterClient {
    private int maxResults = 100;
    final private Twitter twitter4j;
    
    public TwitterClient() {
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
        Query q = new Query(query);
        int tweetsFound = 0;
        while (q != null && tweetsFound < this.getMaxResults()) {
            QueryResult qr = search(q);
            result.addAll(extractResult(qr));
            q = qr.nextQuery();
            tweetsFound += qr.getCount();
        }
        return result;
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

    private Collection<? extends Tweet> extractResult(QueryResult qr) {
        ArrayList<Tweet> result = new ArrayList<>();
        Tweet t;
        for(Status s: qr.getTweets()) {
            t = new Tweet();
            t.setId(s.getId());
            t.setMessage(s.getText());
            result.add(t);
        }
        return result;
    }
}
