package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BackgroundJobResponseDTO(
        UUID id,
        JobType type,
        JobStatus status,
        String message,
        UUID profileId,
        Instant startedAt,
        Instant finishedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static BackgroundJobResponseDTO fromEntity(BackgroundJob job) {
        return new BackgroundJobResponseDTO(
                job.id(),
                job.type(),
                job.status(),
                job.message(),
                job.profileId(),
                job.startedAt(),
                job.finishedAt(),
                job.createdAt(),
                job.updatedAt()
        );
    }
}
