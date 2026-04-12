package com.rattatarr.rattatarr.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rattatarr.rattatarr.exceptions.JellyfinClientExceptions;
import org.springframework.http.HttpStatus;

public enum JellyfinMediaType {
    FOLDER("Folder"),
    MOVIE("Movie"),
    SERIES("Series"),
    SEASON("Season"),
    EPISODE("Episode"),
    MANUAL_PLAYLISTS_FOLDER("ManualPlaylistsFolder");

    private final String label;

    JellyfinMediaType(String label) {
        this.label = label;
    }

    @JsonCreator
    public static JellyfinMediaType fromLabel(String value) {
        for (JellyfinMediaType type : values()) {
            if (type.label.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new JellyfinClientExceptions("Unknown Jellyfin media type: " + value, HttpStatus.BAD_GATEWAY);
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
