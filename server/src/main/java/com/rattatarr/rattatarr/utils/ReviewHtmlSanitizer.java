package com.rattatarr.rattatarr.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * Sanitizes user-submitted review HTML before persistence.
 *
 * <p>Review fields are rendered as HTML by the frontend, so raw user input is a stored-XSS
 * vector. Every review field is cleaned server-side against a fixed safelist that permits only
 * basic formatting tags and strips scripts, styles, event handlers, and anything else.
 */
public final class ReviewHtmlSanitizer {

    private static final Safelist SAFELIST = new Safelist()
            .addTags("b", "i", "em", "strong", "u", "h1", "h2", "h3", "p", "br", "ul", "ol", "li", "a", "blockquote")
            .addAttributes("a", "href")
            .addProtocols("a", "href", "http", "https", "mailto");

    private ReviewHtmlSanitizer() {}

    /**
     * Cleans the given HTML against the review safelist.
     *
     * @param html raw user input (may be null)
     * @return sanitized HTML, or null if the input is null/blank or sanitizes to nothing
     */
    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        // Keep relative href resolution off; we don't have a base URI for user content.
        String cleaned = Jsoup.clean(html, "", SAFELIST, new Document.OutputSettings().prettyPrint(false));
        return cleaned.isBlank() ? null : cleaned;
    }
}
