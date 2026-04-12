package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaTargetType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_completions",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "profile_id", "target_type", "target_id"
        })
)
public class MediaCompletion extends BaseEntity {
    @ManyToOne(optional = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private MediaTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "first_watched_at")
    private Instant firstWatchedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "days_to_complete")
    private Integer daysToComplete;

    protected MediaCompletion() {
    }

    public MediaCompletion(Profile profile, MediaTargetType targetType, UUID targetId) {
        this.profile = profile;
        this.targetType = targetType;
        this.targetId = targetId;
        this.completed = false;
    }

    public Profile profile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public MediaTargetType targetType() {
        return targetType;
    }

    public void setTargetType(MediaTargetType targetType) {
        this.targetType = targetType;
    }

    public UUID targetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public boolean completed() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Instant firstWatchedAt() {
        return firstWatchedAt;
    }

    public void setFirstWatchedAt(Instant firstWatchedAt) {
        this.firstWatchedAt = firstWatchedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Integer daysToComplete() {
        return daysToComplete;
    }

    public void setDaysToComplete(Integer daysToComplete) {
        this.daysToComplete = daysToComplete;
    }
}
