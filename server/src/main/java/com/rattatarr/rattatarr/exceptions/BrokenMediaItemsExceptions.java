package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class BrokenMediaItemsExceptions extends BaseRattatarrExceptions {
    private BrokenMediaItemsExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class BrokenMediaItemNotFoundExceptions extends BrokenMediaItemsExceptions {
        public BrokenMediaItemNotFoundExceptions(UUID id) {
            super(String.format("Broken media item with id %s not found", id), HttpStatus.NOT_FOUND);
        }
    }
}
