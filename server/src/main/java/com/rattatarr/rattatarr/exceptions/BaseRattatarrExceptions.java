package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

public abstract class BaseRattatarrExceptions extends RuntimeException {
    private final HttpStatus status;

    protected BaseRattatarrExceptions(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    protected BaseRattatarrExceptions(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
