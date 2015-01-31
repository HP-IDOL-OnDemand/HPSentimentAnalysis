/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.twitter;

import java.util.Date;

/**
 *
 * @author carloshq
 */
public class Tweet {
    private static final char RESERVED_CHAR = '|';
    private static final char RESERVED_CHAR_REPLACEMENT = ' ';
    
    private long id;
    private String message;
    private String jsonInfo;
    private Date createdAt;
    private String language;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getId()).append(RESERVED_CHAR)
          .append(getMessage()).append(RESERVED_CHAR)
          .append(getLanguage()).append(RESERVED_CHAR)
          .append(getCreatedAt()).append(RESERVED_CHAR)
          .append(jsonInfo);
        return sb.toString();
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message.replace(RESERVED_CHAR, RESERVED_CHAR_REPLACEMENT);
    }

    public String getJsonInfo() {
        return jsonInfo;
    }

    public void setJsonInfo(String jsonInfo) {
        this.jsonInfo = jsonInfo.replace(RESERVED_CHAR, RESERVED_CHAR_REPLACEMENT);
    }
    
    public String getLanguage() {
        return language;
    }
    
    void setLanguage(String lang) {
        this.language = lang;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
