package com.lagunex.twitter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import twitter4j.Status;

/**
 * Class that represents a Tweet.
 * 
 * This class encapsulates the implementation from twitter4j.Status
 * It can be extended to capture more information.
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class Tweet {
    private final long id;
    private final String message;
    private final LocalDateTime createdAt;
    private final String language;

    public Tweet(Status s) {
	    id = s.getId();
        message = s.getText();
        language = s.getLang();
        createdAt = LocalDateTime.ofInstant(s.getCreatedAt().toInstant(),ZoneOffset.UTC);
    }
    
    public long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getLanguage() {
        return language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
