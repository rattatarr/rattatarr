package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.agents.AgentClient;
import com.rattatarr.rattatarr.clients.agents.AgentMessage;
import com.rattatarr.rattatarr.exceptions.AgentExceptions;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileAgentServiceTest {

    @Mock
    private ProfilesRepository profilesRepository;
    @Mock
    private AgentPromptService agentPromptService;
    @Mock
    private AgentConversationService conversationService;
    @Mock
    private AgentClient ollamaClient;

    private ProfileAgentService service;

    private final UUID profileId = UUID.randomUUID();
    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile("Alice", null);
        when(ollamaClient.name()).thenReturn("ollama");
        service = new ProfileAgentService(
                profilesRepository,
                agentPromptService,
                conversationService,
                List.of(ollamaClient));
    }

    @Test
    void chat_shouldReturnAgentReply() {
        when(profilesRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(ollamaClient.isConfigured()).thenReturn(true);
        when(conversationService.loadHistory(profileId, "ollama")).thenReturn(List.of());
        when(agentPromptService.buildSystemPrompt(profile, profileId)).thenReturn("system-prompt");
        when(ollamaClient.chat(eq("system-prompt"), anyList(), eq("Recommend something"))).thenReturn("Watch Parasite.");
        doNothing().when(conversationService).saveExchange(any(), eq("ollama"), anyString(), anyString());

        String result = service.chat(profileId, "ollama", "Recommend something");

        assertEquals("Watch Parasite.", result);
        verify(conversationService).saveExchange(profile, "ollama", "Recommend something", "Watch Parasite.");
    }

    @Test
    void chat_shouldThrowWhenProfileNotFound() {
        when(profilesRepository.findById(profileId)).thenReturn(Optional.empty());

        assertThrows(ProfilesExceptions.ProfileNotFoundExceptions.class,
                () -> service.chat(profileId, "ollama", "Hello"));
    }

    @Test
    void chat_shouldThrowWhenAgentNotFound() {
        when(profilesRepository.findById(profileId)).thenReturn(Optional.of(profile));

        assertThrows(AgentExceptions.AgentNotFoundException.class,
                () -> service.chat(profileId, "unknown-agent", "Hello"));
    }

    @Test
    void chat_shouldThrowWhenAgentNotConfigured() {
        when(profilesRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(ollamaClient.isConfigured()).thenReturn(false);

        assertThrows(AgentExceptions.AgentNotConfiguredException.class,
                () -> service.chat(profileId, "ollama", "Hello"));
    }

    @Test
    void chat_shouldPassConversationHistoryToAgent() {
        List<AgentMessage> history = List.of(
                new AgentMessage(AgentMessage.Role.USER, "Hi"),
                new AgentMessage(AgentMessage.Role.ASSISTANT, "Hello!"));
        when(profilesRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(ollamaClient.isConfigured()).thenReturn(true);
        when(conversationService.loadHistory(profileId, "ollama")).thenReturn(history);
        when(agentPromptService.buildSystemPrompt(profile, profileId)).thenReturn("system-prompt");
        when(ollamaClient.chat(eq("system-prompt"), eq(history), eq("New question"))).thenReturn("An answer.");
        doNothing().when(conversationService).saveExchange(any(), anyString(), anyString(), anyString());

        String result = service.chat(profileId, "ollama", "New question");

        assertEquals("An answer.", result);
        verify(ollamaClient).chat(eq("system-prompt"), eq(history), eq("New question"));
    }

    @Test
    void clearConversation_shouldDelegateToConversationService() {
        when(profilesRepository.findById(profileId)).thenReturn(Optional.of(profile));
        doNothing().when(conversationService).clearHistory(profileId, "ollama");

        service.clearConversation(profileId, "ollama");

        verify(conversationService).clearHistory(profileId, "ollama");
    }
}
