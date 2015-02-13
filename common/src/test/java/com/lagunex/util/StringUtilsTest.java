/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.util;

import java.time.LocalDateTime;
import java.time.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class StringUtilsTest {
    
    @Test
    public void collapseLines() {
        String multiLines = "line1\nline2\nline3";
        String singleLine = "line1\\nline2\\nline3";
        assertEquals(singleLine, StringUtils.collapseLines(multiLines));
    } 

    @Test
    public void collapseLinesWithOneLineString() {
        String singleLine = "line1";
        assertEquals(singleLine, StringUtils.collapseLines(singleLine));
    }

    @Test
    public void uncollapseLines() {
        String multiLines = "line1\nline2\nline3";
        String singleLine = "line1\\nline2\\nline3";
        assertEquals(multiLines, StringUtils.uncollapseLines(singleLine));
    }

    @Test
    public void uncollapseLinesWithOneLineString() {
        String singleLine = "line1";
        assertEquals(singleLine, StringUtils.uncollapseLines(singleLine));
    }
    
    @Test
    public void escape() {
        String lineWithReservedChar = "line|with|reserved chars|\\";
        String escapedLine = "line\\|with\\|reserved chars\\|\\ ";
        assertEquals(escapedLine, StringUtils.escape(lineWithReservedChar));
    }
    
    @Test
    public void escapeWithCustomReservedChar() {
        String reservedChar = "&";
        String lineWithReservedChar = "line&with&reserved |chars&\\";
        String escapedLine = "line\\&with\\&reserved |chars\\&\\ ";
        assertEquals(escapedLine, StringUtils.escape(lineWithReservedChar, reservedChar));
    }
    
    @Test
    public void unescape() {
        String lineWithReservedChar = "line|with|reserved chars|\\";
        String escapedLine = "line\\|with\\|reserved chars\\|\\ ";
        assertEquals(lineWithReservedChar, StringUtils.unescape(escapedLine));
    }
    
    @Test
    public void unescapeWithCustomReservedChar() {
        String reservedChar = "&";
        String lineWithReservedChar = "line&with&reserved |chars&\\";
        String escapedLine = "line\\&with\\&reserved |chars\\&\\ ";
        assertEquals(lineWithReservedChar, StringUtils.unescape(escapedLine, reservedChar));
    }

    @Test
    public void formatDateTime() {
        LocalDateTime time = LocalDateTime.of(2015, Month.MARCH, 10, 13, 20);
        assertEquals("2015-03-10 13:20:00", StringUtils.formatDateTime(time));
        assertEquals("2015-03-10 13:20:15", StringUtils.formatDateTime(time.withSecond(15)));
    }
}
