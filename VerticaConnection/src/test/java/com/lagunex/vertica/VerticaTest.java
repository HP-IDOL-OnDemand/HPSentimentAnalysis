package com.lagunex.vertica;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

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
        Map<String,LocalDateTime> result = vertica.getDateRange();

        assertNotNull(result.get("begin"));
        assertNotNull(result.get("end"));
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

    @Test
    public void getAggregateHistogramDuringOneHourOrMore() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 00);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 2, 00);
        List<Map<String, Object>> result = vertica.getAggregateHistogram(begin, end);

        result.stream().forEach(row -> {
            assertNotNull(row.get("time"));
            assertNotNull(row.get("total"));
        });
    }

    @Test
    public void getAggregateHistogramDuringLessThanOneHour() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 0);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 50);
        List<Map<String, Object>> result = vertica.getAggregateHistogram(begin, end);

        result.stream().forEach(row -> {
            assertNotNull(row.get("time"));
            assertNotNull(row.get("total"));
        });
    } 

    @Test
    public void getSentimentHistogramDuringOneHourOrMore() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 00);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 2, 00);
        List<Map<String, Object>> result = vertica.getSentimentHistogram(begin, end);

        result.stream().forEach(row -> {
            assertNotNull(row.get("time"));
            assertNotNull(row.get("total"));
        });
    }

    @Test
    public void getSentimentHistogramDuringLessThanOneHour() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 0);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 50);
        List<Map<String, Object>> result = vertica.getSentimentHistogram(begin, end);

        result.stream().forEach(row -> {
            assertNotNull(row.get("time"));
            assertNotNull(row.get("total"));
        });
    } 

    @Test
    public void getSentimentTotal() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 0);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 8, 0);
        List<Map<String, Object>> result = vertica.getSentimentTotal(begin, end);

        assertEquals(16, result.size());
        result.stream().forEach(row -> {
            assertNotNull(row.get("label"));
            assertNotNull(row.get("total"));
        });
    }

    @Test
    public void getTweetsWithAggregate() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 0);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 8, 0);

        for (String aggregate : new String[]{"neutral", "positive", "negative"}) {
            List<Map<String, Object>> result = vertica.getTweetsWithAggregate(aggregate, begin, end); 
            assertTrue(result.size()>0);
            result.stream().forEach(row -> {
                    assertNotNull(row.get("time"));
                    assertNotNull(row.get("message")); 
            });
        }
    }
    
    @Test
    public void getTweetsWithSentiment() {
        LocalDateTime begin = LocalDateTime.of(2015, Month.FEBRUARY, 2, 1, 0);
        LocalDateTime end = LocalDateTime.of(2015, Month.FEBRUARY, 2, 8, 0);

        String sampleSentiment = "Congrats";
        List<Map<String, Object>> result = vertica.getTweetsWithSentiment(sampleSentiment, begin, end); 
        assertTrue(result.size()>0);
        result.stream().forEach(row -> {
            assertNotNull(row.get("time"));
            assertNotNull(row.get("message")); 
        });
    }

    @Test
    public void getTweetsWithTimeInATenMinuteWindow() {
        String window = "2015-02-02 03:00";
        
        List<Map<String, Object>> result = vertica.getTweetsWithTime(window); 
        assertTrue(result.size()>0);
        result.stream().forEach(row -> {
            String time = row.get("time").toString();
            assertNotNull(time);
            assertTrue(window.compareTo(time)<=0 && "2015-02-02 03:10".compareTo(time)>0);
            assertNotNull(row.get("message")); 
        });
    }

    @Test
    public void getTweetsWithTimeInAOneMinuteWindow() {
        String window = "2015-02-02 01:41";
        
        List<Map<String, Object>> result = vertica.getTweetsWithTime(window); 
        assertTrue(result.size()>0);
        result.stream().forEach(row -> {
            String time = row.get("time").toString();
            assertNotNull(time);
            assertTrue(window.compareTo(time)<=0 && "2015-02-02 01:42".compareTo(time)>0);
            assertNotNull(row.get("message")); 
        });
    }

    @Test
    public void getTweetsWithTimeWithInvalidParameter() {
        String window = "invalid datetime string";
        assertEquals(0,vertica.getTweetsWithTime(window).size()); 
    }
}