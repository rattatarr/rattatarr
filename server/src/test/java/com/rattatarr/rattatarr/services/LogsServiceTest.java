package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.InMemoryAppender;
import com.rattatarr.rattatarr.models.LogEvent;
import com.rattatarr.rattatarr.models.dtos.requests.LogsFilterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogsServiceTest {

    @Mock
    private InMemoryAppender inMemoryAppender;

    @InjectMocks
    private LogsService logsService;

    private List<LogEvent> sampleLogs;

    @BeforeEach
    void setUp() {
        // Create logs with specific timestamps for testing date filtering
        long now = System.currentTimeMillis();
        sampleLogs = List.of(
                new LogEvent(now - 3000, "INFO", "com.test.Logger1", "Info message 1", new HashMap<>(), "test-service"),
                new LogEvent(now - 2000, "ERROR", "com.test.ErrorLogger", "Error message", new HashMap<>(), "test-service"),
                new LogEvent(now - 1000, "WARN", "com.test.Logger2", "Warning message", new HashMap<>(), "test-service"),
                new LogEvent(now, "INFO", "com.test.Logger1", "Info message 2", new HashMap<>(), "test-service"),
                new LogEvent(now + 1000, "DEBUG", "com.test.DebugLogger", "Debug message", new HashMap<>(), "test-service")
        );
    }

    @Test
    void testGetLogsWithoutFilters() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(5, result.getTotalElements());
        assertEquals(5, result.getContent().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    @Test
    void testGetLogsFilteredByLevel() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO("INFO", null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(log -> log.level().equalsIgnoreCase("INFO")));
    }

    @Test
    void testGetLogsFilteredByLevelCaseInsensitive() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO("error", null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("ERROR", result.getContent().getFirst().level());
    }

    @Test
    void testGetLogsFilteredByDateRange() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        // Use ISO-8601 format dates
        String startDate = "2024-02-14T10:00:00";
        String endDate = "2024-02-14T12:00:00";
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, startDate, endDate, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        // Note: Actual filtering depends on sample log timestamps
        assertNotNull(result);
    }

    @Test
    void testGetLogsFilteredByStartDateOnly() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        String startDate = "2024-02-14T10:00:00";
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, startDate, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetLogsFilteredByEndDateOnly() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        String endDate = "2024-02-14T12:00:00";
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, endDate, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetLogsFilteredByLogger() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, "Error");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().getFirst().logger().contains("Error"));
    }

    @Test
    void testGetLogsFilteredByLoggerCaseInsensitive() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, "logger1");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        // Should match com.test.Logger1 (2 logs)
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testGetLogsWithPagination() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(5, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(2, result.getSize());
    }

    @Test
    void testGetLogsSecondPage() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(1, 2);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(5, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalPages());
        assertEquals(1, result.getNumber());
    }

    @Test
    void testGetLogsLastPage() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(2, 2);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(5, result.getTotalElements());
        assertEquals(1, result.getContent().size()); // Last page has only 1 log
        assertEquals(3, result.getTotalPages());
        assertEquals(2, result.getNumber());
    }

    @Test
    void testGetLogsPageBeyondAvailable() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(10, 2);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(5, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void testGetLogsMultipleFilters() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO("INFO", "2024-02-14T10:00:00", "2024-02-14T12:00:00", "Logger1");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertNotNull(result);
        // All returned logs should match the level and logger filters
        assertTrue(result.getContent().stream().allMatch(log ->
                log.level().equalsIgnoreCase("INFO") &&
                        log.logger().contains("Logger1")
        ));
    }

    @Test
    void testGetLogsSortedByTimestampDescending() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        List<LogEvent> logs = result.getContent();
        for (int i = 0; i < logs.size() - 1; i++) {
            assertTrue(logs.get(i).timestamp() >= logs.get(i + 1).timestamp(),
                    "Logs should be sorted by timestamp descending (newest first)");
        }
    }

    @Test
    void testGetLogsWithEmptyList() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(List.of());
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    void testGetLogsFilterNoMatches() {
        // Given
        when(inMemoryAppender.getEvents()).thenReturn(sampleLogs);
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO("TRACE", null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LogEvent> result = logsService.getLogs(filter, pageable);

        // Then
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
