package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

public record SearchFiltersDTO(
        @NotBlank(message = "Query cannot be blank")
        String query,
        @Nullable
        String posterSize,
        @Nullable
        Integer page
) implements Serializable {
}
