package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

public class JellyfinConfigExceptions extends BaseRattatarrExceptions {
    private JellyfinConfigExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class JellyfinIsNotConfiguredException extends JellyfinConfigExceptions {
        public JellyfinIsNotConfiguredException(String baseUrl, String apiKey) {
            super(String.format("Jellyfin is not properly configured. Base URL: '%s', API Key: '%s'", baseUrl, apiKey), HttpStatus.BAD_REQUEST);
        }
    }
}
