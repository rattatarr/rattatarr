package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.SettingValue;
import com.rattatarr.rattatarr.models.dtos.responses.SettingResponseDTO;

import java.io.Serializable;
import java.util.Map;

public record SettingWrapper(
        SettingResponseDTO setting
) implements Serializable {
    public static SettingWrapper fromMap(Map<String, SettingValue> setting) {
        Map.Entry<String, SettingValue> entry = setting.entrySet().iterator().next();
        SettingResponseDTO settingDTO = new SettingResponseDTO(
                entry.getKey(),
                entry.getValue().value(),
                entry.getValue().description()
        );
        return new SettingWrapper(settingDTO);
    }
}
