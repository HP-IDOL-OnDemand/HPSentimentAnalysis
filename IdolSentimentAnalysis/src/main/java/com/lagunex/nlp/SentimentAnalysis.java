package com.lagunex.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Logger;

// external dependencies used to call IdolOnDemand's REST API
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Singleton that performs the Sentiment Analysis using IdolOnDemand's API
 * 
 * This class assumes that the following system property is defined:
 * 
 * idolOnDemand.apiKey
 * 
 * This property can be passed at runtime using Java's -D option or through a
 * configuration file accessible from the CLASSPATH called idol.properties
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class SentimentAnalysis {
    private static final String URL = "https://api.idolondemand.com/1/api/sync/analyzesentiment/v1";
    private static final Logger LOGGER = Logger.getLogger(SentimentAnalysis.class.getName());
    
    private static SentimentAnalysis instance;
    private final String API_KEY;
    
    /**
     * Languages valid to analyse
     */
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

        /**
         * 
         * @param code Two-letters ISO 639-1
         * @param longCode Three-letters ISO 639-2/B used internally by the API
         */
        Language(String code, String longCode) {
            this.code = code;
            this.longCode = longCode;
        }

        /**
         * Given a Two-letters language code, return the valid enum or null
         * if not found
         * 
         * @param code Two-letters ISO 639-1
         * @return 
         */
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

    /**
     * Retrieves the API Key from system properties or from a configuration file
     * @return 
     */
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

    /**
     * Perform an analyses using English as the default language
     * 
     * @param opinion
     * @return result or null if opinion could not be analyse 
     */
    public SentimentResult analyse(String opinion) {
        return analyse(opinion, Language.English);
    }

    /**
     * 
     * @param opinion
     * @param lang
     * 
     * @return result or null if opinion could not be analyse with the given language 
     */
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

    /**
     * Call the external API and binds the response into a SentimentResult object.
     * 
     * The binding is performed automatically by the third-party library jackson-databind.
     * In order to do so, the class SentimentResult must match the JSON response format returned by the API.
     * 
     * SentimentResult follows the schema defined in
     * https://www.idolondemand.com/developer/apis/analyzesentiment#response
     * 
     * @param opinion
     * @param lang
     * @return result from the call or null if a problem occurs
     */
    private SentimentResult callRestApi(String opinion, String lang) {
        SentimentResult result = null;
        RestTemplate rest = new RestTemplate();
        try {
            // calls the API and parse the JSON response into a Java object
            result = rest.getForObject(
                URL+"?apikey="+this.API_KEY+"&text="+opinion+"&language="+lang,
                SentimentResult.class); 
        } catch (RestClientException ex) {
            LOGGER.warning(ex.getMessage());
        }
        return result;
    }
}
