package com.rattatarr.rattatarr.models.dtos.requests;

import com.rattatarr.rattatarr.models.MediaType;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record ImportMediaItemRequestDTO(
        @NotBlank(message = "ID cannot be blank")
        String id,
        MediaType mediaType
) implements Serializable {
}
