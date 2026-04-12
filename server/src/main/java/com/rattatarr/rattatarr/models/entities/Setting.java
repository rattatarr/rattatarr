package com.rattatarr.rattatarr.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "settings")
public class Setting extends BaseEntity {
    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    @Size(max = 100, message = "Setting key cannot exceed {max} characters")
    private String key;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;

    protected Setting() {
    }

    public Setting(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public String key() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
