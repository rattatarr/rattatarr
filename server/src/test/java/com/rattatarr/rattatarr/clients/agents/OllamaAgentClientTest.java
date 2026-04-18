package com.rattatarr.rattatarr.clients.agents;

import com.rattatarr.rattatarr.clients.agents.ollama.OllamaAgentClient;
import com.rattatarr.rattatarr.clients.agents.ollama.OllamaConfig;
import com.rattatarr.rattatarr.exceptions.AgentExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OllamaAgentClientTest {

    @Mock
    private OllamaConfig config;

    private OllamaAgentClient client;

    @BeforeEach
    void setUp() {
        client = new OllamaAgentClient(config);
    }

    @Test
    void name_shouldReturnOllama() {
        assertEquals("ollama", client.name());
    }

    @Test
    void isConfigured_shouldDelegateToConfig() {
        when(config.isConfigured()).thenReturn(true);
        assertTrue(client.isConfigured());

        when(config.isConfigured()).thenReturn(false);
        assertFalse(client.isConfigured());
    }

    @Test
    void chat_shouldThrowWhenNotConfigured() {
        doThrow(new AgentExceptions.AgentNotConfiguredException("ollama"))
                .when(config).throwIfNotConfigured();

        assertThrows(AgentExceptions.AgentNotConfiguredException.class,
                () -> client.chat("system", List.of(), "hello"));
    }
}
