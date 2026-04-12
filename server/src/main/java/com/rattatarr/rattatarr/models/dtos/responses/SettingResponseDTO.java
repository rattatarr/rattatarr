package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.models.entities.Setting;

import java.io.Serializable;

public record SettingResponseDTO(
        String key,
        String value,
        String description
) implements Serializable {
    public static SettingResponseDTO fromEntity(Setting setting) {
        return new SettingResponseDTO(
                setting.key(),
                setting.value(),
                setting.description()
        );
    }
}
