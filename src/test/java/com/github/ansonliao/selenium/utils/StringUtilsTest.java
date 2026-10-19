package com.github.ansonliao.selenium.utils;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class StringUtilsTest {

    @Test
    public void removeQuoteMarkStripsDoubleQuotes() {
        assertEquals(StringUtils.removeQuoteMark("\"hello\""), "hello");
    }

    @Test
    public void removeQuoteMarkStripsSingleQuotes() {
        assertEquals(StringUtils.removeQuoteMark("'hello'"), "hello");
    }

    @Test
    public void removeQuoteMarkLeavesUnquotedString() {
        assertEquals(StringUtils.removeQuoteMark("hello"), "hello");
    }

    @Test
    public void removeQuoteMarkReturnsNullForNull() {
        assertEquals(StringUtils.removeQuoteMark(null), null);
    }

    @Test
    public void removeQuoteMarkReturnsEmptyForEmpty() {
        assertEquals(StringUtils.removeQuoteMark(""), "");
    }

    @Test
    public void isNullOrEmptyDetectsNullAndEmpty() {
        assertTrue(StringUtils.isNullOrEmpty(null));
        assertTrue(StringUtils.isNullOrEmpty(""));
        assertFalse(StringUtils.isNullOrEmpty("x"));
    }

    @Test
    public void isNotNullAndNotEmptyInverts() {
        assertFalse(StringUtils.isNotNullAndNotEmpty(null));
        assertFalse(StringUtils.isNotNullAndNotEmpty(""));
        assertTrue(StringUtils.isNotNullAndNotEmpty("x"));
    }
}
