package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class MediaSeasonExceptions extends BaseRattatarrExceptions {
    private MediaSeasonExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class MediaSeasonNotFoundExceptions extends MediaSeasonExceptions {
        public MediaSeasonNotFoundExceptions(UUID id) {
            super(String.format("Media Season with id %s not found", id), HttpStatus.NOT_FOUND);
        }

        public MediaSeasonNotFoundExceptions(String name) {
            super(String.format("Media Season with name '%s' not found", name), HttpStatus.NOT_FOUND);
        }
    }
}
