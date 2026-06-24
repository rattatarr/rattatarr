package com.rattatarr.rattatarr.models.dtos.requests;

import org.jspecify.annotations.Nullable;

public record LogsFilterRequestDTO(
        @Nullable String level,
        @Nullable String startDate,
        @Nullable String endDate,
        @Nullable String logger,
        @Nullable String requestId
) {
    public LogsFilterRequestDTO(
            @Nullable String level,
            @Nullable String startDate,
            @Nullable String endDate,
            @Nullable String logger
    ) {
        this(level, startDate, endDate, logger, null);
    }
}
