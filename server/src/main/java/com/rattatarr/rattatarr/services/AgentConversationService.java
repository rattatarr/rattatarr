package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.agents.AgentMessage;
import com.rattatarr.rattatarr.models.entities.AgentConversationMessage;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.AgentConversationMessageRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@NullMarked
public class AgentConversationService extends BaseService<AgentConversationMessage, AgentConversationMessageRepository> {

    private static final int HISTORY_LIMIT = 20;

    public AgentConversationService(AgentConversationMessageRepository repository) {
        super(repository);
    }

    @Override
    protected void updateEntity(AgentConversationMessage existing, AgentConversationMessage updated) {
    }

    @Transactional(readOnly = true)
    public List<AgentMessage> loadHistory(UUID profileId, String agentName) {
        List<AgentConversationMessage> recent =
                repository.findRecentByProfileAndAgent(profileId, agentName, HISTORY_LIMIT);
        Collections.reverse(recent);
        return recent.stream()
                .map(m -> new AgentMessage(m.role(), m.content()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgentConversationMessage> fetchHistory(UUID profileId, String agentName) {
        List<AgentConversationMessage> recent =
                repository.findRecentByProfileAndAgent(profileId, agentName, HISTORY_LIMIT);
        Collections.reverse(recent);
        return recent;
    }

    @Transactional
    public void saveExchange(Profile profile, String agentName, String userMessage, String assistantReply) {
        repository.save(new AgentConversationMessage(profile, AgentMessage.Role.USER, userMessage, agentName));
        repository.save(new AgentConversationMessage(profile, AgentMessage.Role.ASSISTANT, assistantReply, agentName));
    }

    @Transactional
    public void clearHistory(UUID profileId, String agentName) {
        repository.deleteByProfileAndAgent(profileId, agentName);
        logger.info("Cleared conversation history for profile {} agent {}", profileId, agentName);
    }
}
