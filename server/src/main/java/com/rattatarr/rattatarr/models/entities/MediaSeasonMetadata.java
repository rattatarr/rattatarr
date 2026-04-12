package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "media_season_metadata")
public class MediaSeasonMetadata extends BaseEntity {
    @OneToOne(optional = false)
    @JoinColumn(name = "media_season_id", nullable = false, unique = true)
    private MediaSeason mediaSeason;

    @Column(name = "episode_count")
    private int episodeCount;

    @Column(name = "poster_image_url")
    private String posterImageUrl;

    @Column(name = "air_date")
    private String airDate;

    protected MediaSeasonMetadata() {
    }

    public MediaSeasonMetadata(MediaSeason mediaSeason, int episodeCount, String posterImageUrl, String airDate) {
        this.mediaSeason = mediaSeason;
        this.episodeCount = episodeCount;
        this.posterImageUrl = posterImageUrl;
        this.airDate = airDate;
    }

    public MediaSeason mediaSeason() {
        return mediaSeason;
    }

    public void setMediaSeason(MediaSeason mediaSeason) {
        this.mediaSeason = mediaSeason;
    }

    public int episodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String posterImageUrl() {
        return posterImageUrl;
    }

    public void setPosterImageUrl(String posterImageUrl) {
        this.posterImageUrl = posterImageUrl;
    }

    public String airDate() {
        return airDate;
    }

    public void setAirDate(String airDate) {
        this.airDate = airDate;
    }
}
