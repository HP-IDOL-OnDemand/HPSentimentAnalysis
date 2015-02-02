/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author carloshq
 */
public class SentimentAnalysis {
    private static final String URL = "https://api.idolondemand.com/1/api/sync/analyzesentiment/v1";
    private static final Logger LOGGER = Logger.getLogger(SentimentAnalysis.class.getName());
    
    private static SentimentAnalysis instance;
    private final String API_KEY;
    
    private final List<Locale> LANGS = Arrays.asList(new Locale[]{
        Locale.ENGLISH, new Locale("es"), Locale.FRENCH, Locale.GERMAN,
        Locale.ITALIAN, Locale.CHINESE, new Locale("por"), new Locale("dut"),
        new Locale("rus"), new Locale("cze"), new Locale("tur")
    });

    public static SentimentAnalysis getInstance() {
        if (instance == null) {
            instance = new SentimentAnalysis();
        }
        return instance;
    }
    
    private SentimentAnalysis(){
        API_KEY = getApiKey();
    }

    private String getApiKey() {
        String apiKey = System.getProperty("idolOnDemand.apiKey");
        try {
            InputStream is = SentimentAnalysis.class.getResourceAsStream("/idol.properties");
            Properties p = new Properties();
            if (is != null) p.load(is);
            if (p.containsKey("idolOnDemand.apiKey")) {
                apiKey = p.getProperty("idolOnDemand.apiKey");
            }
        } catch (IOException e) {
        }
        if (apiKey == null) {
            throw new RuntimeException("property idolOnDemand.apiKey not defined");
        }
        return apiKey;
    }

    public SentimentResult analyse(String opinion) {
        return analyse(opinion, Locale.ENGLISH);
    }

    public SentimentResult analyse(String opinion, Locale locale) {
        opinion = encode(opinion);
        if (opinion != null && LANGS.contains(locale)) {
            return callRestApi(opinion, locale.getISO3Language());
        } else {
            return null;
        }
    }

    private String encode(String opinion) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(opinion, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.warning(ex.getMessage());
        } 
        return encoded;
    }

    private SentimentResult callRestApi(String opinion, String lang) {
        SentimentResult result = null;
        RestTemplate rest = new RestTemplate();
        try {
            result = rest.getForObject(
                URL+"?apikey="+this.API_KEY+"&text="+opinion+"&language="+lang,
                SentimentResult.class); 
        } catch (RestClientException ex) {
            LOGGER.warning(ex.getMessage());
        }
        return result;
    }
}
