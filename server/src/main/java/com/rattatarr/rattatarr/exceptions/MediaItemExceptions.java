package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class MediaItemExceptions extends BaseRattatarrExceptions {
    private MediaItemExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class MediaItemNotFoundExceptions extends MediaItemExceptions {
        public MediaItemNotFoundExceptions(UUID id) {
            super(String.format("Media Item with id %s not found", id), HttpStatus.NOT_FOUND);
        }

        public MediaItemNotFoundExceptions(String name) {
            super(String.format("Media Item with name '%s' not found", name), HttpStatus.NOT_FOUND);
        }
    }
}
