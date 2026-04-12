package com.rattatarr.rattatarr.models.entities;

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
public class MediaItemRating extends BaseEntity {
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
}
