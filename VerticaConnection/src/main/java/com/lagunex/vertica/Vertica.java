package com.lagunex.vertica;

import com.lagunex.util.StringUtils;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// external dependency to connect with Vertica
import javax.sql.DataSource;

// high level external dependencies to facilitate the execution of queries
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Singleton that connects with the Vertica database and performs query to retrieve analytics
 * or inserts new records in the database
 * 
 * This class assumes that the following system properties are defined:
 * 
 * vertica.hostname
 * vertica.database
 * vertica.username
 * vertica.password
 * 
 * This property must be passed at runtime using Java's -D option 
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
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

    /**
     * 
     * @param tblRecord line with the info to insert. Each column separated by separator
     * @param separator String that represents the column separator in tblRecord
     * @return number of rows inserted
     */
    public int insertTweetRecord(String tblRecord, String separator) {
        String query = "insert into tweet values (?,?,?,?,?,?)";

        // we should split by separator only when it is not escaped
        Object[] args = tblRecord.split(String.format("(?<!\\\\)\\%s", separator));
        
        if (args.length != 6 && args.length != 4) { // invalid format
            LOGGER.log(Level.WARNING, "Invalid line: {0}", tblRecord);
            return 0;
        }
      
        // Restore the original message with unescaped characters and line breaks
        String unescapedMessage = StringUtils.unescape(args[1].toString(), separator);
        args[1] = StringUtils.uncollapseLines(unescapedMessage);
        if (args.length == 4) { // does not include aggregate data
            query = "insert into tweet (id, message, lang, created_at) values (?,?,?,?)";
        }
        
        return insertRecord(query, args);
    }

    /**
     * Insert a record following the insert query and returns the numbers of rows inserted
     * 
     * @param insert query to build the statement
     * @param args parameters to pass to the query to execute the insert, the must have a SQL equivalent
     * @return number of records inserted
     */
    private int insertRecord(String insert, Object... args) {
        int result = 0;
        try {
            result = jdbcTemplate.update(insert, args);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.WARNING, "{0} {1}", new Object[]{ex.getMessage(), Arrays.toString(args)});
        }
        return result;
    }
    
    /**
     * 
     * @param tblRecord line with the info to insert. Each column separated by |
     * @return number of rows inserted
     */
    public int insertTweetRecord(String tblRecord) {
        return insertTweetRecord(tblRecord, StringUtils.SEPARATOR);
    }
    
    /**
     * Insert a record into the sentiment table
     * @param tblRecord line with the info to insert. Each column separated by separator
     * @param separator string that represents the column separator in tblRecord
     * @param nullValue string that represents the null value in tblRecord
     * @return number of rows inserted
     */
    public int insertSentimentRecord(String tblRecord, String separator, String nullValue) {
        String query = "insert into sentiment (tweet_id, sentiment, topic, score) values (?,?,?,?)";

        // we should split by separator only when it is not escaped
        Object[] args = tblRecord.split(String.format("(?<!\\\\)\\%s", separator));
        args[1] = nullValue.equals(args[1]) ? null : args[1];
        args[2] = nullValue.equals(args[2]) ? null : args[2];
        
        return insertRecord(query, args);
    }

    /**
     * Insert a record in the sentiment table, columns are split by "|" and "null" columns
     * are added as NULL
     * 
     * @param tblRecord line with the info to insert. Each column separated by separator
     * @return number of rows inserted
     */
    public int insertSentimentRecord(String tblRecord) {
        String nullValue = null;
        return insertSentimentRecord(tblRecord, StringUtils.SEPARATOR, String.valueOf(nullValue));
    }
    
    /**
     * Return the aggregate sentiment (negative, neutral or positive) of tweets sent during the date range 
     * @param start inclusive
     * @param end exclusive
     * @return each Map in List has the form {"label" : string, "total" : integer} 
     */
    public List<Map<String,Object>> getAggregateTotal(LocalDateTime start, LocalDateTime end) {
        StringBuilder query = new StringBuilder(100);
            query.append("select aggregate_sentiment as label, count(*) as total ")
                 .append("from tweet ")
                 .append("where created_at >= ? and created_at < ? ")
                 .append("  and aggregate_sentiment is not null ")
                 .append("group by label ")
                 .append("order by total desc ");
        
        return getResult(query.toString(), Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    /**
     * Performs the database query, logs the time it took and returns a List of Maps with the results
     * 
     * The binding between the query result and the List<Map<>> object returned is performed automatically
     * by jdbcTemplate
     * 
     * @param query sql query to build the statement
     * @param args parameters to pass to the query to execute the search, the must have a SQL equivalent
     * @return the Map's keys and values vary depending on the query
     */
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

    /**
     * Return the topic of tweets sent during the date range 
     * @param start inclusive
     * @param end exclusive
     * @return each Map in List has the form {"label" : string, "total" : integer} 
     */
    public List<Map<String,Object>> getTopicTotal(LocalDateTime start, LocalDateTime end) {
        StringBuilder query = new StringBuilder(100);
            query.append("select topic as label, count(*) as total ")
                 .append("from tweet, sentiment ")
                 .append(getJoinWhereClause("created_at"))
                 .append("  and topic is not null ")
                 .append("group by label ")
                 .append("order by total desc ");

        List<Map<String,Object>> result = getResult(query.toString(), Timestamp.valueOf(start), Timestamp.valueOf(end));
        List<Map<String,Object>> filtered = groupSmallerValuesAsOthers(result);
        return filtered;
    }

    private StringBuilder getTimeClause(String column) {
        StringBuilder clause = new StringBuilder(50);
        clause.append(column).append(" >= ? and ").append(column).append(" < ? ");
        return clause;
    }

    /**
     * Create a list with the first 15 elements of data and group all others (if any) in a single
     * sample called "others"
     * 
     * @param data list of elements with {"label": string, "total": integer}
     * @return data[0,15]+[{"label": "others", "total": integer}] 
     */
    private List<Map<String, Object>> groupSmallerValuesAsOthers(List<Map<String, Object>> data) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        
        int mostCommon = 50;
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

    /**
     * Return a histogram of tweets that fall in the given time range.
     * 
     * If the time range is less than one hour, the histogram groups the tweets in windows of one minute;
     * otherwise it groups them in windows of ten minutes
     * 
     * @param start inclusive
     * @param end exclusive
     * @return each Map in List has the form {"time": datetime, "total" : integer}  
     */
    public List<Map<String, Object>> getAggregateHistogram(LocalDateTime start, LocalDateTime end) {
        String histogramClass = getHistogramClass(start, end);

        StringBuilder query = new StringBuilder(100);
        query.append("select ").append(histogramClass).append(" as time, avg(aggregate_score) as total ")
             .append("from tweet where ").append(getTimeClause(histogramClass))
             .append("group by time order by time");
        
        return getResult(query.toString(), Timestamp.valueOf(start), Timestamp.valueOf(end));
    }
    
    private String getHistogramClass(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return getHistogramClass(duration.toHours());
    }

    /**
     * Creates a sql function to round a time according to a duration in hours.
     * 
     * @param hours if less than one, the time is rounded to the minute; otherwise to the ten
     * @return a function to round a time either like 'YYYY-MM-DD HH:MM' or 'YYYY-MM-DD HH:M0' 
     */
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

    /**
     * Return a histogram of topics that fall in the given time range.
     * 
     * The histogram count is divided in topics.
     * If the time range is less than one hour, the histogram groups the tweets within a topic
     * in windows of one minute; otherwise it groups them in windows of ten minutes.
     * 
     * @param start inclusive
     * @param end exclusive
     * @return each Map in List has the form {"label": string, "time": datetime, "total" : integer}  
     */
    public List<Map<String, Object>> getTopicHistogram(LocalDateTime start, LocalDateTime end) {
        String histogramClass = getHistogramClass(start, end);
        StringBuilder where = getJoinWhereClause(histogramClass);
        StringBuilder query = new StringBuilder(200);
        query.append("select topic as label, ").append(histogramClass).append(" as time, ")
             .append("count(*) as total from tweet, sentiment ").append(where)
             .append("group by label, time having topic in (select topic from sentiment, tweet ")
             .append(where).append("group by topic order by count(*) desc limit 15) order by time");
        return getResult(query.toString(), 
                Timestamp.valueOf(start), Timestamp.valueOf(end),
                Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    private StringBuilder getJoinWhereClause(String timeColumn) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("where tweet.id = sentiment.tweet_id and ").append(getTimeClause(timeColumn));
        return sb;
    }

    /**
     * Returns the minimum and maximum datetime found among all the tweets
     * @return {"begin" : datetime, "end": datetime }
     */
    public Map<String, LocalDateTime> getDateRange() {
        String query = "select min(created_at) as begin, max(created_at) as end from tweet";

        Map<String, Object> result = getResult(query).get(0);

        HashMap<String, LocalDateTime> dates = new HashMap<>();
        dates.put("begin", LocalDateTime.parse(result.get("begin").toString().replace(' ', 'T')));
        dates.put("end", LocalDateTime.parse(result.get("end").toString().replace(' ', 'T')));
        return dates;
    }

    /**
     * Returns the tweet's message and time for the given time window.
     * 
     * @param time it should have the format 'yyyy-mm-dd hh:mm'
     *             If 'mm' is multiple of ten, it is a 10 minutes window; otherwise it is a one minute window
     * @return Map with format { "time": datatime, "message": string } 
     */
    public List<Map<String, Object>> getTweetsWithTime(String time) {
        long hours = time.endsWith("0") ? 1 : 0;
        String histogramClass = getHistogramClass(hours);
        StringBuilder query = new StringBuilder(100);
        query.append("select created_at as time, message from tweet where ")
             .append(histogramClass).append(" = ? order by time");
        return getResult(query.toString(), time);
    }
    
    /**
     * Returns the tweet's message and time for the given topic in the given time range
     * 
     * @param sentiment 
     * @param start inclusive
     * @param end exclusive
     * @return Map with format { "time": datatime, "message": string } 
     */
    public List<Map<String, Object>> getTweetsWithTopic(String sentiment, LocalDateTime start, LocalDateTime end) {
        StringBuilder query = new StringBuilder(100);
        query.append("select created_at as time, message from tweet, sentiment ")
             .append(getJoinWhereClause("created_at"))
             .append(" and topic = ? order by time");
        return getResult(query.toString(), Timestamp.valueOf(start), Timestamp.valueOf(end), sentiment); 
    }
    
    /**
     * Returns the tweet's message and time for the given aggregate sentiment in the given time range
     * 
     * @param sentiment 
     * @param start inclusive
     * @param end exclusive
     * @return Map with format { "time": datatime, "message": string } 
     */
    public List<Map<String, Object>> getTweetsWithAggregate(String sentiment, LocalDateTime start, LocalDateTime end) {
        StringBuilder query = new StringBuilder(100);
        query.append("select created_at as time, message from tweet where ")
             .append(getTimeClause("created_at"))
             .append(" and aggregate_sentiment = ? order by time");
        return getResult(query.toString(), Timestamp.valueOf(start), Timestamp.valueOf(end), sentiment); 
    }
}
