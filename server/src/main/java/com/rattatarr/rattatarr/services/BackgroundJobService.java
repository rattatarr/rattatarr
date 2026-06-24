package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.dtos.requests.JobsFilterRequestDTO;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.websocket.WebSocketMessageType;
import com.rattatarr.rattatarr.repositories.BackgroundJobRepository;
import com.rattatarr.rattatarr.specifications.BackgroundJobSpecifications;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import com.rattatarr.rattatarr.utils.TimeConverter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@NullMarked
public class BackgroundJobService extends BaseService<BackgroundJob, BackgroundJobRepository> {

    private final JobEventPublisher eventPublisher;

    public BackgroundJobService(BackgroundJobRepository repository, JobEventPublisher eventPublisher) {
        super(repository);
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void updateEntity(BackgroundJob existing, BackgroundJob updated) {
        // State transitions are explicit — no generic update needed
    }

    @Transactional
    public BackgroundJob create(JobType type, @Nullable UUID profileId) {
        BackgroundJob job = new BackgroundJob(type, profileId);
        repository.save(job);
        logger.info("Job created: id={}, type={}, profileId={}", job.id(), type, profileId);
        eventPublisher.publish(job, WebSocketMessageType.JOB_STARTED);
        return job;
    }

    @Transactional
    public void markRunning(BackgroundJob job) {
        job.markRunning();
        repository.save(job);
        logger.debug("Job running: id={}, type={}", job.id(), job.type());
        eventPublisher.publish(job, WebSocketMessageType.JOB_STARTED);
    }

    @Transactional
    public void markCompleted(BackgroundJob job, String message) {
        job.markCompleted(message);
        repository.save(job);
        logger.info("Job completed: id={}, type={}", job.id(), job.type());
        eventPublisher.publish(job, WebSocketMessageType.JOB_COMPLETED);
    }

    @Transactional
    public void markFailed(BackgroundJob job, String message) {
        job.markFailed(message);
        repository.save(job);
        logger.error("Job failed: id={}, type={}, message={}", job.id(), job.type(), message);
        eventPublisher.publish(job, WebSocketMessageType.JOB_FAILED);
    }

    @Transactional
    public void sendProgress(BackgroundJob job, String message) {
        job.updateMessage(message);
        repository.save(job);
        logger.debug("Job progress: id={}, type={} — {}", job.id(), job.type(), message);
        eventPublisher.publish(job, WebSocketMessageType.JOB_PROGRESS);
    }

    @Transactional(readOnly = true)
    public Page<BackgroundJob> findWithFilters(JobsFilterRequestDTO filter, Pageable pageable) {
        Specification<BackgroundJob> spec = Specification.allOf(
                BackgroundJobSpecifications.hasType(filter.type()),
                BackgroundJobSpecifications.hasStatus(filter.status()),
                GenericSpecifications.createdAtAfter(TimeConverter.parseToInstant(filter.startDate())),
                GenericSpecifications.createdAtBefore(TimeConverter.parseToInstant(filter.endDate()))
        );
        return repository.findAll(spec, pageable);
    }
}
