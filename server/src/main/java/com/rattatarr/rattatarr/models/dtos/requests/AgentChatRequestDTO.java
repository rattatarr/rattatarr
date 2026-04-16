package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AgentChatRequestDTO(
        @NotNull(message = "profileId is required") UUID profileId,
        @NotBlank(message = "message cannot be blank")
        @Size(max = 2000, message = "message cannot exceed 2000 characters")
        String message) {
}
