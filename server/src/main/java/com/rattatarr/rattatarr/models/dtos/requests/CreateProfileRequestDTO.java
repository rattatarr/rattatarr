package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record CreateProfileRequestDTO(
        @NotBlank(message = "Profile name cannot be blank")
        @Size(max = 100, message = "Profile name cannot exceed {max} characters")
        String name,
        @Nullable
        String jellyfinId
) implements Serializable {
}
