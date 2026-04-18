package com.rattatarr.rattatarr.models.dtos.requests;

import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import org.jspecify.annotations.Nullable;

public record JobsFilterRequestDTO(
        @Nullable JobType type,
        @Nullable JobStatus status,
        @Nullable String startDate,
        @Nullable String endDate
) {
}
