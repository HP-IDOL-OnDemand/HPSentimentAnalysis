package com.lagunex.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class SentimentAnalysisTest {
    @Test
    public void mandatoryPropertiesAreDefined() {
        Properties p = loadPropertiesFromSystemOrFile();
        assertNotNull(p.get("idolOnDemand.apiKey"));
    }

    private Properties loadPropertiesFromSystemOrFile() {
        Properties p = new Properties();
        InputStream is = getClass().getResourceAsStream("/idol.properties");
        if (is != null) {
            try {
                p.load(is);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        p.putAll(System.getProperties());
        return p;
    }
    
    @Test
    public void analyse() {
        SentimentAnalysis engine = SentimentAnalysis.getInstance(); 
        SentimentResult t = engine.analyse("This is a good day");
        assertNotNull(t);
        assertNotNull(t.getPositive());
        assertNotNull(t.getNegative());
        assertNotNull(t.getAggregate());
    }

    @Test
    public void analyseWithValidLanguage() {
        SentimentAnalysis engine = SentimentAnalysis.getInstance(); 
        SentimentResult t = engine.analyse(
                "Este es un buen día", 
                SentimentAnalysis.Language.Spanish);
        assertNotNull(t);
        assertNotNull(t.getPositive());
        assertNotNull(t.getNegative());
        assertNotNull(t.getAggregate());
    }

    @Test
    public void analyseWithNullLanguage() {
        SentimentAnalysis engine = SentimentAnalysis.getInstance(); 
        SentimentResult t = engine.analyse("This is a good day", null);
        assertNull(t);
    }
}
