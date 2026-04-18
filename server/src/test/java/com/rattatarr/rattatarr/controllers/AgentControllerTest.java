package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.clients.agents.AgentMessage;
import com.rattatarr.rattatarr.exceptions.AgentExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.AgentChatRequestDTO;
import com.rattatarr.rattatarr.models.entities.AgentConversationMessage;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.services.AgentConversationService;
import com.rattatarr.rattatarr.services.ProfileAgentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    private final UUID profileId = UUID.randomUUID();
    @Mock
    private ProfileAgentService profileAgentService;
    @Mock
    private AgentConversationService conversationService;
    @InjectMocks
    private AgentController agentController;

    @Test
    void getSuggestions_shouldReturnNonEmptyList() {
        var result = agentController.getSuggestions();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().suggestions().isEmpty());
    }

    @Test
    void getPrompt_shouldReturnPromptAndAgent() {
        when(profileAgentService.buildContextPrompt(profileId)).thenReturn("You are a movie assistant...");

        var result = agentController.getPrompt(profileId, "ollama");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("You are a movie assistant...", result.getBody().prompt());
        assertEquals("ollama", result.getBody().agent());
    }

    @Test
    void getConversation_shouldReturnWrappedMessages() {
        Profile profile = new Profile("Alice", null);
        AgentConversationMessage msg = new AgentConversationMessage(profile, AgentMessage.Role.USER, "Hi", "ollama");
        when(conversationService.fetchHistory(profileId, "ollama")).thenReturn(List.of(msg));

        var result = agentController.getConversation(profileId, "ollama");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().conversation().size());
        assertEquals(AgentMessage.Role.USER, result.getBody().conversation().get(0).role());
        assertEquals("Hi", result.getBody().conversation().get(0).content());
    }

    @Test
    void getConversation_shouldReturnEmptyConversationWhenNoHistory() {
        when(conversationService.fetchHistory(profileId, "ollama")).thenReturn(List.of());

        var result = agentController.getConversation(profileId, "ollama");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().conversation().isEmpty());
    }

    @Test
    void chat_shouldReturnReplyFromService() {
        var request = new AgentChatRequestDTO(profileId, "What movie should I watch?");
        when(profileAgentService.chat(profileId, "ollama", "What movie should I watch?"))
                .thenReturn("I recommend Inception.");

        var result = agentController.chat(request, "ollama");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("I recommend Inception.", result.getBody().reply());
        assertEquals("ollama", result.getBody().agent());
    }

    @Test
    void chat_shouldUseDefaultAgentWhenNotSpecified() {
        var request = new AgentChatRequestDTO(profileId, "Any horror recommendations?");
        when(profileAgentService.chat(profileId, "ollama", "Any horror recommendations?"))
                .thenReturn("Try The Shining.");

        var result = agentController.chat(request, "ollama");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("ollama", result.getBody().agent());
    }

    @Test
    void chat_shouldPropagateAgentNotConfiguredException() {
        var request = new AgentChatRequestDTO(profileId, "Hello");
        when(profileAgentService.chat(profileId, "ollama", "Hello"))
                .thenThrow(new AgentExceptions.AgentNotConfiguredException("ollama"));

        assertThrows(AgentExceptions.AgentNotConfiguredException.class,
                () -> agentController.chat(request, "ollama"));
    }

    @Test
    void chat_shouldPropagateAgentNotFoundException() {
        var request = new AgentChatRequestDTO(profileId, "Hello");
        when(profileAgentService.chat(profileId, "gpt99", "Hello"))
                .thenThrow(new AgentExceptions.AgentNotFoundException("gpt99"));

        assertThrows(AgentExceptions.AgentNotFoundException.class,
                () -> agentController.chat(request, "gpt99"));
    }

    @Test
    void clearConversation_shouldReturnNoContent() {
        doNothing().when(profileAgentService).clearConversation(profileId, "ollama");

        var result = agentController.clearConversation(profileId, "ollama");

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(profileAgentService).clearConversation(profileId, "ollama");
    }
}
