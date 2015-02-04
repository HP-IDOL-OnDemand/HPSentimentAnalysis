package com.lagunex.nlp;

/**
 * Class that represents an Aggregate result of IdolOnDemand's analyzesentiment API
 * 
 * https://www.idolondemand.com/developer/apis/analyzesentiment#response
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class Aggregate {
    
    private static final char SEPARATOR = '|';
    
    private String sentiment;
    private double score;

    /**
     * 
     * @return "sentiment|score" 
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sentiment).append(SEPARATOR)
          .append(score);
        return sb.toString();
    }
    
    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
