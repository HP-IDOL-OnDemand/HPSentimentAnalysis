package com.lagunex.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class that manipulates Strings
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class StringUtils {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String SEPARATOR = "|";
    
    /**
     * Returns a string with all break lines replaced by the string "\n"
     * @param message
     * @return 
     */
    public static String collapseLines(String message) {
        return message.replaceAll("\n", "\\\\n");
    }

    /**
     * Replaces all occurrence of the string "\n" with a break line and returns the result
     * @param message
     * @return 
     */
    public static String uncollapseLines(String message) {
        return message.replaceAll("\\\\n", "\n");
    }
    
    /**
     * Returns a string with all occurrence of StringUtils.SEPARATOR escaped
     * @param message
     * @return 
     */
    public static String escape(String message) {
        return escape(message, SEPARATOR);
    }
    
    /**
     * Returns a string with all occurrence of reserved escaped
     * @param message
     * @param reserved
     * @return 
     */
    public static String escape(String message, String reserved) {
        String escaped = String.format("\\%s", reserved);
        return message.replace(reserved, escaped).replaceFirst("\\\\$", "\\\\ ");
    }

    /**
     * Returns a string with all escaped occurrence of StringUtils.SEPARATOR unescaped
     * @param escapedMessage
     * @return 
     */
    public static String unescape(String escapedMessage) {
        return unescape(escapedMessage, SEPARATOR);
    }
    
    /**
     * Returns a string with all escaped occurrence of reserved unescaped
     * @param escapedMessage
     * @param reserved
     * @return 
     */
    public static String unescape(String escapedMessage, String reserved) {
        String escaped = String.format("\\%s", reserved);
        return escapedMessage.replace(escaped, reserved).replaceFirst("\\\\ $", "\\\\"); 
    }

    /**
     * Returns a string representing dateTime with DATE_TIME_FORMATTER
     * @param dateTime
     * @return 
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
