package com.rattatarr.rattatarr.clients.agents;

public record AgentMessage(Role role, String content) {
    public enum Role {
        USER,
        ASSISTANT
    }
}
