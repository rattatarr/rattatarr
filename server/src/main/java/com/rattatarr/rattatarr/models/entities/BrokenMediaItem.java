package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaType;
import jakarta.persistence.*;

@Entity
@Table(name = "broken_media_items")
public class BrokenMediaItem extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String title;

    @Column(name = "jellyfin_id", unique = true, nullable = false)
    private String jellyfinId;

    @Column(name = "tmdb_id", unique = true)
    private String TMDbId;

    @Column(name = "imdb_id", unique = true)
    private String IMDbId;

    @Column(name = "production_year")
    private Integer productionYear;

    @Column(name = "missing_fields", nullable = false)
    private String missingFields;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    @OneToOne
    @JoinColumn(name = "resolved_media_item_id", unique = true)
    private MediaItem resolvedMediaItem;

    protected BrokenMediaItem() {
    }

    public BrokenMediaItem(
            MediaType mediaType,
            String title,
            String jellyfinId,
            String TMDbId,
            String IMDbId,
            Integer productionYear,
            String missingFields,
            boolean resolved
    ) {
        this.mediaType = mediaType;
        this.title = title;
        this.jellyfinId = jellyfinId;
        this.TMDbId = TMDbId;
        this.IMDbId = IMDbId;
        this.productionYear = productionYear;
        this.missingFields = missingFields;
        this.resolved = resolved;
    }

    public BrokenMediaItem(
            MediaType mediaType,
            String title,
            String jellyfinId,
            String TMDbId,
            String IMDbId,
            Integer productionYear,
            String missingFields
    ) {
        this.mediaType = mediaType;
        this.title = title;
        this.jellyfinId = jellyfinId;
        this.TMDbId = TMDbId;
        this.IMDbId = IMDbId;
        this.productionYear = productionYear;
        this.missingFields = missingFields;
        this.resolved = false;
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

    public String missingFields() {
        return missingFields;
    }

    public void setMissingFields(String missingFields) {
        this.missingFields = missingFields;
    }

    public boolean resolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public MediaItem resolvedMediaItem() {
        return resolvedMediaItem;
    }

    public void setResolvedMediaItem(MediaItem resolvedMediaItem) {
        this.resolvedMediaItem = resolvedMediaItem;
    }
}
