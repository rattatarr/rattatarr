package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

public class RadarrConfigExceptions extends BaseRattatarrExceptions {
    private RadarrConfigExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class RadarrIsNotConfiguredException extends RadarrConfigExceptions {
        public RadarrIsNotConfiguredException(String baseUrl, String apiKey) {
            super(String.format("Radarr is not properly configured. Base URL: '%s', API Key: '%s'", baseUrl, apiKey), HttpStatus.BAD_REQUEST);
        }
    }
}
