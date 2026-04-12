package com.rattatarr.rattatarr.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class ZonedMapper {
    private ZonedMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ZonedDateTime toZonedDateTime(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId);
    }

    public static ZonedDateTime toZonedDateTime(Instant instant) {
        return toZonedDateTime(instant, ZoneId.systemDefault());
    }
}
