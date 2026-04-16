package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.agents.AgentClient;
import com.rattatarr.rattatarr.clients.agents.AgentMessage;
import com.rattatarr.rattatarr.exceptions.AgentExceptions;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import com.rattatarr.rattatarr.specifications.StatisticsSpecifications;
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
    private MediaItemRatingsService mediaItemRatingsService;
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
                mediaItemRatingsService,
                conversationService,
                List.of(ollamaClient));
    }

    private void stubEmptyRatingContext(Profile p, UUID id) {
        when(mediaItemRatingsService.findAllRatedByProfileRecentFirst(p)).thenReturn(List.of());
        when(mediaItemRatingsService.findTop15ByProfileOrderByRatingDesc(p)).thenReturn(List.of());
        when(mediaItemRatingsService.findTopGenresByWatchCount(eq(id), anyInt())).thenReturn(List.of());
        when(mediaItemRatingsService.findTopGenresByAverageRating(eq(id), anyFloat(), anyInt())).thenReturn(List.of());
        when(mediaItemRatingsService.findTopActors(eq(id), anyInt(), anyInt(), any(StatisticsSpecifications.SortBy.class))).thenReturn(List.of());
        when(mediaItemRatingsService.findTopDirectors(eq(id), anyInt(), anyInt(), any(StatisticsSpecifications.SortBy.class))).thenReturn(List.of());
        when(mediaItemRatingsService.findTopProducers(eq(id), anyInt(), anyInt(), any(StatisticsSpecifications.SortBy.class))).thenReturn(List.of());
    }

    @Test
    void chat_shouldReturnAgentReply() {
        when(profilesRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(ollamaClient.isConfigured()).thenReturn(true);
        when(conversationService.loadHistory(profileId, "ollama")).thenReturn(List.of());
        stubEmptyRatingContext(profile, profileId);
        when(ollamaClient.chat(anyString(), anyList(), eq("Recommend something"))).thenReturn("Watch Parasite.");
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
        stubEmptyRatingContext(profile, profileId);
        when(ollamaClient.chat(anyString(), eq(history), eq("New question"))).thenReturn("An answer.");
        doNothing().when(conversationService).saveExchange(any(), anyString(), anyString(), anyString());

        String result = service.chat(profileId, "ollama", "New question");

        assertEquals("An answer.", result);
        verify(ollamaClient).chat(anyString(), eq(history), eq("New question"));
    }

    @Test
    void clearConversation_shouldDelegateToConversationService() {
        when(profilesRepository.findById(profileId)).thenReturn(Optional.of(profile));
        doNothing().when(conversationService).clearHistory(profileId, "ollama");

        service.clearConversation(profileId, "ollama");

        verify(conversationService).clearHistory(profileId, "ollama");
    }
}
