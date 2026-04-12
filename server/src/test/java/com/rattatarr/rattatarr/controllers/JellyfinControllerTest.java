package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.exceptions.JellyfinClientExceptions;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfilesWrapper;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.services.JellyfinService;
import com.rattatarr.rattatarr.services.JellyfinTraversalService;
import com.rattatarr.rattatarr.services.MediaItemMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JellyfinControllerTest {

    @Mock
    private JellyfinService jellyfinService;
    @Mock
    private JellyfinTraversalService jellyfinTraversalService;
    @Mock
    private MediaItemMetadataService mediaItemMetadataService;
    @Mock
    private TMDbController tmDbController;

    @InjectMocks
    private JellyfinController jellyfinController;

    private ZoneId testZoneId;

    @BeforeEach
    void setUp() {
        testZoneId = ZoneId.of("UTC");
    }

    @Test
    void testConnection_shouldReturnSuccessWhenHealthy() {
        when(jellyfinService.testConnection()).thenReturn(true);

        GenericResponseDTO result = jellyfinController.testConnection();

        assertNotNull(result);
        assertTrue(result.message().contains("successful"));
        verify(jellyfinService).testConnection();
    }

    @Test
    void testConnection_shouldReturnFailureWhenUnhealthy() {
        when(jellyfinService.testConnection()).thenReturn(false);

        GenericResponseDTO result = jellyfinController.testConnection();

        assertNotNull(result);
        assertTrue(result.message().contains("failed"));
        verify(jellyfinService).testConnection();
    }

    @Test
    void testConnection_shouldHandleError() {
        when(jellyfinService.testConnection())
                .thenThrow(new JellyfinClientExceptions("Network error", HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(JellyfinClientExceptions.class, () ->
                jellyfinController.testConnection()
        );
        verify(jellyfinService).testConnection();
    }

    @Test
    void syncProfiles_shouldReturnSyncedProfiles() {
        Profile profile1 = createProfileWithTimestamps("User1", UUID.randomUUID().toString());
        Profile profile2 = createProfileWithTimestamps("User2", UUID.randomUUID().toString());
        List<Profile> profiles = List.of(profile1, profile2);

        when(jellyfinService.syncJellyfinUsersWithRattatarrProfiles())
                .thenReturn(profiles);

        ProfilesWrapper result = jellyfinController.syncProfiles(testZoneId);

        assertNotNull(result);
        assertEquals(2, result.profiles().size());
        verify(jellyfinService).syncJellyfinUsersWithRattatarrProfiles();
    }

    @Test
    void syncProfiles_shouldHandleEmptyProfilesList() {
        when(jellyfinService.syncJellyfinUsersWithRattatarrProfiles())
                .thenReturn(List.of());

        ProfilesWrapper result = jellyfinController.syncProfiles(testZoneId);

        assertNotNull(result);
        assertEquals(0, result.profiles().size());
        verify(jellyfinService).syncJellyfinUsersWithRattatarrProfiles();
    }

    @Test
    void syncProfiles_shouldHandleError() {
        when(jellyfinService.syncJellyfinUsersWithRattatarrProfiles())
                .thenThrow(new JellyfinClientExceptions("Sync failed", HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(JellyfinClientExceptions.class, () ->
                jellyfinController.syncProfiles(testZoneId)
        );
        verify(jellyfinService).syncJellyfinUsersWithRattatarrProfiles();
    }

    @Test
    void syncMedia_shouldTriggerAsyncSyncAndReturnImmediately() {
        GenericResponseDTO result = jellyfinController.syncMedia();

        assertNotNull(result);
        assertTrue(result.message().contains("started in background"));
        verify(jellyfinTraversalService).syncMediaAsync();
    }

    private Profile createProfileWithTimestamps(String name, String jellyfinId) {
        Profile profile = new Profile(name, jellyfinId);
        try {
            var createdAtField = profile.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(profile, java.time.Instant.now());

            var updatedAtField = profile.getClass().getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(profile, java.time.Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return profile;
    }
}
