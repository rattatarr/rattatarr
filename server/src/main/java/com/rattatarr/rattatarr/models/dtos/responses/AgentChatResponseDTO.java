package com.rattatarr.rattatarr.models.dtos.responses;

import java.io.Serializable;

public record AgentChatResponseDTO(
        String reply,
        String agent
) implements Serializable {
}
