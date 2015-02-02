/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.vertica;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author carloshq
 */
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
    public void mandatoryProperties() {
        assertNotNull(System.getProperty("vertica.hostname"));
        assertNotNull(System.getProperty("vertica.database"));
        assertNotNull(System.getProperty("vertica.username"));
        assertNotNull(System.getProperty("vertica.password"));
    }

    @Test
    public void getDateRange() {
        List<Map<String,Object>> result = vertica.getDateRange();

        assertTrue(result.size()==1);
        
        assertNotNull(result.get(0).get("begin"));
        assertTrue(result.get(0).get("begin") instanceof java.sql.Timestamp);
        
        assertNotNull(result.get(0).get("end"));
        assertTrue(result.get(0).get("end") instanceof java.sql.Timestamp);
    } 

    @Test
    public void getAggregateTotal() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 0);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 8, 0);
        List<Map<String, Object>> result = vertica.getAggregateTotal(begin, end);

        assertEquals(3, result.size());
        String[] labels = {"negative", "neutral", "positive"};
        for(int i=0; i<result.size(); i++) {
            assertEquals(labels[i], result.get(i).get("label"));
        }
    }

    @Test
    public void getAggregateHistogramDuringOneHourOrMore() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 6, 30);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 7, 30);
        List<Map<String, Object>> result = vertica.getAggregateHistogram(begin, end);

        int oneSampleEvery10min = 6; 
        assertEquals(oneSampleEvery10min, result.size());
        result.stream().forEach(row -> {
            assertNotNull(row.get("time"));
            assertNotNull(row.get("total"));
        });
    }

    @Test
    public void getAggregateHistogramDuringLessThanOneHour() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 7, 0);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 7, 50);
        List<Map<String, Object>> result = vertica.getAggregateHistogram(begin, end);

        int oneSampleEveryMinute = 50;
        assertEquals(oneSampleEveryMinute, result.size());
        result.stream().forEach(row -> {
            assertNotNull(row.get("time"));
            assertNotNull(row.get("total"));
        });
    } 
}