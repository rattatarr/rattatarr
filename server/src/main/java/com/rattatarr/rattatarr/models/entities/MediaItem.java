package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaType;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "media_items")
public class MediaItem extends BaseEntity {
    @ManyToMany
    @JoinTable(
            name = "media_item_genres",
            joinColumns = @JoinColumn(name = "media_item_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"),
            indexes = {
                    @Index(name = "idx_item_genre_genre_id", columnList = "genre_id")
            }
    )
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 50)
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "mediaItem", fetch = FetchType.LAZY)
    @OrderBy("season ASC")
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 50)
    private Set<MediaSeason> seasons = new HashSet<>();

    @OneToMany(mappedBy = "mediaItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 50)
    private Set<MediaItemCast> cast = new HashSet<>();

    @OneToMany(mappedBy = "mediaItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 50)
    private Set<MediaItemCrew> crew = new HashSet<>();

    @OneToMany(mappedBy = "mediaItem", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 50)
    private Set<MediaItemRating> ratings = new HashSet<>();

    @OneToOne(mappedBy = "mediaItem")
    private MediaItemMetadata metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String title;

    @Column(name = "jellyfin_id", unique = true)
    private String jellyfinId;

    @Column(name = "tmdb_id", unique = true)
    private String TMDbId;

    @Column(name = "imdb_id", unique = true)
    private String IMDbId;

    @Column(name = "production_year", nullable = false)
    private Integer productionYear;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    protected MediaItem() {
    }

    public MediaItem(MediaType mediaType,
                     String title,
                     String jellyfinId,
                     String TMDbId,
                     String IMDbId,
                     Integer productionYear,
                     Integer runtimeMinutes,
                     Set<Genre> genres,
                     Set<MediaSeason> seasons,
                     Set<MediaItemCast> cast,
                     Set<MediaItemCrew> crew
    ) {
        this.mediaType = mediaType;
        this.title = title;
        this.jellyfinId = jellyfinId;
        this.TMDbId = TMDbId;
        this.IMDbId = IMDbId;
        this.productionYear = productionYear;
        this.runtimeMinutes = runtimeMinutes;
        this.genres = genres;
        this.seasons = seasons;
        this.cast = cast;
        this.crew = crew;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String jellyfinId() {
        return jellyfinId;
    }

    public void setJellyfinId(String jellyfinId) {
        this.jellyfinId = jellyfinId;
    }

    public String TMDbId() {
        return TMDbId;
    }

    public void setTMDbId(String TMDbId) {
        this.TMDbId = TMDbId;
    }

    public String IMDbId() {
        return IMDbId;
    }

    public void setIMDbId(String IMDbId) {
        this.IMDbId = IMDbId;
    }

    public Integer productionYear() {
        return productionYear;
    }

    public void setProductionYear(Integer productionYear) {
        this.productionYear = productionYear;
    }

    public Set<Genre> genres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public Integer runtimeMinutes() {
        return runtimeMinutes;
    }

    public void setRuntimeMinutes(Integer runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }

    public Set<MediaSeason> seasons() {
        return seasons;
    }

    public void setSeasons(Set<MediaSeason> seasons) {
        this.seasons = seasons;
    }

    public Set<MediaItemCast> cast() {
        return cast;
    }

    public void setCast(Set<MediaItemCast> cast) {
        this.cast = cast;
    }

    public Set<MediaItemCrew> crew() {
        return crew;
    }

    public void setCrew(Set<MediaItemCrew> crew) {
        this.crew = crew;
    }

    public MediaItemMetadata metadata() {
        return metadata;
    }

    public void setMetadata(MediaItemMetadata metadata) {
        this.metadata = metadata;
    }

    public Set<MediaItemRating> ratings() {
        return ratings;
    }

    public void setRatings(Set<MediaItemRating> ratings) {
        this.ratings = ratings;
    }
}
