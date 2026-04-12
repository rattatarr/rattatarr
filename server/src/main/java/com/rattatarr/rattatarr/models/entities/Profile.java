package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    @Size(max = 100, message = "Profile name cannot exceed {max} characters")
    private String name;

    @Column(name = "jellyfin_id", unique = true)
    private String jellyfinId;

    protected Profile() {
    }

    public Profile(String name, String jellyfinId) {
        this.name = name;
        this.jellyfinId = jellyfinId;
    }

    @Override
    public void softDelete() {
        String timestamp = Instant.now().toString().replace(":", "-");
        this.name = this.name + "_deleted_at_" + timestamp;
        super.softDelete();
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String jellyfinId() {
        return jellyfinId;
    }

    public void setJellyfinId(String jellyfinId) {
        this.jellyfinId = jellyfinId;
    }


}
