package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

public record ResolveBrokenMediaItemRequestDTO(
        @NotNull(message = "mediaItemId must not be null") UUID mediaItemId
) implements Serializable {
}
