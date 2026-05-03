package com.rattatarr.rattatarr.exceptions;

import com.rattatarr.rattatarr.models.ArrInstance;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class RadarrConfigExceptions extends BaseRattatarrExceptions {
    private RadarrConfigExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class RadarrIsNotConfiguredException extends RadarrConfigExceptions {
        public RadarrIsNotConfiguredException(ArrInstance instance, @Nullable String baseUrl, @Nullable String apiKey) {
            super(String.format("Radarr (%s) is not properly configured. Base URL: '%s', API Key: '%s'", instance, baseUrl, apiKey), HttpStatus.BAD_REQUEST);
        }
    }
}
