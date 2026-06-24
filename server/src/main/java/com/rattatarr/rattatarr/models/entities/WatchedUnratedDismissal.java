package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Tombstone marking that a profile dismissed a media item from the
 * "watched but not rated" list. The item stays hidden only while there is no
 * qualifying watch event newer than {@code dismissedAt}; a later re-watch
 * (watchedAt &gt; dismissedAt) makes it re-appear automatically.
 */
@Entity
@Table(name = "watched_unrated_dismissals",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "media_item_id"}),
        indexes = {
                @Index(name = "idx_dismissal_profile_item", columnList = "profile_id, media_item_id")
        }
)
public class WatchedUnratedDismissal extends BaseEntity {

    @ManyToOne(optional = false)
    private Profile profile;

    @ManyToOne(optional = false)
    private MediaItem mediaItem;

    @Column(name = "dismissed_at", nullable = false)
    private Instant dismissedAt;

    protected WatchedUnratedDismissal() {
    }

    public WatchedUnratedDismissal(Profile profile, MediaItem mediaItem, Instant dismissedAt) {
        this.profile = profile;
        this.mediaItem = mediaItem;
        this.dismissedAt = dismissedAt;
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

    public Instant dismissedAt() {
        return dismissedAt;
    }

    public void setDismissedAt(Instant dismissedAt) {
        this.dismissedAt = dismissedAt;
    }
}
