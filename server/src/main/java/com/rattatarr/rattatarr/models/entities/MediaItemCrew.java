package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.*;

@Entity
@Table(
        name = "media_item_crew",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"media_item_id", "person_id", "job", "department"})
        },
        indexes = {
                @Index(name = "idx_crew_media_item_id", columnList = "media_item_id"),
                @Index(name = "idx_crew_person_id", columnList = "person_id"),
                @Index(name = "idx_crew_job", columnList = "job")
        }
)
public class MediaItemCrew extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_item_id", nullable = false)
    private MediaItem mediaItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(nullable = false)
    private String job;

    @Column(nullable = false)
    private String department;

    protected MediaItemCrew() {
    }

    public MediaItemCrew(MediaItem mediaItem, Person person, String job, String department) {
        this.mediaItem = mediaItem;
        this.person = person;
        this.job = job;
        this.department = department;
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

    public String job() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String department() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
