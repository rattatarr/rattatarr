package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record PeopleFiltersDTO(
        @NotBlank(message = "Name cannot be blank")
        String name,
        String profileSize
) implements Serializable {
    public PeopleFiltersDTO {
        if (profileSize == null) {
            profileSize = "w185";
        }
    }
}
