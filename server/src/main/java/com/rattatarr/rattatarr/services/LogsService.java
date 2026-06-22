package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.InMemoryAppender;
import com.rattatarr.rattatarr.models.LogEvent;
import com.rattatarr.rattatarr.models.dtos.requests.LogsFilterRequestDTO;
import com.rattatarr.rattatarr.utils.TimeConverter;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@NullMarked
public class LogsService {
    private static final Logger logger = LoggerFactory.getLogger(LogsService.class);
    // The logs endpoint logs its own activity; those lines are pure noise in the UI console, so exclude them.
    private static final String SELF_LOGGER = LogsService.class.getName();
    private final InMemoryAppender inMemoryAppender;

    public LogsService(InMemoryAppender inMemoryAppender) {
        this.inMemoryAppender = inMemoryAppender;
    }

    public Page<LogEvent> getLogs(LogsFilterRequestDTO filter, Pageable pageable) {
        logger.debug("Fetching logs with filter: level={}, startDate={}, endDate={}, logger={}, requestId={}, page={}, size={}",
                filter.level(), filter.startDate(), filter.endDate(), filter.logger(), filter.requestId(),
                pageable.getPageNumber(), pageable.getPageSize());

        Long startTimestamp = TimeConverter.parseDateTime(filter.startDate());
        Long endTimestamp = TimeConverter.parseDateTime(filter.endDate());

        List<LogEvent> allLogs = inMemoryAppender.getEvents();

        List<LogEvent> filteredLogs = allLogs.stream()
                .filter(event -> !SELF_LOGGER.equals(event.logger()))
                .filter(event -> matchesLevel(event, filter.level()))
                .filter(event -> matchesDateRange(event, startTimestamp, endTimestamp))
                .filter(event -> matchesLogger(event, filter.logger()))
                .filter(event -> matchesRequestId(event, filter.requestId()))
                .sorted(Comparator.comparing(LogEvent::timestamp).reversed()) // Newest first
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredLogs.size());

        List<LogEvent> paginatedLogs = start >= filteredLogs.size()
                ? List.of()
                : filteredLogs.subList(start, end);

        logger.debug("Returning {} logs out of {} total matching logs", paginatedLogs.size(), filteredLogs.size());

        return new PageImpl<>(paginatedLogs, pageable, filteredLogs.size());
    }

    private boolean matchesLevel(LogEvent event, String level) {
        if (level == null || level.isBlank()) {
            return true;
        }
        return event.level().equalsIgnoreCase(level.trim());
    }

    private boolean matchesDateRange(LogEvent event, Long startDate, Long endDate) {
        if (startDate != null && event.timestamp() < startDate) {
            return false;
        }
        if (endDate != null && event.timestamp() > endDate) {
            return false;
        }
        return true;
    }

    private boolean matchesLogger(LogEvent event, String logger) {
        if (logger == null || logger.isBlank()) {
            return true;
        }
        return event.logger().toLowerCase().contains(logger.trim().toLowerCase());
    }

    private boolean matchesRequestId(LogEvent event, String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return true;
        }
        String eventRequestId = event.mdc() == null ? null : event.mdc().get("requestId");
        return requestId.trim().equalsIgnoreCase(eventRequestId);
    }
}
