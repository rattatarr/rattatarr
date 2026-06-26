package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaReviewType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(
        name = "media_item_ratings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"profile_id", "media_item_id"})
        },
        indexes = {
                @Index(name = "idx_item_rating_media_item_id", columnList = "media_item_id")
        }
)
public class MediaItemRating extends BaseEntity implements ReviewableRating {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_item_id", nullable = false)
    private MediaItem mediaItem;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating cannot exceed 10.0")
    @Column(nullable = false)
    private Float rating;

    @Column
    private Instant ratedAt;

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

    protected MediaItemRating() {
    }

    public MediaItemRating(Profile profile, MediaItem mediaItem, Float rating) {
        this.profile = profile;
        this.mediaItem = mediaItem;
        this.rating = rating;
        this.ratedAt = Instant.now();
    }

    public MediaItemRating(Profile profile, MediaItem mediaItem, Float rating, Instant ratedAt) {
        this.profile = profile;
        this.mediaItem = mediaItem;
        this.rating = rating;
        this.ratedAt = ratedAt != null ? ratedAt : Instant.now();
    }

    public Profile profile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public MediaItem mediaItem() {
        return mediaItem;
    }

    public void setMediaItem(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
    }

    public Float rating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Instant ratedAt() {
        return ratedAt;
    }

    public void setRatedAt(Instant ratedAt) {
        this.ratedAt = ratedAt;
    }

    public MediaReviewType reviewType() {
        return reviewType;
    }

    public void setReviewType(MediaReviewType reviewType) {
        this.reviewType = reviewType;
    }

    public String reviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String reviewStory() {
        return reviewStory;
    }

    public void setReviewStory(String reviewStory) {
        this.reviewStory = reviewStory;
    }

    public String reviewPerformances() {
        return reviewPerformances;
    }

    public void setReviewPerformances(String reviewPerformances) {
        this.reviewPerformances = reviewPerformances;
    }

    public String reviewDirection() {
        return reviewDirection;
    }

    public void setReviewDirection(String reviewDirection) {
        this.reviewDirection = reviewDirection;
    }

    public String reviewVisuals() {
        return reviewVisuals;
    }

    public void setReviewVisuals(String reviewVisuals) {
        this.reviewVisuals = reviewVisuals;
    }

    public String reviewSound() {
        return reviewSound;
    }

    public void setReviewSound(String reviewSound) {
        this.reviewSound = reviewSound;
    }

    public String reviewVerdict() {
        return reviewVerdict;
    }

    public void setReviewVerdict(String reviewVerdict) {
        this.reviewVerdict = reviewVerdict;
    }
}
