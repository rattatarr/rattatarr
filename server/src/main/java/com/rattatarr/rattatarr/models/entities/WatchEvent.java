package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.WatchEventType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "watch_events",
        indexes = {
                @Index(name = "idx_watch_profile_time", columnList = "profile_id, watched_at"),
                @Index(name = "idx_watch_media_time", columnList = "media_item_id"),
                @Index(name = "idx_watch_season_time", columnList = "media_season_id"),
                @Index(name = "idx_watch_jellyfin_log_id", columnList = "jellyfin_log_id", unique = true)
        }
)
public class WatchEvent extends BaseEntity {

    @Column(name = "jellyfin_log_id")
    private Long jellyfinLogId;

    @ManyToOne(optional = false)
    private Profile profile;

    @ManyToOne(optional = false)
    private MediaItem mediaItem;

    @ManyToOne
    private MediaSeason mediaSeason;

    @ManyToOne
    private MediaEpisode episode; // nullable for movies

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private WatchEventType eventType;

    @Column(name = "watched_at", nullable = false)
    private Instant watchedAt;

    @Column(name = "position_seconds")
    private Integer positionSeconds; // playstate -> positionticks

    protected WatchEvent() {
    }

    public WatchEvent(Long jellyfinLogId, Profile profile, MediaItem mediaItem, MediaSeason mediaSeason, MediaEpisode episode, WatchEventType eventType, Instant watchedAt, Integer positionSeconds) {
        this.jellyfinLogId = jellyfinLogId;
        this.profile = profile;
        this.mediaItem = mediaItem;
        this.mediaSeason = mediaSeason;
        this.episode = episode;
        this.eventType = eventType;
        this.watchedAt = watchedAt;
        this.positionSeconds = positionSeconds;
    }

    public Profile profile() {
        return profile;
    }

    public Long jellyfinLogId() {
        return jellyfinLogId;
    }

    public void setJellyfinLogId(Long jellyfinLogId) {
        this.jellyfinLogId = jellyfinLogId;
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

    public MediaSeason mediaSeason() {
        return mediaSeason;
    }

    public void setSeason(MediaSeason mediaSeason) {
        this.mediaSeason = mediaSeason;
    }

    public MediaEpisode episode() {
        return episode;
    }

    public void setEpisode(MediaEpisode episode) {
        this.episode = episode;
    }

    public WatchEventType eventType() {
        return eventType;
    }

    public void setEventType(WatchEventType eventType) {
        this.eventType = eventType;
    }

    public Instant watchedAt() {
        return watchedAt;
    }

    public void setWatchedAt(Instant watchedAt) {
        this.watchedAt = watchedAt;
    }

    public Integer positionSeconds() {
        return positionSeconds;
    }

    public void setPositionSeconds(Integer positionSeconds) {
        this.positionSeconds = positionSeconds;
    }
}
