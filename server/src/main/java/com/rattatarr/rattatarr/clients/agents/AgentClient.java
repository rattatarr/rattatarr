package com.rattatarr.rattatarr.clients.agents;

import java.util.List;

public interface AgentClient {

    /**
     * Send a message to the agent and return its response.
     *
     * @param systemPrompt      instructions + profile context
     * @param conversationHistory prior turns (oldest first), may be empty
     * @param userMessage       the new message from the user
     * @return the agent's text response
     */
    String chat(String systemPrompt, List<AgentMessage> conversationHistory, String userMessage);

    boolean isConfigured();

    String name();
}
