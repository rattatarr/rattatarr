package com.rattatarr.rattatarr;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.rattatarr.rattatarr.models.LogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryAppenderTest {

    private InMemoryAppender appender;

    @BeforeEach
    void setUp() {
        appender = new InMemoryAppender();
        ReflectionTestUtils.setField(appender, "serviceName", "test-service");
        appender.start();
    }

    @Test
    void testAppendEvent() {
        // Given
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Test message");
        when(event.getLevel()).thenReturn(Level.INFO);
        when(event.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(event.getLoggerName()).thenReturn("com.rattatarr.test");
        when(event.getMDCPropertyMap()).thenReturn(new HashMap<>());
        when(event.getThreadName()).thenReturn("test-thread");

        // When
        appender.append(event);

        // Then
        List<LogEvent> logs = appender.getEvents();
        assertEquals(1, logs.size());
        assertEquals("Test message", logs.getFirst().message());
        assertEquals("INFO", logs.getFirst().level());
        assertEquals("test-service", logs.getFirst().serviceName());
    }

    @Test
    void testGetEventsInitiallyEmpty() {
        // When
        List<LogEvent> logs = appender.getEvents();

        // Then
        assertNotNull(logs);
        assertTrue(logs.isEmpty());
    }

    @Test
    void testMultipleEvents() {
        // Given
        ILoggingEvent event1 = createMockEvent("Message 1", Level.INFO);
        ILoggingEvent event2 = createMockEvent("Message 2", Level.ERROR);
        ILoggingEvent event3 = createMockEvent("Message 3", Level.WARN);

        // When
        appender.append(event1);
        appender.append(event2);
        appender.append(event3);

        // Then
        List<LogEvent> logs = appender.getEvents();
        assertEquals(3, logs.size());
        assertEquals("Message 1", logs.get(0).message());
        assertEquals("Message 2", logs.get(1).message());
        assertEquals("Message 3", logs.get(2).message());
    }

    @Test
    void testAppendEventWithMDC() {
        // Given
        ILoggingEvent event = mock(ILoggingEvent.class);
        Map<String, String> mdc = new HashMap<>();
        mdc.put("userId", "123");
        mdc.put("requestId", "abc-xyz");

        when(event.getFormattedMessage()).thenReturn("Test message");
        when(event.getLevel()).thenReturn(Level.INFO);
        when(event.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(event.getLoggerName()).thenReturn("com.rattatarr.test");
        when(event.getMDCPropertyMap()).thenReturn(mdc);
        when(event.getThreadName()).thenReturn("test-thread");

        // When
        appender.append(event);

        // Then
        List<LogEvent> logs = appender.getEvents();
        assertEquals(1, logs.size());
        assertEquals(mdc, logs.getFirst().mdc());
    }

    @Test
    void testAppendEventCapturesStackTrace() {
        // Given
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("Boom");
        when(event.getLevel()).thenReturn(Level.ERROR);
        when(event.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(event.getLoggerName()).thenReturn("com.rattatarr.test");
        when(event.getMDCPropertyMap()).thenReturn(new HashMap<>());
        when(event.getThreadName()).thenReturn("test-thread");
        when(event.getThrowableProxy())
                .thenReturn(new ch.qos.logback.classic.spi.ThrowableProxy(
                        new IllegalStateException("kaboom")));

        // When
        appender.append(event);

        // Then
        String stackTrace = appender.getEvents().getFirst().stackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("IllegalStateException"));
        assertTrue(stackTrace.contains("kaboom"));
    }

    @Test
    void testAppendEventWithoutThrowableHasNullStackTrace() {
        // Given (default mock returns null throwable proxy)
        ILoggingEvent event = createMockEvent("No error", Level.INFO);

        // When
        appender.append(event);

        // Then
        assertNull(appender.getEvents().getFirst().stackTrace());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // Given
        int threadCount = 10;
        int eventsPerThread = 5;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    ILoggingEvent event = createMockEvent(
                            "Thread " + threadIndex + " Message " + j,
                            Level.INFO
                    );
                    appender.append(event);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        List<LogEvent> logs = appender.getEvents();
        assertEquals(threadCount * eventsPerThread, logs.size());
    }

    private ILoggingEvent createMockEvent(String message, Level level) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(message);
        when(event.getLevel()).thenReturn(level);
        when(event.getTimeStamp()).thenReturn(System.currentTimeMillis());
        when(event.getLoggerName()).thenReturn("com.rattatarr.test");
        when(event.getMDCPropertyMap()).thenReturn(new HashMap<>());
        when(event.getThreadName()).thenReturn(Thread.currentThread().getName());
        return event;
    }
}
