package com.rattatarr.rattatarr.exceptions;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public final class CommonExceptions {

    private CommonExceptions() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static class ResourceNotFoundExceptions extends BaseRattatarrExceptions {
        public ResourceNotFoundExceptions(String resourceType, UUID id) {
            super(String.format("%s with id %s not found", resourceType, id), HttpStatus.NOT_FOUND);
        }

        public ResourceNotFoundExceptions(String message) {
            super(message, HttpStatus.NOT_FOUND);
        }
    }

    public static class InvalidRequestExceptions extends BaseRattatarrExceptions {
        public InvalidRequestExceptions(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }

        public InvalidRequestExceptions(String message, @Nullable Throwable cause) {
            super(message, cause, HttpStatus.BAD_REQUEST);
        }
    }

    public static class InvalidTimezoneExceptions extends InvalidRequestExceptions {
        public InvalidTimezoneExceptions(String timezone) {
            super(String.format("Invalid timezone: %s", timezone));
        }
    }

    public static class UnauthorizedExceptions extends BaseRattatarrExceptions {
        public UnauthorizedExceptions(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    public static class ForbiddenExceptions extends BaseRattatarrExceptions {
        public ForbiddenExceptions(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }
    }

    public static class ConflictExceptions extends BaseRattatarrExceptions {
        public ConflictExceptions(String message) {
            super(message, HttpStatus.CONFLICT);
        }
    }
}
