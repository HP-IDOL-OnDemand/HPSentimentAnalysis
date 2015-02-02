/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.nlp;

/**
 *
 * @author carloshq
 */
public class Aggregate {
    
    private static final char SEPARATOR = '|';
    
    private String sentiment;
    private double score;

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
