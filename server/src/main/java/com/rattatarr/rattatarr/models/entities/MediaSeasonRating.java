package com.rattatarr.rattatarr.models.entities;

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
public class MediaSeasonRating extends BaseEntity {
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
}
