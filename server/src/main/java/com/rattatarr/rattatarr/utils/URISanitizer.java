package com.rattatarr.rattatarr.utils;

public class URISanitizer {
    public static String removeTrailingSlash(String url) {
        if (url == null) {
            return null;
        }
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String pathEnsureLeadingSlash(String url) {
        if (url == null) {
            return null;
        }
        if (!url.startsWith("/")) {
            return "/" + url;
        }
        return url;
    }
}
