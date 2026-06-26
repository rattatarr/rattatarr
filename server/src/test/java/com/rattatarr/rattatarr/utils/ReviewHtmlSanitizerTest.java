package com.rattatarr.rattatarr.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReviewHtmlSanitizerTest {

    @Test
    void sanitize_nullOrBlank_returnsNull() {
        assertNull(ReviewHtmlSanitizer.sanitize(null));
        assertNull(ReviewHtmlSanitizer.sanitize(""));
        assertNull(ReviewHtmlSanitizer.sanitize("   "));
    }

    @Test
    void sanitize_keepsBasicFormatting() {
        String input = "<p>A <strong>great</strong> <em>film</em> with <u>style</u>.</p>";
        assertEquals(input, ReviewHtmlSanitizer.sanitize(input));
    }

    @Test
    void sanitize_keepsHeadingsAndLists() {
        String input = "<h1>Verdict</h1><ul><li>one</li><li>two</li></ul>";
        String out = ReviewHtmlSanitizer.sanitize(input);
        assertTrue(out.contains("<h1>"));
        assertTrue(out.contains("<li>one</li>"));
    }

    @Test
    void sanitize_stripsScriptTag() {
        String out = ReviewHtmlSanitizer.sanitize("<p>hi</p><script>alert('x')</script>");
        assertFalse(out.contains("<script"));
        assertFalse(out.contains("alert"));
        assertTrue(out.contains("<p>hi</p>"));
    }

    @Test
    void sanitize_stripsEventHandlersAndStyle() {
        String out = ReviewHtmlSanitizer.sanitize("<p onclick=\"steal()\" style=\"x\">hi</p>");
        assertFalse(out.contains("onclick"));
        assertFalse(out.contains("style"));
        assertTrue(out.contains("hi"));
    }

    @Test
    void sanitize_dropsJavascriptHref() {
        String out = ReviewHtmlSanitizer.sanitize("<a href=\"javascript:alert(1)\">x</a>");
        assertFalse(out.toLowerCase().contains("javascript"));
    }

    @Test
    void sanitize_keepsHttpLink() {
        String out = ReviewHtmlSanitizer.sanitize("<a href=\"https://example.com\">x</a>");
        assertTrue(out.contains("href=\"https://example.com\""));
    }

    @Test
    void sanitize_dropsDisallowedTagsKeepingText() {
        String out = ReviewHtmlSanitizer.sanitize("<div><img src=x onerror=alert(1)>text</div>");
        assertFalse(out.contains("<img"));
        assertFalse(out.contains("<div"));
        assertTrue(out.contains("text"));
    }

    @Test
    void sanitize_onlyMaliciousContent_returnsNull() {
        assertNull(ReviewHtmlSanitizer.sanitize("<script>alert(1)</script>"));
    }
}
