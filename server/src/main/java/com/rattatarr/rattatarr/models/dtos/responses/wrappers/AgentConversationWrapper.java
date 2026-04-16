package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.AgentConversationMessageResponseDTO;

import java.util.List;

public record AgentConversationWrapper(List<AgentConversationMessageResponseDTO> conversation) {

    public static AgentConversationWrapper of(List<AgentConversationMessageResponseDTO> messages) {
        return new AgentConversationWrapper(messages);
    }
}
