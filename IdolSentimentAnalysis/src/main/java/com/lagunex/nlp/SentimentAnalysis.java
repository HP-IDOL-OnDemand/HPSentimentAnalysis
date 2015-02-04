package com.lagunex.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class SentimentAnalysis {
    private static final String URL = "https://api.idolondemand.com/1/api/sync/analyzesentiment/v1";
    private static final Logger LOGGER = Logger.getLogger(SentimentAnalysis.class.getName());
    
    private static SentimentAnalysis instance;
    private final String API_KEY;
    
    public enum Language {
        English("en", "eng"),
        French("fr", "fre"),
        Spanish("es", "spa"),
        German("de", "ger"),
        Italian("it", "ita"),
        Chinese("zh", "chi"),
        Portuguese("pt", "por"),
        Dutch("nl", "dut"),
        Russian("ru", "rus"),
        Czech("cs", "cze"),
        Turkish("tr", "tur");

        private final String code, longCode;
        Language(String code, String longCode) {
            this.code = code;
            this.longCode = longCode;
        }

        static Language getLanguage(String code) {
            Language lang = null;
            for(Language candidate: Language.values()) {
                if (candidate.code.equals(code)) {
                    lang = candidate;
                    break;
                }
            }
            return lang;
        }
    }

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
        return analyse(opinion, Language.English);
    }

    public SentimentResult analyse(String opinion, Language lang) {
        opinion = encode(opinion);
        if (opinion != null && lang != null) {
            return callRestApi(opinion, lang.longCode);
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
