/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.twitter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import twitter4j.Status;

/**
 *
 * @author carloshq
 */
public class Tweet {
    private static final char RESERVED_CHAR = '|';
    private static final char RESERVED_CHAR_REPLACEMENT = ' ';
    
    private final long id;
    private final String message;
    private final LocalDateTime createdAt;
    private final String language;

    public Tweet(Status s) {
	    id = s.getId();
        message = s.getText().replace(RESERVED_CHAR, RESERVED_CHAR_REPLACEMENT);
        language = s.getLang();
        createdAt = LocalDateTime.ofInstant(s.getCreatedAt().toInstant(),ZoneOffset.UTC);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getId()).append(RESERVED_CHAR)
          .append(getMessage()).append(RESERVED_CHAR)
          .append(getLanguage()).append(RESERVED_CHAR)
          .append(getCreatedAt());
        return sb.toString().replace('\n', ' ');
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
