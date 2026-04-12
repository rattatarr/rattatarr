package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "media_seasons",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "media_item_id", "season"
        }),
        indexes = {
                @Index(name = "idx_season_media_item_id", columnList = "media_item_id")
        }
)
public class MediaSeason extends BaseEntity {
    @OneToMany(mappedBy = "mediaSeason", fetch = FetchType.LAZY)
    @OrderBy("episode ASC")
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 50)
    private Set<MediaEpisode> episodes = new HashSet<>();

    @OneToMany(mappedBy = "mediaSeason", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 50)
    private Set<MediaSeasonRating> ratings = new HashSet<>();

    @OneToOne(mappedBy = "mediaSeason")
    private MediaSeasonMetadata metadata;

    @ManyToOne(optional = false)
    private MediaItem mediaItem;

    @Column(name = "jellyfin_id", unique = true)
    private String jellyfinId;

    @Column(name = "season", nullable = false)
    private Integer season;

    @Column
    private String title;

    protected MediaSeason() {
    }

    public MediaSeason(MediaItem mediaItem,
                       String jellyfinId,
                       Integer season,
                       String title,
                       Set<MediaEpisode> episodes
    ) {
        this.mediaItem = mediaItem;
        this.jellyfinId = jellyfinId;
        this.season = season;
        this.title = title;
        this.episodes = episodes;
    }

    public MediaItem mediaItem() {
        return mediaItem;
    }

    public void setMediaItem(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
    }

    public String jellyfinId() {
        return jellyfinId;
    }

    public void setJellyfinId(String jellyfinId) {
        this.jellyfinId = jellyfinId;
    }

    public Integer season() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<MediaEpisode> episodes() {
        return episodes;
    }

    public void setEpisodes(Set<MediaEpisode> episodes) {
        this.episodes = episodes;
    }

    public MediaSeasonMetadata metadata() {
        return metadata;
    }

    public void setMetadata(MediaSeasonMetadata metadata) {
        this.metadata = metadata;
    }

    public Set<MediaSeasonRating> ratings() {
        return ratings;
    }

    public void setRatings(Set<MediaSeasonRating> ratings) {
        this.ratings = ratings;
    }

}
