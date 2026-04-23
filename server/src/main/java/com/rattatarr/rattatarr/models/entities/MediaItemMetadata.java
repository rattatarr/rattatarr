package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "media_item_metadata")
public class MediaItemMetadata extends BaseEntity {
    @OneToOne(optional = false)
    @JoinColumn(name = "media_item_id", nullable = false, unique = true)
    private MediaItem mediaItem;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster_image_url")
    private String posterImageUrl;

    @Column(name = "backdrop_image_url")
    private String backdropImageUrl;

    @Column(name = "imdb_rating")
    private Float imdbRating;

    @Column(name = "rotten_tomatoes_rating")
    private Integer rottenTomatoesRating;

    protected MediaItemMetadata() {

    }

    public MediaItemMetadata(MediaItem mediaItem, String description, String posterImageUrl, String backdropImageUrl) {
        this.mediaItem = mediaItem;
        this.description = description;
        this.posterImageUrl = posterImageUrl;
        this.backdropImageUrl = backdropImageUrl;
    }

    public MediaItem mediaItem() {
        return mediaItem;
    }

    public void setMediaItem(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String posterImageUrl() {
        return posterImageUrl;
    }

    public void setPosterImageUrl(String posterImageUrl) {
        this.posterImageUrl = posterImageUrl;
    }

    public String backdropImageUrl() {
        return backdropImageUrl;
    }

    public void setBackdropImageUrl(String backdropImageUrl) {
        this.backdropImageUrl = backdropImageUrl;
    }

    public Float imdbRating() {
        return imdbRating;
    }

    public void setImdbRating(Float imdbRating) {
        this.imdbRating = imdbRating;
    }

    public Integer rottenTomatoesRating() {
        return rottenTomatoesRating;
    }

    public void setRottenTomatoesRating(Integer rottenTomatoesRating) {
        this.rottenTomatoesRating = rottenTomatoesRating;
    }
}
