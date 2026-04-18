package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.agents.AgentClient;
import com.rattatarr.rattatarr.clients.agents.AgentMessage;
import com.rattatarr.rattatarr.exceptions.AgentExceptions;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import com.rattatarr.rattatarr.utils.SQLiteRetry;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NullMarked
public class ProfileAgentService {

    private final Logger logger = LoggerFactory.getLogger(ProfileAgentService.class);

    private final ProfilesRepository profilesRepository;
    private final AgentPromptService agentPromptService;
    private final AgentConversationService conversationService;
    private final Map<String, AgentClient> agentsByName;

    public ProfileAgentService(
            ProfilesRepository profilesRepository,
            AgentPromptService agentPromptService,
            AgentConversationService conversationService,
            List<AgentClient> agentClients
    ) {
        this.profilesRepository = profilesRepository;
        this.agentPromptService = agentPromptService;
        this.conversationService = conversationService;
        this.agentsByName = agentClients.stream().collect(Collectors.toMap(AgentClient::name, Function.identity()));
    }

    public String chat(UUID profileId, String agentName, String userMessage) {
        Profile profile = profilesRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        AgentClient agent = resolveAgent(agentName);
        if (!agent.isConfigured()) {
            throw new AgentExceptions.AgentNotConfiguredException(agentName);
        }

        List<AgentMessage> history = conversationService.loadHistory(profileId, agentName);
        String systemPrompt = agentPromptService.buildSystemPrompt(profile, profileId);

        logger.debug("Sending chat for profile {} via agent {}, history size {}", profileId, agentName, history.size());

        String reply = agent.chat(systemPrompt, history, userMessage);

        SQLiteRetry.execute(
                () -> conversationService.saveExchange(profile, agentName, userMessage, reply),
                logger,
                "save conversation exchange");

        return reply;
    }

    public String buildContextPrompt(UUID profileId) {
        Profile profile = profilesRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfilesExceptions.ProfileNotFoundExceptions(profileId));
        return agentPromptService.buildSystemPrompt(profile, profileId);
    }

    public void clearConversation(UUID profileId, String agentName) {
        profilesRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfilesExceptions.ProfileNotFoundExceptions(profileId));
        resolveAgent(agentName);
        conversationService.clearHistory(profileId, agentName);
    }

    private AgentClient resolveAgent(String agentName) {
        AgentClient client = agentsByName.get(agentName.toLowerCase());
        if (client == null) {
            throw new AgentExceptions.AgentNotFoundException(agentName);
        }
        return client;
    }
}
