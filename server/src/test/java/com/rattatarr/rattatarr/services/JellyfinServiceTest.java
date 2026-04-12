package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.jellyfin.JellyfinClient;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientUserResponseDTO;
import com.rattatarr.rattatarr.models.entities.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JellyfinServiceTest {

    @Mock
    private JellyfinClient jellyfinClient;
    @Mock
    private ProfilesService profilesService;

    @InjectMocks
    private JellyfinService jellyfinService;

    private JellyfinClientUserResponseDTO user1;
    private JellyfinClientUserResponseDTO user2;

    @BeforeEach
    void setUp() {
        user1 = new JellyfinClientUserResponseDTO("John Doe", "user-1", "2023-01-01", "2023-01-02");
        user2 = new JellyfinClientUserResponseDTO("Jane Smith", "user-2", "2023-01-01", "2023-01-02");
    }

    @Test
    void testConnection_shouldDelegateToClient() {
        when(jellyfinClient.testConnection()).thenReturn(true);

        boolean result = jellyfinService.testConnection();

        assertTrue(result);
        verify(jellyfinClient).testConnection();
    }

    @Test
    void syncJellyfinUsersWithRattatarrProfiles_shouldSyncSuccessfully() {
        List<JellyfinClientUserResponseDTO> users = List.of(user1, user2);
        Profile profile1 = new Profile("John Doe", "user-1");
        Profile profile2 = new Profile("Jane Smith", "user-2");

        when(jellyfinClient.getUsers()).thenReturn(users);
        when(profilesService.syncProfilesWithJellyfin(anyList())).thenReturn(List.of(profile1, profile2));

        List<Profile> result = jellyfinService.syncJellyfinUsersWithRattatarrProfiles();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jellyfinClient).getUsers();
        verify(profilesService).syncProfilesWithJellyfin(anyList());
    }

    @Test
    void syncJellyfinUsersWithRattatarrProfiles_shouldMapUsersToProfiles() {
        List<JellyfinClientUserResponseDTO> users = List.of(user1);
        Profile mappedProfile = new Profile("John Doe", "user-1");

        when(jellyfinClient.getUsers()).thenReturn(users);
        when(profilesService.syncProfilesWithJellyfin(anyList())).thenReturn(List.of(mappedProfile));

        List<Profile> result = jellyfinService.syncJellyfinUsersWithRattatarrProfiles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).name());
        assertEquals("user-1", result.get(0).jellyfinId());
    }

    @Test
    void syncJellyfinUsersWithRattatarrProfiles_shouldHandleEmptyUserList() {
        when(jellyfinClient.getUsers()).thenReturn(List.of());
        when(profilesService.syncProfilesWithJellyfin(anyList())).thenReturn(List.of());

        List<Profile> result = jellyfinService.syncJellyfinUsersWithRattatarrProfiles();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(jellyfinClient).getUsers();
        verify(profilesService).syncProfilesWithJellyfin(anyList());
    }
}
