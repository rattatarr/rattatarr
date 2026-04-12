package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.util.Objects;

@Entity
@Table(name = "people")
@BatchSize(size = 50)
public class Person extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(name = "tmdb_id", unique = true, nullable = false, updatable = false)
    private String TMDbId;

    @Column(name = "profile_path_url")
    private String profilePathUrl;

    protected Person() {
    }

    public Person(String name, String TMDbId, String profilePathUrl) {
        this.name = name;
        this.TMDbId = TMDbId;
        this.profilePathUrl = profilePathUrl;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String TMDbId() {
        return TMDbId;
    }

    public void setTMDbId(String TMDbId) {
        this.TMDbId = TMDbId;
    }

    public String profilePathUrl() {
        return profilePathUrl;
    }

    public void setProfilePathUrl(String profilePathUrl) {
        this.profilePathUrl = profilePathUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Person person)) return false;
        return Objects.equals(name, person.name) && Objects.equals(TMDbId, person.TMDbId) && Objects.equals(profilePathUrl, person.profilePathUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(TMDbId);
    }
}
