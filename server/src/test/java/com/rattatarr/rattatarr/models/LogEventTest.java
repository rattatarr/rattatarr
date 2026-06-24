package com.rattatarr.rattatarr.models;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogEventTest {

    @Test
    void testRecordConstructor() {
        // Given
        long timestamp = System.currentTimeMillis();
        String level = "INFO";
        String logger = "com.rattatarr.test";
        String message = "Test message";
        Map<String, String> mdc = new HashMap<>();
        mdc.put("userId", "123");
        String serviceName = "test-service";

        // When
        LogEvent event = new LogEvent(timestamp, level, logger, message, mdc, serviceName, "test-thread", 0L);

        // Then
        assertEquals(timestamp, event.timestamp());
        assertEquals(level, event.level());
        assertEquals(logger, event.logger());
        assertEquals(message, event.message());
        assertEquals(mdc, event.mdc());
        assertEquals(serviceName, event.serviceName());
    }

    @Test
    void testRecordEquality() {
        // Given
        long timestamp = System.currentTimeMillis();
        Map<String, String> mdc = new HashMap<>();

        LogEvent event1 = new LogEvent(timestamp, "INFO", "logger", "Test", mdc, "service", "test-thread", 0L);
        LogEvent event2 = new LogEvent(timestamp, "INFO", "logger", "Test", mdc, "service", "test-thread", 0L);

        // Then
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void testRecordInequality() {
        // Given
        long timestamp = System.currentTimeMillis();
        Map<String, String> mdc = new HashMap<>();

        LogEvent event1 = new LogEvent(timestamp, "INFO", "logger", "Test", mdc, "service", "test-thread", 0L);
        LogEvent event2 = new LogEvent(timestamp, "ERROR", "logger", "Test", mdc, "service", "test-thread", 0L);

        // Then
        assertNotEquals(event1, event2);
    }

    @Test
    void testRecordToString() {
        // Given
        Map<String, String> mdc = new HashMap<>();
        LogEvent event = new LogEvent(123456789L, "ERROR", "com.test", "Test error", mdc, "app", "test-thread", 0L);

        // When
        String toString = event.toString();

        // Then
        assertTrue(toString.contains("ERROR"));
        assertTrue(toString.contains("Test error"));
        assertTrue(toString.contains("123456789"));
    }

    @Test
    void testWithNullMdc() {
        // When
        LogEvent event = new LogEvent(System.currentTimeMillis(), "INFO", "logger", "message", null, "service", "test-thread", 0L);

        // Then
        assertNull(event.mdc());
    }

    @Test
    void testWithEmptyMdc() {
        // Given
        Map<String, String> emptyMdc = new HashMap<>();

        // When
        LogEvent event = new LogEvent(System.currentTimeMillis(), "INFO", "logger", "message", emptyMdc, "service", "test-thread", 0L);

        // Then
        assertNotNull(event.mdc());
        assertTrue(event.mdc().isEmpty());
    }

    @Test
    void testWithMultipleMdcEntries() {
        // Given
        Map<String, String> mdc = new HashMap<>();
        mdc.put("userId", "123");
        mdc.put("requestId", "abc-xyz");
        mdc.put("sessionId", "session-456");

        // When
        LogEvent event = new LogEvent(System.currentTimeMillis(), "INFO", "logger", "message", mdc, "service", "test-thread", 0L);

        // Then
        assertEquals(3, event.mdc().size());
        assertEquals("123", event.mdc().get("userId"));
        assertEquals("abc-xyz", event.mdc().get("requestId"));
        assertEquals("session-456", event.mdc().get("sessionId"));
    }

    @Test
    void testDifferentLogLevels() {
        // Given
        long timestamp = System.currentTimeMillis();
        Map<String, String> mdc = new HashMap<>();

        // When
        LogEvent info = new LogEvent(timestamp, "INFO", "logger", "Info message", mdc, "service", "test-thread", 0L);
        LogEvent warn = new LogEvent(timestamp, "WARN", "logger", "Warn message", mdc, "service", "test-thread", 1L);
        LogEvent error = new LogEvent(timestamp, "ERROR", "logger", "Error message", mdc, "service", "test-thread", 2L);
        LogEvent debug = new LogEvent(timestamp, "DEBUG", "logger", "Debug message", mdc, "service", "test-thread", 3L);

        // Then
        assertEquals("INFO", info.level());
        assertEquals("WARN", warn.level());
        assertEquals("ERROR", error.level());
        assertEquals("DEBUG", debug.level());
    }
}
