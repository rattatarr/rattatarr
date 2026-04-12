package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record CreateSettingRequestDTO(
        @NotBlank(message = "Setting key cannot be blank")
        String key,
        @NotBlank(message = "Setting value cannot be blank")
        String value,
        String description
) implements Serializable {
}
