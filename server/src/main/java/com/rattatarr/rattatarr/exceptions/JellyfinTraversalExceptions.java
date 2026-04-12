package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

public class JellyfinTraversalExceptions extends BaseRattatarrExceptions {
    private JellyfinTraversalExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class JellyfinTraversalFailedException extends JellyfinTraversalExceptions {
        public JellyfinTraversalFailedException(Throwable cause) {
            super(String.format("Jellyfin traversal failed: %s", cause.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
