package com.rattatarr.rattatarr.models.dtos.requests;

import java.io.Serializable;
import java.util.List;

public record CreateSettingsRequestDTO(
        List<CreateSettingRequestDTO> settings
) implements Serializable {
}
