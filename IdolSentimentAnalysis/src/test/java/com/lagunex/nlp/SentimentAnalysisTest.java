package com.lagunex.nlp;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class SentimentAnalysisTest {

    SentimentAnalysis engine;
    
    @Before
    public void setup() {
        engine = SentimentAnalysis.getInstance();
    }
    
    @Test
    public void analyse() {
        SentimentResult t = engine.analyse("This is a good day");
        assertNotNull(t);
        assertNotNull(t.getPositive());
        assertNotNull(t.getNegative());
        assertNotNull(t.getAggregate());
    }

    @Test
    public void analyseWithValidLanguage() {
        SentimentResult t = engine.analyse(
                "This is a good day", 
                SentimentAnalysis.Language.English);
        assertNotNull(t);
        assertNotNull(t.getPositive());
        assertNotNull(t.getNegative());
        assertNotNull(t.getAggregate());
    }

    @Test
    public void analyseWithNullLanguage() {
        SentimentResult t = engine.analyse("This is a good day", null);
        assertNull(t);
    }
}
