package com.lagunex.nlp;

import java.util.List;

public class SentimentResult {
    private List<Sentiment> positive;
    private List<Sentiment> negative;
    private Aggregate aggregate;

    public List<Sentiment> getPositive() {
        return positive;
    }

    public void setPositive(List<Sentiment> positive) {
        this.positive = positive;
    }

    public List<Sentiment> getNegative() {
        return negative;
    }

    public void setNegative(List<Sentiment> negative) {
        this.negative = negative;
    }

    public Aggregate getAggregate() {
        return aggregate;
    }

    public void setAggregate(Aggregate aggregate) {
        this.aggregate = aggregate;
    }
}
