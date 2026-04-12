package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "media_episodes",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "media_season_id", "episode"
        }),
        indexes = {
                @Index(name = "idx_episode_media_season_id", columnList = "media_season_id")
        }
)
public class MediaEpisode extends BaseEntity {
    @ManyToOne(optional = false)
    private MediaSeason mediaSeason;

    @Column(name = "jellyfin_id", unique = true)
    private String jellyfinId;

    @Column(nullable = false)
    private Integer episode;

    @Column(nullable = false)
    private String title;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    protected MediaEpisode() {
    }

    public MediaEpisode(MediaSeason mediaSeason, String jellyfinId, Integer episode, String title, Integer runtimeMinutes) {
        this.mediaSeason = mediaSeason;
        this.jellyfinId = jellyfinId;
        this.episode = episode;
        this.title = title;
        this.runtimeMinutes = runtimeMinutes;
    }

    public MediaEpisode(MediaSeason mediaSeason, Integer episode, String title, Integer runtimeMinutes) {
        this.mediaSeason = mediaSeason;
        this.episode = episode;
        this.title = title;
        this.runtimeMinutes = runtimeMinutes;
    }

    public MediaSeason mediaSeason() {
        return mediaSeason;
    }

    public void setMediaSeason(MediaSeason mediaSeason) {
        this.mediaSeason = mediaSeason;
    }

    public String jellyfinId() {
        return jellyfinId;
    }

    public void setJellyfinId(String jellyfinId) {
        this.jellyfinId = jellyfinId;
    }

    public Integer episode() {
        return episode;
    }

    public void setEpisode(Integer episode) {
        this.episode = episode;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer runtimeMinutes() {
        return runtimeMinutes;
    }

    public void setRuntimeMinutes(Integer runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }
}
