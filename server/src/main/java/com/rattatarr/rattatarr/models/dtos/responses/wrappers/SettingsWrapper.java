package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.SettingValue;
import com.rattatarr.rattatarr.models.dtos.responses.SettingResponseDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record SettingsWrapper(
        List<SettingResponseDTO> settings
) implements Serializable {
    public static SettingsWrapper fromMap(Map<String, SettingValue> settings) {
        List<SettingResponseDTO> settingDTOs = settings.entrySet().stream()
                .map(entry -> new SettingResponseDTO(
                        entry.getKey(),
                        entry.getValue().value(),
                        entry.getValue().description()
                ))
                .toList();
        return new SettingsWrapper(settingDTOs);
    }
}
