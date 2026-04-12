package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ProfilesExceptions extends BaseRattatarrExceptions {
    private ProfilesExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class ProfileNotFoundExceptions extends ProfilesExceptions {
        public ProfileNotFoundExceptions(UUID id) {
            super(String.format("Profile with id %s not found", id), HttpStatus.NOT_FOUND);
        }

        public ProfileNotFoundExceptions(String name) {
            super(String.format("Profile with name '%s' not found", name), HttpStatus.NOT_FOUND);
        }
    }

    public static class DuplicateProfileNameExceptions extends ProfilesExceptions {
        public DuplicateProfileNameExceptions(String name) {
            super(String.format("Profile with name '%s' already taken", name), HttpStatus.CONFLICT);
        }
    }

    public static class InvalidProfileNameExceptions extends ProfilesExceptions {
        public InvalidProfileNameExceptions(String reason) {
            super(String.format("Invalid profile name: %s", reason), HttpStatus.BAD_REQUEST);
        }
    }
}
