package com.rattatarr.rattatarr.exceptions;

import com.rattatarr.rattatarr.models.ArrInstance;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class SonarrConfigExceptions extends BaseRattatarrExceptions {
    private SonarrConfigExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class SonarrIsNotConfiguredException extends SonarrConfigExceptions {
        public SonarrIsNotConfiguredException(ArrInstance instance, @Nullable String baseUrl, @Nullable String apiKey) {
            super(String.format("Sonarr (%s) is not properly configured. Base URL: '%s', API Key: '%s'", instance, baseUrl, apiKey), HttpStatus.BAD_REQUEST);
        }
    }
}
