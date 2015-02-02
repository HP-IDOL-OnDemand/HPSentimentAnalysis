/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.nlp;

import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author carloshq
 */
public class SentimentAnalysisTest {

    SentimentAnalysis engine;
    
    @Test
    public void analyse() {
        SentimentResult t = engine.analyse("This is a good day");
        assertNotNull(t);
        assertNotNull(t.getPositive());
        assertNotNull(t.getNegative());
        assertNotNull(t.getAggregate());
    }

    @Test
    public void analyseWithValidLocale() {
        SentimentResult t = engine.analyse("This is a good day", Locale.ENGLISH);
        assertNotNull(t);
        assertNotNull(t.getPositive());
        assertNotNull(t.getNegative());
        assertNotNull(t.getAggregate());
    }

    @Test
    public void analyseWithInvalidLocale() {
        SentimentResult t = engine.analyse("This is a good day", Locale.JAPANESE);
        assertNull(t);
    }
}
