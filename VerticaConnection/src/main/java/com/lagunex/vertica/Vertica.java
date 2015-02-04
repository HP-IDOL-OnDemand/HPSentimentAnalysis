package com.lagunex.vertica;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

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
        StringBuilder query = new StringBuilder(100);
            query.append("select aggregate_sentiment as label, count(*) as total ")
                 .append("from tweet ")
                 .append("where created_at >= ? and created_at < ? ")
                 .append("  and aggregate_sentiment is not null ")
                 .append("group by label ")
                 .append("order by total desc ");
        
        return getResult(query.toString(), getTimestamp(start), getTimestamp(end));
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
        StringBuilder query = new StringBuilder(100);
            query.append("select sentiment as label, count(*) as total ")
                 .append("from tweet, sentiment ")
                 .append(getJoinWhereClause("created_at"))
                 .append("  and sentiment is not null ")
                 .append("group by label ")
                 .append("order by total desc ");

        List<Map<String,Object>> result = getResult(query.toString(), getTimestamp(start), getTimestamp(end));
        List<Map<String,Object>> filtered = groupSmallerValuesAsOthers(result);
        return filtered;
    }

    private StringBuilder getTimeClause(String column) {
        StringBuilder clause = new StringBuilder(50);
        clause.append(column).append(" >= ? and ").append(column).append(" < ? ");
        return clause;
    }

    private List<Map<String, Object>> groupSmallerValuesAsOthers(List<Map<String, Object>> data) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        
        int mostCommon = 15;
        int added = 0;
        
        long otherTotal = 0L;

        for(Map<String,Object> sample : data) {
            Long total = (Long)sample.get("total");
            if (added++ < mostCommon) {
                filtered.add(sample);
            } else {
                otherTotal += total;
            }
        }

        Map<String,Object> other = new HashMap<>();
        other.put("label", "others");
        other.put("total", otherTotal);
        filtered.add(other);
        return filtered; 
    }

    public List<Map<String, Object>> getAggregateHistogram(LocalDateTime start, LocalDateTime end) {
        String histogramClass = getHistogramClass(start, end);

        StringBuilder query = new StringBuilder(100);
        query.append("select ").append(histogramClass).append(" as time, avg(aggregate_score) as total ")
             .append("from tweet where ").append(getTimeClause(histogramClass))
             .append("group by time order by time");
        
        return getResult(query.toString(), getTimestamp(start), getTimestamp(end));
    }
    
    private String getHistogramClass(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return getHistogramClass(duration.toHours());
    }

    private String getHistogramClass(long hours) {
        String histogramClass;
        int timeStringLength;
        if (hours < 1) {
            timeStringLength = 16; // one class per minute
        } else {
            timeStringLength = 15; // one class every ten minutes
        }
        histogramClass = "SUBSTRING(TO_CHAR(created_at, 'YYYY-MM-DD HH:MI'), 1, "+timeStringLength+")";
        if (timeStringLength == 15) {
            histogramClass = "CONCAT("+histogramClass+",'0')"; // add '0' at the end e.g. 23:10
        }
        return histogramClass; 
    }

    public List<Map<String, Object>> getSentimentHistogram(LocalDateTime start, LocalDateTime end) {
        String histogramClass = getHistogramClass(start, end);
        StringBuilder where = getJoinWhereClause(histogramClass);
        StringBuilder query = new StringBuilder(200);
        query.append("select sentiment as label, ").append(histogramClass).append(" as time, ")
             .append("count(*) as total from tweet, sentiment ").append(where)
             .append("group by label, time having sentiment in (select sentiment from sentiment, tweet ")
             .append(where).append("group by sentiment order by count(*) desc limit 15) order by time");
        return getResult(query.toString(), 
                getTimestamp(start), getTimestamp(end),
                getTimestamp(start), getTimestamp(end));
    }

    private StringBuilder getJoinWhereClause(String timeColumn) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("where tweet.id = sentiment.tweet_id and ").append(getTimeClause(timeColumn));
        return sb;
    }

    public Map<String, LocalDateTime> getDateRange() {
        String query = "select min(created_at) as begin, max(created_at) as end from tweet";

        Map<String, Object> result = getResult(query).get(0);

        HashMap<String, LocalDateTime> dates = new HashMap<>();
        dates.put("begin", LocalDateTime.parse(result.get("begin").toString().replace(' ', 'T')));
        dates.put("end", LocalDateTime.parse(result.get("end").toString().replace(' ', 'T')));
        return dates;
    }

    public List<Map<String, Object>> getTweetsWithTime(String text) {
        long hours = text.endsWith("0") ? 1 : 0;
        String histogramClass = getHistogramClass(hours);
        StringBuilder query = new StringBuilder(100);
        query.append("select created_at as time, message from tweet where ")
             .append(histogramClass).append(" = ? order by time");
        return getResult(query.toString(), text);
    }

    public List<Map<String, Object>> getTweetsWithSentiment(String sentiment, LocalDateTime start, LocalDateTime end) {
        StringBuilder query = new StringBuilder(100);
        query.append("select created_at as time, message from tweet, sentiment ")
             .append(getJoinWhereClause("created_at"))
             .append(" and sentiment = ? order by time");
        return getResult(query.toString(), getTimestamp(start), getTimestamp(end), sentiment); 
    }

    public List<Map<String, Object>> getTweetsWithAggregate(String sentiment, LocalDateTime start, LocalDateTime end) {
        StringBuilder query = new StringBuilder(100);
        query.append("select created_at as time, message from tweet where ")
             .append(getTimeClause("created_at"))
             .append(" and aggregate_sentiment = ? order by time");
        return getResult(query.toString(), getTimestamp(start), getTimestamp(end), sentiment); 
    }
}
