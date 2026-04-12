package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

public class TMDbConfigExceptions extends BaseRattatarrExceptions {
    private TMDbConfigExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class TMDbIsNotConfiguredException extends TMDbConfigExceptions {
        public TMDbIsNotConfiguredException(String baseUrl, String apiKey) {
            super(String.format("TMDb is not properly configured. Base URL: '%s', API Key: '%s'", baseUrl, apiKey), HttpStatus.BAD_REQUEST);
        }

        public TMDbIsNotConfiguredException(String baseUrl, String apiKey, String imageBaseUrl) {
            super(String.format("TMDb is not properly configured. Base URL: '%s', API Key: '%s', Image Base URL: '%s'", baseUrl, apiKey, imageBaseUrl), HttpStatus.BAD_REQUEST);
        }
    }
}
