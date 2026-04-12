package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record GenresFiltersDTO(
        @NotBlank(message = "Name cannot be blank")
        String name
) implements Serializable {
}
