package com.lagunex.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class StringUtils {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String SEPARATOR = "|";
    
    public static String collapseLines(String message) {
        return message.replaceAll("\n", "\\\\n");
    }

    public static String uncollapseLines(String message) {
        return message.replaceAll("\\\\n", "\n");
    }
    
    public static String escape(String message) {
        return escape(message, SEPARATOR);
    }
    
    public static String escape(String message, String reserved) {
        String escaped = String.format("\\%s", reserved);
        return message.replace(reserved, escaped).replaceFirst("\\\\$", "\\\\ ");
    }

    public static String unescape(String escapedMessage) {
        return unescape(escapedMessage, SEPARATOR);
    }
    
    public static String unescape(String escapedMessage, String reserved) {
        String escaped = String.format("\\%s", reserved);
        return escapedMessage.replace(escaped, reserved).replaceFirst("\\\\ $", "\\\\"); 
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
