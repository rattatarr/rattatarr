package com.rattatarr.rattatarr.utils;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TimeConverter {
    private static final Logger logger = LoggerFactory.getLogger(TimeConverter.class);

    private TimeConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Instant toUTCInstant(LocalDateTime local, ZoneId zoneId) {
        if (local == null) {
            return null;
        }

        return local.atZone(zoneId).toInstant();
    }

    @Nullable
    public static Long parseDateTime(@Nullable String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }

        try {
            // Try parsing as Instant first (handles "2024-02-14T10:30:00Z" with timezone)
            return Instant.parse(dateTimeString).toEpochMilli();
        } catch (DateTimeParseException e1) {
            try {
                // Try parsing as LocalDateTime (handles "2024-02-14T10:30:00" without timezone)
                // Assume UTC if no timezone specified
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
            } catch (DateTimeParseException e2) {
                logger.warn("Failed to parse date-time string: '{}'. Expected ISO-8601 format (e.g., '2024-02-14T10:30:00' or '2024-02-14T10:30:00Z')", dateTimeString);
                return null;
            }
        }
    }

    public static String toIsoDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).toString();
    }
}
