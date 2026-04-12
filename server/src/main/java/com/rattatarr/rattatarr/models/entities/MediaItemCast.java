package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.*;

@Entity
@Table(
        name = "media_item_cast",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"media_item_id", "person_id", "character"})
        },
        indexes = {
                @Index(name = "idx_cast_media_item_id", columnList = "media_item_id"),
                @Index(name = "idx_cast_person_id", columnList = "person_id")
        }
)
public class MediaItemCast extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_item_id", nullable = false)
    private MediaItem mediaItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(nullable = false)
    private String character;

    @Column(name = "cast_order")
    private Integer order;

    protected MediaItemCast() {
    }

    public MediaItemCast(MediaItem mediaItem, Person person, String character, Integer order) {
        this.mediaItem = mediaItem;
        this.person = person;
        this.character = character;
        this.order = order;
    }

    public MediaItem mediaItem() {
        return mediaItem;
    }

    public void setMediaItem(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
    }

    public Person person() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String character() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public Integer order() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
