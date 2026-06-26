package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaReviewType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "media_season_ratings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"profile_id", "media_season_id"})
        },
        indexes = {
                @Index(name = "idx_season_rating_media_season_id", columnList = "media_season_id")
        }
)
public class MediaSeasonRating extends BaseEntity implements ReviewableRating {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_season_id", nullable = false)
    private MediaSeason mediaSeason;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating cannot exceed 10.0")
    @Column(nullable = false)
    private Float rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type")
    private MediaReviewType reviewType;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "review_story", columnDefinition = "TEXT")
    private String reviewStory;

    @Column(name = "review_performances", columnDefinition = "TEXT")
    private String reviewPerformances;

    @Column(name = "review_direction", columnDefinition = "TEXT")
    private String reviewDirection;

    @Column(name = "review_visuals", columnDefinition = "TEXT")
    private String reviewVisuals;

    @Column(name = "review_sound", columnDefinition = "TEXT")
    private String reviewSound;

    @Column(name = "review_verdict", columnDefinition = "TEXT")
    private String reviewVerdict;

    protected MediaSeasonRating() {
    }

    public MediaSeasonRating(Profile profile, MediaSeason mediaSeason, Float rating) {
        this.profile = profile;
        this.mediaSeason = mediaSeason;
        this.rating = rating;
    }

    public Profile profile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public MediaSeason mediaSeason() {
        return mediaSeason;
    }

    public void setMediaSeason(MediaSeason mediaSeason) {
        this.mediaSeason = mediaSeason;
    }

    public Float rating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    @Override
    public MediaReviewType reviewType() {
        return reviewType;
    }

    @Override
    public void setReviewType(MediaReviewType reviewType) {
        this.reviewType = reviewType;
    }

    @Override
    public String reviewText() {
        return reviewText;
    }

    @Override
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    @Override
    public String reviewStory() {
        return reviewStory;
    }

    @Override
    public void setReviewStory(String reviewStory) {
        this.reviewStory = reviewStory;
    }

    @Override
    public String reviewPerformances() {
        return reviewPerformances;
    }

    @Override
    public void setReviewPerformances(String reviewPerformances) {
        this.reviewPerformances = reviewPerformances;
    }

    @Override
    public String reviewDirection() {
        return reviewDirection;
    }

    @Override
    public void setReviewDirection(String reviewDirection) {
        this.reviewDirection = reviewDirection;
    }

    @Override
    public String reviewVisuals() {
        return reviewVisuals;
    }

    @Override
    public void setReviewVisuals(String reviewVisuals) {
        this.reviewVisuals = reviewVisuals;
    }

    @Override
    public String reviewSound() {
        return reviewSound;
    }

    @Override
    public void setReviewSound(String reviewSound) {
        this.reviewSound = reviewSound;
    }

    @Override
    public String reviewVerdict() {
        return reviewVerdict;
    }

    @Override
    public void setReviewVerdict(String reviewVerdict) {
        this.reviewVerdict = reviewVerdict;
    }
}
