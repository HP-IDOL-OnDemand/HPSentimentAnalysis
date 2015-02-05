package com.lagunex.nlp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Class that represents an Entity result of IdolOnDemand's analyzesentiment API
 * 
 * https://www.idolondemand.com/developer/apis/analyzesentiment#response
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sentiment {
    private static final char SEPARATOR = '|';
    private String sentiment;
    private String topic;
    private double score;

    /**
     * 
     * @return "sentiment|topic|score"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sentiment).append(SEPARATOR)
          .append(topic).append(SEPARATOR)
          .append(score);
        return sb.toString();
    }
    
    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
