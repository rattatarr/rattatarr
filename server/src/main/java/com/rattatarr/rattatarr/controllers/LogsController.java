package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.LogsFilterRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.LogsResponseWrapper;
import com.rattatarr.rattatarr.services.LogsService;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@ApiVersion("v1")
public class LogsController {
    private static final Logger logger = LoggerFactory.getLogger(LogsController.class);
    private final LogsService logsService;

    public LogsController(LogsService logsService) {
        this.logsService = logsService;
    }

    @GetMapping
    public LogsResponseWrapper getLogs(
            @Parameter(description = "Filter by log level (DEBUG, INFO, WARN, ERROR) - case insensitive")
            @RequestParam(required = false) String level,
            @Parameter(
                    description = "Start date-time in ISO-8601 format (e.g., '2024-02-14T10:30:00' or '2024-02-14T10:30:00Z')",
                    example = "2024-02-14T10:30:00"
            )
            @RequestParam(required = false) String startDate,
            @Parameter(
                    description = "End date-time in ISO-8601 format (e.g., '2024-02-14T15:30:00' or '2024-02-14T15:30:00Z')",
                    example = "2024-02-14T15:30:00"
            )
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Filter by logger name (substring match, case insensitive)")
            @RequestParam(required = false) String logger,
            @Parameter(description = "Filter by MDC requestId (exact match, case insensitive) to trace all logs of one request")
            @RequestParam(required = false) String requestId,
            @PageableDefault(size = 100) Pageable pageable
    ) {
        LogsFilterRequestDTO filter = new LogsFilterRequestDTO(level, startDate, endDate, logger, requestId);
        return LogsResponseWrapper.fromPage(logsService.getLogs(filter, pageable));
    }
}
