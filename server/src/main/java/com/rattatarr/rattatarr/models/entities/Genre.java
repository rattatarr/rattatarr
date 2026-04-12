package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "genres")
public class Genre extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    protected Genre() {
    }

    public Genre(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
