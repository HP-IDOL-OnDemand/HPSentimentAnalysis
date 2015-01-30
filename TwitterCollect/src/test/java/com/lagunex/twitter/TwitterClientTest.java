/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.twitter;

import java.io.IOException;
import java.io.InputStream;
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
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void search() {
        TwitterClient client = new TwitterClient();
        
        List<Tweet> result = client.search("$HPQ");
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
}
