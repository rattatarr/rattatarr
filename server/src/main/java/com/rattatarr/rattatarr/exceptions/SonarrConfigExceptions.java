package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

public class SonarrConfigExceptions extends BaseRattatarrExceptions {
    private SonarrConfigExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class SonarrIsNotConfiguredException extends SonarrConfigExceptions {
        public SonarrIsNotConfiguredException(String baseUrl, String apiKey) {
            super(String.format("Sonarr is not properly configured. Base URL: '%s', API Key: '%s'", baseUrl, apiKey), HttpStatus.BAD_REQUEST);
        }
    }
}
