package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "background_jobs")
public class BackgroundJob extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private JobType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;

    @Column(name = "message")
    private String message;

    @Column(name = "profile_id")
    private UUID profileId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    protected BackgroundJob() {}

    public BackgroundJob(JobType type, UUID profileId) {
        this.type = type;
        this.status = JobStatus.PENDING;
        this.profileId = profileId;
    }

    public void markRunning() {
        this.status = JobStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void markCompleted(String message) {
        this.status = JobStatus.COMPLETED;
        this.message = message;
        this.finishedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.status = JobStatus.FAILED;
        this.message = message;
        this.finishedAt = Instant.now();
    }

    public void updateMessage(String message) {
        this.message = message;
    }

    public JobType type() { return type; }
    public JobStatus status() { return status; }
    public String message() { return message; }
    public UUID profileId() { return profileId; }
    public Instant startedAt() { return startedAt; }
    public Instant finishedAt() { return finishedAt; }
}
