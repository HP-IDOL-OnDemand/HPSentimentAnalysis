package com.lagunex.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TwitterClientTest {
    TwitterClient client;
    
    @Before
    public void setUp() throws IOException {
        client = TwitterClient.getInstance();
    }
    
    @Test
    public void propertyFileExists() {
        loadProperties();
    }
    
    private Properties loadProperties() {
        Properties p = new Properties();
        InputStream is = TwitterClientTest.class.getResourceAsStream("/twitter4j.properties");
        if (is != null) {
            try {
                p.load(is);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return p;
    }
    
    @Test
    public void mandatoryPropertiesAreDefined() {
        Properties file = loadProperties();
        Properties system = System.getProperties();
        
        assertTrue(file.containsKey("oauth.consumerKey") || system.containsKey("twitter4j.oauth.consumerKey"));
        assertTrue(file.containsKey("oauth.consumerSecret") || system.containsKey("twitter4j.oauth.consumerSecret"));
        assertTrue(file.containsKey("http.useSSL") || system.containsKey("twitter4j.http.useSSL"));
        assertTrue(file.containsKey("enableApplicationOnlyAuth") ||
                system.containsKey("twitter4j.enableApplicationOnlyAuth")
        );

        Object prop = file.containsKey("enableApplicationOnlyAuth") ?
                file.get("enableApplicationOnlyAuth") : system.get("twitter4j.enableApplicationOnlyAuth");
        assertEquals("true",prop);

        prop = file.containsKey("http.useSSL") ?
                file.get("http.useSSL").toString() : system.get("twitter4j.http.useSSL").toString(); 
        assertEquals("true",prop);
    }
    
    @Test
    public void search() {
        String sampleQuery = "$HPQ -filter:retweets"; // https://dev.twitter.com/rest/public/search
        List<Tweet> result = new ArrayList<>();
        client.search(sampleQuery, tweet -> result.add(tweet));
        assertTrue(result.size() <= client.getMaxResults());
    }

    @Test
    public void searchSince() {
        long tweetId = 558655785975025664L;
        List<Tweet> result = new ArrayList<>();
        client.searchSince("$HPQ", tweetId, tweet -> result.add(tweet));
        result.stream().forEach(t -> {
            assertTrue(tweetId < t.getId());
        });
    }
    
    @Test
    public void searchUntil() {
        long tweetId = 561472063341600768L;
        List<Tweet> result = new ArrayList<>();
        client.searchUntil("$HPQ", tweetId, tweet -> result.add(tweet));
        result.stream().forEach(t -> {
            assertTrue(tweetId >= t.getId());
        });
    }
}
