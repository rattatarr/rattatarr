package com.rattatarr.rattatarr.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ZonedMapperTest {

    @Test
    void testConstructorThrowsException() {
        // When/Then
        assertThrows(Exception.class, () -> {
            var constructor = ZonedMapper.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testToZonedDateTimeWithInstantAndZone() {
        // Given
        Instant instant = Instant.parse("2024-01-01T12:00:00Z");
        ZoneId zoneId = ZoneId.of("America/New_York");

        // When
        ZonedDateTime result = ZonedMapper.toZonedDateTime(instant, zoneId);

        // Then
        assertNotNull(result);
        assertEquals(instant, result.toInstant());
        assertEquals(zoneId, result.getZone());
    }

    @Test
    void testToZonedDateTimeWithInstantOnly() {
        // Given
        Instant instant = Instant.parse("2024-01-01T12:00:00Z");

        // When
        ZonedDateTime result = ZonedMapper.toZonedDateTime(instant);

        // Then
        assertNotNull(result);
        assertEquals(instant, result.toInstant());
        assertEquals(ZoneId.systemDefault(), result.getZone());
    }

    @Test
    void testToZonedDateTimeWithUTC() {
        // Given
        Instant instant = Instant.now();
        ZoneId utcZone = ZoneId.of("UTC");

        // When
        ZonedDateTime result = ZonedMapper.toZonedDateTime(instant, utcZone);

        // Then
        assertEquals(utcZone, result.getZone());
        assertEquals(instant, result.toInstant());
    }

    @Test
    void testToZonedDateTimeWithTokyo() {
        // Given
        Instant instant = Instant.now();
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");

        // When
        ZonedDateTime result = ZonedMapper.toZonedDateTime(instant, tokyoZone);

        // Then
        assertEquals(tokyoZone, result.getZone());
        assertEquals(instant, result.toInstant());
    }

    @Test
    void testToZonedDateTimePreservesInstant() {
        // Given
        Instant instant = Instant.parse("2024-01-01T12:00:00Z");
        ZoneId zone1 = ZoneId.of("UTC");
        ZoneId zone2 = ZoneId.of("Asia/Tokyo");

        // When
        ZonedDateTime result1 = ZonedMapper.toZonedDateTime(instant, zone1);
        ZonedDateTime result2 = ZonedMapper.toZonedDateTime(instant, zone2);

        // Then
        assertEquals(result1.toInstant(), result2.toInstant());
        assertNotEquals(result1.getZone(), result2.getZone());
    }

    @Test
    void testToZonedDateTimeWithDifferentTimezonesShowsDifferentLocalTime() {
        // Given
        Instant instant = Instant.parse("2024-01-01T12:00:00Z");
        ZoneId utcZone = ZoneId.of("UTC");
        ZoneId nyZone = ZoneId.of("America/New_York");

        // When
        ZonedDateTime utcResult = ZonedMapper.toZonedDateTime(instant, utcZone);
        ZonedDateTime nyResult = ZonedMapper.toZonedDateTime(instant, nyZone);

        // Then
        assertEquals(instant, utcResult.toInstant());
        assertEquals(instant, nyResult.toInstant());
        assertNotEquals(utcResult.getHour(), nyResult.getHour());
    }

    @Test
    void testToZonedDateTimeWithPastInstant() {
        // Given
        Instant pastInstant = Instant.parse("2000-01-01T00:00:00Z");
        ZoneId zoneId = ZoneId.of("Europe/London");

        // When
        ZonedDateTime result = ZonedMapper.toZonedDateTime(pastInstant, zoneId);

        // Then
        assertEquals(pastInstant, result.toInstant());
        assertEquals(zoneId, result.getZone());
    }

    @Test
    void testToZonedDateTimeWithFutureInstant() {
        // Given
        Instant futureInstant = Instant.parse("2030-12-31T23:59:59Z");
        ZoneId zoneId = ZoneId.of("Pacific/Auckland");

        // When
        ZonedDateTime result = ZonedMapper.toZonedDateTime(futureInstant, zoneId);

        // Then
        assertEquals(futureInstant, result.toInstant());
        assertEquals(zoneId, result.getZone());
    }

    @Test
    void testToZonedDateTimeDefaultUsesSystemZone() {
        // Given
        Instant instant = Instant.now();

        // When
        ZonedDateTime result = ZonedMapper.toZonedDateTime(instant);

        // Then
        assertEquals(ZoneId.systemDefault(), result.getZone());
    }
}
