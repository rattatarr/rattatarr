package com.rattatarr.rattatarr.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeConverterTest {

    @Test
    void testConstructorThrowsException() {
        // When/Then
        assertThrows(Exception.class, () -> {
            var constructor = TimeConverter.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testToUTCInstantWithValidLocalDateTime() {
        // Given
        LocalDateTime local = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        ZoneId zoneId = ZoneId.of("UTC");

        // When
        Instant result = TimeConverter.toUTCInstant(local, zoneId);

        // Then
        assertNotNull(result);
        assertEquals(Instant.parse("2026-01-01T12:00:00Z"), result);
    }

    @Test
    void testToUTCInstantWithNullLocalDateTime() {
        // Given
        ZoneId zoneId = ZoneId.of("UTC");

        // When
        Instant result = TimeConverter.toUTCInstant(null, zoneId);

        // Then
        assertNull(result);
    }

    @Test
    void testToUTCInstantWithNewYorkTimezone() {
        // Given
        LocalDateTime local = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        ZoneId zoneId = ZoneId.of("America/New_York");

        // When
        Instant result = TimeConverter.toUTCInstant(local, zoneId);

        // Then
        assertNotNull(result);
        // 12:00 in New York should be 17:00 UTC (EST is UTC-5)
        assertEquals(Instant.parse("2026-01-01T17:00:00Z"), result);
    }

    @Test
    void testToUTCInstantWithTokyoTimezone() {
        // Given
        LocalDateTime local = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        ZoneId zoneId = ZoneId.of("Asia/Tokyo");

        // When
        Instant result = TimeConverter.toUTCInstant(local, zoneId);

        // Then
        assertNotNull(result);
        // 12:00 in Tokyo should be 03:00 UTC (JST is UTC+9)
        assertEquals(Instant.parse("2026-01-01T03:00:00Z"), result);
    }

    @Test
    void testToUTCInstantWithLondonTimezone() {
        // Given
        LocalDateTime local = LocalDateTime.of(2026, 6, 1, 12, 0, 0); // Summer time
        ZoneId zoneId = ZoneId.of("Europe/London");

        // When
        Instant result = TimeConverter.toUTCInstant(local, zoneId);

        // Then
        assertNotNull(result);
        // During BST (British Summer Time), London is UTC+1
        assertEquals(Instant.parse("2026-06-01T11:00:00Z"), result);
    }

    @Test
    void testToUTCInstantPreservesInstantValue() {
        // Given
        LocalDateTime local = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        ZoneId utcZone = ZoneId.of("UTC");

        // When
        Instant result = TimeConverter.toUTCInstant(local, utcZone);
        ZonedDateTime zonedResult = result.atZone(utcZone);

        // Then
        assertEquals(local, zonedResult.toLocalDateTime());
    }

    @Test
    void testToUTCInstantWithMidnight() {
        // Given
        LocalDateTime local = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        ZoneId zoneId = ZoneId.of("UTC");

        // When
        Instant result = TimeConverter.toUTCInstant(local, zoneId);

        // Then
        assertNotNull(result);
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), result);
    }

    @Test
    void testToUTCInstantWithEndOfDay() {
        // Given
        LocalDateTime local = LocalDateTime.of(2026, 1, 1, 23, 59, 59);
        ZoneId zoneId = ZoneId.of("UTC");

        // When
        Instant result = TimeConverter.toUTCInstant(local, zoneId);

        // Then
        assertNotNull(result);
        assertEquals(Instant.parse("2026-01-01T23:59:59Z"), result);
    }

    @Test
    void testToUTCInstantWithDifferentTimezonesProducesDifferentInstants() {
        // Given
        LocalDateTime local = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        ZoneId utcZone = ZoneId.of("UTC");
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");

        // When
        Instant utcResult = TimeConverter.toUTCInstant(local, utcZone);
        Instant tokyoResult = TimeConverter.toUTCInstant(local, tokyoZone);

        // Then
        assertNotEquals(utcResult, tokyoResult);
    }

    @Test
    void testParseDateTimeWithTimezone() {
        // Given
        String dateTimeString = "2024-02-14T10:30:00Z";

        // When
        Long result = TimeConverter.parseDateTime(dateTimeString);

        // Then
        assertNotNull(result);
        assertEquals(Instant.parse("2024-02-14T10:30:00Z").toEpochMilli(), result);
    }

    @Test
    void testParseDateTimeWithoutTimezone() {
        // Given
        String dateTimeString = "2024-02-14T10:30:00";

        // When
        Long result = TimeConverter.parseDateTime(dateTimeString);

        // Then
        assertNotNull(result);
        // Should assume UTC
        assertEquals(Instant.parse("2024-02-14T10:30:00Z").toEpochMilli(), result);
    }

    @Test
    void testParseDateTimeWithNull() {
        // When
        Long result = TimeConverter.parseDateTime(null);

        // Then
        assertNull(result);
    }

    @Test
    void testParseDateTimeWithBlankString() {
        // When
        Long result = TimeConverter.parseDateTime("   ");

        // Then
        assertNull(result);
    }

    @Test
    void testParseDateTimeWithInvalidFormat() {
        // Given
        String invalidDateTime = "not-a-date";

        // When
        Long result = TimeConverter.parseDateTime(invalidDateTime);

        // Then
        assertNull(result);
    }

    @Test
    void testParseDateTimeWithPartialFormat() {
        // Given
        String partialDateTime = "2024-02-14";

        // When
        Long result = TimeConverter.parseDateTime(partialDateTime);

        // Then
        assertNull(result); // Should fail to parse, expecting full date-time
    }

    @Test
    void testToIsoDateTime() {
        // Given
        long timestamp = Instant.parse("2024-02-14T10:30:00Z").toEpochMilli();

        // When
        String result = TimeConverter.toIsoDateTime(timestamp);

        // Then
        assertEquals("2024-02-14T10:30:00Z", result);
    }

    @Test
    void testToIsoDateTimeRoundTrip() {
        // Given
        String original = "2024-02-14T10:30:00Z";
        Long timestamp = TimeConverter.parseDateTime(original);

        // When
        String result = TimeConverter.toIsoDateTime(timestamp);

        // Then
        assertEquals(original, result);
    }
}
