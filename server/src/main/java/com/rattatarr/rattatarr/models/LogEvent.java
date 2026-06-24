package com.rattatarr.rattatarr.models;

import java.util.Map;

public record LogEvent(
        long timestamp,
        String level,
        String logger,
        String message,
        Map<String, String> mdc,
        String serviceName,
        String thread,
        long sequence,
        String stackTrace
) {
    public LogEvent(
            long timestamp,
            String level,
            String logger,
            String message,
            Map<String, String> mdc,
            String serviceName,
            String thread,
            long sequence
    ) {
        this(timestamp, level, logger, message, mdc, serviceName, thread, sequence, null);
    }
}
