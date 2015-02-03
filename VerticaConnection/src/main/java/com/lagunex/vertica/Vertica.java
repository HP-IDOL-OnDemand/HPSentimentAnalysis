/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.vertica;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 *
 * @author carloshq
 */
public class Vertica {
    private static final Logger LOGGER = Logger.getLogger(Vertica.class.getName());

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
        checkSystemProperties();
        DataSource dataSource = createDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource); 
    }

    private void checkSystemProperties() {
        String message = "property %s not defined";
        String missingProperty = null;
        if (System.getProperty(HOSTNAME) == null) {
            missingProperty = HOSTNAME;
        } else if (System.getProperty(DATABASE) == null) {
            missingProperty = DATABASE;
        } else if (System.getProperty(PASSWORD) == null) {
            missingProperty = PASSWORD;
        } else if (System.getProperty(USERNAME) == null) {
            missingProperty = USERNAME;
        }

        if (missingProperty != null) {
            throw new RuntimeException(String.format(message, missingProperty));
        }
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
        String query = 
            "select aggregate_sentiment as label, count(*) as total "
            + "from tweet "
            + "where created_at >= ? and created_at < ? "
            + "  and aggregate_sentiment is not null "
            + "group by label "
            + "order by label";
        
        return getResult(query, getTimestamp(start), getTimestamp(end));
    }

    private List<Map<String, Object>> getResult(String query, Object... args) {
        List<Map<String,Object>> result = new ArrayList<>();
        try {
            Instant begin = Instant.now();
            result = jdbcTemplate.queryForList(query, args);
            LOGGER.log(Level.INFO, "Query duration: {0}ms", Duration.between(begin, Instant.now()).toMillis());
        } catch (DataAccessException ex) {
            LOGGER.warning(ex.getMessage());
        }
        return result;
    }

    private Timestamp getTimestamp(LocalDateTime datetime) {
        Timestamp t = Timestamp.valueOf(datetime);
        return t;
    }

    public List<Map<String,Object>> getSentimentTotal(LocalDateTime start, LocalDateTime end) {
        String query =
            "select sentiment as label, count(*) as total "
            + "from tweet, sentiment "
            + "where tweet.id = sentiment.tweet_id "
            + "  and sentiment is not null "
            + "  and created_at >= ? and created_at < ? "
            + "group by label";

        return getResult(query, getTimestamp(start), getTimestamp(end));
    }

    public List<Map<String,Object>> getSentimentTotal(String sentiment, LocalDateTime start, LocalDateTime end) {
        String query =
            "select aggregate_sentiment as label, count(*) as total "
            + "from tweet, sentiment "
            + "where tweet.id = sentiment.tweet_id "
            + "  and sentiment = ? "
            + "  and created_at >= ? and created_at < ? "
            + "group by label";

        return getResult(query, sentiment, getTimestamp(start), getTimestamp(end));
    }

    public List<Map<String, Object>> getAggregateHistogram(LocalDateTime start, LocalDateTime end) {
        String histogramClass = getHistogramClass(start, end);

        String query = 
            "select "+histogramClass+" as time, avg(aggregate_score) as total "
            + "from tweet "
            + "where "+histogramClass+" >= ? and "+histogramClass+" < ? "
            + "group by time "
            + "order by time";
        return getResult(query, getTimestamp(start), getTimestamp(end));
    }
    
    private String getHistogramClass(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        String histogramClass;
        if (duration.toHours() < 1) {
            // one class per minute
            histogramClass = "TRUNC (created_at, 'mi')";
        } else {
            // one class every ten minutes
            histogramClass = "CONCAT(SUBSTRING(TO_CHAR(created_at, 'YYYY-MM-DD HH:MI'), 1, 15),'0')";
        }
        return histogramClass;
    }

    public List<Map<String, Object>> getTopicHistogram(LocalDateTime start, LocalDateTime end) {
        String histogramClass = getHistogramClass(start, end);

        String query = 
            "select topic as label, "+histogramClass+" as time, avg(aggregate_score) as total"
            + "from tweet, sentiment "
            + "where time >= ? and time < ? "
            + "  and tweet.id = sentiment.tweet_id "
            + "  and topic is not null "
            + "group by label, time";
        return getResult(query, getTimestamp(start), getTimestamp(end));
    }

    public Map<String, LocalDateTime> getDateRange() {
        String query =
            "select min(created_at) as begin, max(created_at) as end "
            + "from tweet";

        Map<String, Object> result = getResult(query).get(0);

        HashMap<String, LocalDateTime> dates = new HashMap<>();
        dates.put("begin", LocalDateTime.ofInstant(
                ((Date)result.get("begin")).toInstant(), ZoneOffset.UTC));
        dates.put("end", LocalDateTime.ofInstant(
                ((Date)result.get("end")).toInstant(), ZoneOffset.UTC));
        return dates;
    }
}
