package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

public class SettingsExceptions extends BaseRattatarrExceptions {
    private SettingsExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    public static class SettingNotFoundException extends SettingsExceptions {
        public SettingNotFoundException(String key) {
            super(String.format("Setting with key '%s' not found", key), HttpStatus.NOT_FOUND);
        }
    }
}
