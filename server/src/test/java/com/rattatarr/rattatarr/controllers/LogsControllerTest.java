package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.InMemoryAppender;
import com.rattatarr.rattatarr.models.LogEvent;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.LogsResponseWrapper;
import com.rattatarr.rattatarr.services.LogsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogsControllerTest {

    @Mock
    private LogsService logsService;

    @Mock
    private InMemoryAppender inMemoryAppender;

    @InjectMocks
    private LogsController logsController;

    private List<LogEvent> sampleLogs;

    @BeforeEach
    void setUp() {
        sampleLogs = List.of(
                new LogEvent(System.currentTimeMillis(), "INFO", "com.test.Logger1", "Info message", new HashMap<>(), "test-service", "test-thread", 0L),
                new LogEvent(System.currentTimeMillis(), "ERROR", "com.test.Logger2", "Error message", new HashMap<>(), "test-service", "test-thread", 1L),
                new LogEvent(System.currentTimeMillis(), "WARN", "com.test.Logger3", "Warning message", new HashMap<>(), "test-service", "test-thread", 2L),
                new LogEvent(System.currentTimeMillis(), "INFO", "com.test.Logger4", "Another info", new HashMap<>(), "test-service", "test-thread", 3L)
        );
    }

    @Test
    void testGetLogsWithoutFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 100);
        Page<LogEvent> expectedPage = new PageImpl<>(sampleLogs, pageable, 4);
        when(logsService.getLogs(any(), any())).thenReturn(expectedPage);

        // When
        LogsResponseWrapper result = logsController.getLogs(null, null, null, null, null, pageable);

        // Then
        assertEquals(4, result.pagination().totalElements());
        assertEquals(4, result.logs().size());
        verify(logsService).getLogs(any(), any());
    }

    @Test
    void testGetLogsWithLevelFilter() {
        // Given
        Pageable pageable = PageRequest.of(0, 100);
        List<LogEvent> infoLogs = sampleLogs.stream()
                .filter(log -> log.level().equalsIgnoreCase("INFO"))
                .toList();
        Page<LogEvent> expectedPage = new PageImpl<>(infoLogs, pageable, 2);
        when(logsService.getLogs(any(), any())).thenReturn(expectedPage);

        // When
        LogsResponseWrapper result = logsController.getLogs("INFO", null, null, null, null, pageable);

        // Then
        assertEquals(2, result.pagination().totalElements());
        assertEquals(2, result.logs().size());
        assertTrue(result.logs().stream().allMatch(log -> log.level().equalsIgnoreCase("INFO")));
    }

    @Test
    void testGetLogsWithDateRangeFilter() {
        // Given
        String startDate = "2024-02-14T10:00:00";
        String endDate = "2024-02-14T15:00:00";
        Pageable pageable = PageRequest.of(0, 100);
        Page<LogEvent> expectedPage = new PageImpl<>(sampleLogs, pageable, 4);
        when(logsService.getLogs(any(), any())).thenReturn(expectedPage);

        // When
        LogsResponseWrapper result = logsController.getLogs(null, startDate, endDate, null, null, pageable);

        // Then
        assertEquals(4, result.pagination().totalElements());
        verify(logsService).getLogs(any(), any());
    }

    @Test
    void testGetLogsWithLoggerFilter() {
        // Given
        Pageable pageable = PageRequest.of(0, 100);
        List<LogEvent> logger1Logs = List.of(sampleLogs.get(0));
        Page<LogEvent> expectedPage = new PageImpl<>(logger1Logs, pageable, 1);
        when(logsService.getLogs(any(), any())).thenReturn(expectedPage);

        // When
        LogsResponseWrapper result = logsController.getLogs(null, null, null, "Logger1", null, pageable);

        // Then
        assertEquals(1, result.pagination().totalElements());
        verify(logsService).getLogs(any(), any());
    }

    @Test
    void testGetLogsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);
        List<LogEvent> firstPage = sampleLogs.subList(0, 2);
        Page<LogEvent> expectedPage = new PageImpl<>(firstPage, pageable, 4);
        when(logsService.getLogs(any(), any())).thenReturn(expectedPage);

        // When
        LogsResponseWrapper result = logsController.getLogs(null, null, null, null, null, pageable);

        // Then
        assertEquals(4, result.pagination().totalElements());
        assertEquals(2, result.logs().size());
        assertEquals(0, result.pagination().currentPage());
        assertEquals(2, result.pagination().pageSize());
    }

    @Test
    void testGetLogsSecondPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 2);
        List<LogEvent> secondPage = sampleLogs.subList(2, 4);
        Page<LogEvent> expectedPage = new PageImpl<>(secondPage, pageable, 4);
        when(logsService.getLogs(any(), any())).thenReturn(expectedPage);

        // When
        LogsResponseWrapper result = logsController.getLogs(null, null, null, null, null, pageable);

        // Then
        assertEquals(4, result.pagination().totalElements());
        assertEquals(2, result.logs().size());
        assertEquals(1, result.pagination().currentPage());
    }

    @Test
    void testGetLogsWithAllFilters() {
        // Given
        String startDate = "2024-02-14T10:00:00";
        String endDate = "2024-02-14T15:00:00";
        Pageable pageable = PageRequest.of(0, 10);
        List<LogEvent> filtered = List.of(sampleLogs.get(0));
        Page<LogEvent> expectedPage = new PageImpl<>(filtered, pageable, 1);
        when(logsService.getLogs(any(), any())).thenReturn(expectedPage);

        // When
        LogsResponseWrapper result = logsController.getLogs("INFO", startDate, endDate, "Logger1", null, pageable);

        // Then
        assertEquals(1, result.pagination().totalElements());
        verify(logsService).getLogs(any(), any());
    }

    @Test
    void testGetLogsWithEmptyResult() {
        // Given
        Pageable pageable = PageRequest.of(0, 100);
        Page<LogEvent> expectedPage = new PageImpl<>(List.of(), pageable, 0);
        when(logsService.getLogs(any(), any())).thenReturn(expectedPage);

        // When
        LogsResponseWrapper result = logsController.getLogs("TRACE", null, null, null, null, pageable);

        // Then
        assertEquals(0, result.pagination().totalElements());
        assertTrue(result.logs().isEmpty());
    }
}
