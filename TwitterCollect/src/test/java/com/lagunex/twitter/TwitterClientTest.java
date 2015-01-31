/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author carloshq
 */
public class TwitterClientTest {
    TwitterClient client;
    
    public TwitterClientTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        client = TwitterClient.getInstance();
    }
    
    @After
    public void tearDown() {
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
        Properties p = loadProperties();
        
        assertNotNull(p.get("oauth.consumerKey"));
        assertNotNull(p.get("oauth.consumerSecret"));
        
        assertEquals("true",p.get("enableApplicationOnlyAuth"));
        assertEquals("true",p.get("http.useSSL"));
    }
    
    @Test
    public void search() {
        List<Tweet> result = new ArrayList<>();
        client.search("$HPQ", tweet -> result.add(tweet));
        assertTrue(result.size() <= client.getMaxResults());
    }

    @Test
    public void searchSince() {
        long tweetId = 558655785975025664L;
        List<Tweet> result = new ArrayList<>();
        client.searchSince("$HPQ", tweetId, tweet -> result.add(tweet));
        result.stream().forEach(t -> {
            assertTrue(tweetId <= t.getId());
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
