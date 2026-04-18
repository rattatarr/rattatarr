package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.dtos.requests.JobsFilterRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.BackgroundJobsWrapper;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@ApiVersion("v1")
public class JobsController {

    private final BackgroundJobService backgroundJobService;

    public JobsController(BackgroundJobService backgroundJobService) {
        this.backgroundJobService = backgroundJobService;
    }

    @GetMapping
    public BackgroundJobsWrapper getJobs(
            @Parameter(description = "Filter by job type (JELLYFIN_SYNC, CSV_IMPORT)")
            @RequestParam(required = false) JobType type,
            @Parameter(description = "Filter by job status (PENDING, RUNNING, COMPLETED, FAILED)")
            @RequestParam(required = false) JobStatus status,
            @Parameter(description = "Start date-time in ISO-8601 format", example = "2024-02-14T10:30:00")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date-time in ISO-8601 format", example = "2024-02-14T15:30:00")
            @RequestParam(required = false) String endDate,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        JobsFilterRequestDTO filter = new JobsFilterRequestDTO(type, status, startDate, endDate);
        return BackgroundJobsWrapper.fromPage(
                backgroundJobService.findWithFilters(filter, pageable).map(BackgroundJobResponseDTO::fromEntity)
        );
    }

    @GetMapping("/{id}")
    public BackgroundJobResponseDTO getJob(@PathVariable UUID id) {
        return BackgroundJobResponseDTO.fromEntity(
                backgroundJobService.findByIdOrThrow(id, jobId -> new CommonExceptions.ResourceNotFoundExceptions("BackgroundJob", jobId))
        );
    }
}
