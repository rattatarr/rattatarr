package com.rattatarr.rattatarr.models.dtos.responses;

import java.io.Serializable;

public record AgentPromptResponseDTO(
        String prompt,
        String agent
) implements Serializable {
}
