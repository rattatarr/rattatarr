package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.clients.agents.AgentMessage;
import com.rattatarr.rattatarr.models.entities.AgentConversationMessage;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record AgentConversationMessageResponseDTO(
        UUID id,
        AgentMessage.Role role,
        String content,
        Instant sentAt
) implements Serializable {
    public static AgentConversationMessageResponseDTO fromEntity(AgentConversationMessage entity) {
        return new AgentConversationMessageResponseDTO(
                entity.id(), entity.role(), entity.content(), entity.sentAt());
    }
}
