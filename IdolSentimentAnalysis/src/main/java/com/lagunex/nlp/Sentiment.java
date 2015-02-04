package com.lagunex.nlp;

public class Sentiment {
    private static final char SEPARATOR = '|';
    private String sentiment;
    private String topic;
    private double score;
    private String original_text;
    private String normalized_text;
    private int original_length;
    private int normalized_length; 

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

    public String getOriginal_text() {
        return original_text;
    }

    public void setOriginal_text(String original_text) {
        this.original_text = original_text;
    }

    public String getNormalized_text() {
        return normalized_text;
    }

    public void setNormalized_text(String normalized_text) {
        this.normalized_text = normalized_text;
    }

    public int getOriginal_length() {
        return original_length;
    }

    public void setOriginal_length(int original_length) {
        this.original_length = original_length;
    }

    public int getNormalized_length() {
        return normalized_length;
    }

    public void setNormalized_length(int normalized_length) {
        this.normalized_length = normalized_length;
    }
}
