package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.CreateProfileRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.ProfilesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfileWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfilesWrapper;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.services.ProfilesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfilesService profilesService;

    @InjectMocks
    private ProfilesController profilesController;

    private ZoneId testZoneId;
    private UUID testProfileId;
    private String testJellyfinId;

    @BeforeEach
    void setUp() {
        testZoneId = ZoneId.of("UTC");
        testProfileId = UUID.randomUUID();
        testJellyfinId = UUID.randomUUID().toString();
    }

    @Test
    void testCreateProfile() {
        // Given
        String profileName = "Test Profile";
        CreateProfileRequestDTO requestDTO = new CreateProfileRequestDTO(profileName, testJellyfinId);
        Profile savedProfile = createTestProfile(profileName, testJellyfinId);
        when(profilesService.save(any(Profile.class))).thenReturn(savedProfile);

        // When
        ResponseEntity<ProfileWrapper> response = profilesController.createProfile(requestDTO, testZoneId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Profile", response.getBody().profile().name());
        assertEquals(testJellyfinId, response.getBody().profile().jellyfinId());
        verify(profilesService).save(any(Profile.class));
    }

    @Test
    void testGetAllProfiles() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        ProfilesFiltersDTO filters = new ProfilesFiltersDTO(null, null, null, null, null);

        Profile profile1 = createTestProfile();
        Profile profile2 = createTestProfile();
        Page<Profile> profilesPage = new PageImpl<>(List.of(profile1, profile2), pageable, 2);

        when(profilesService.filterProfiles(any(), any(), any())).thenReturn(profilesPage);

        // When
        ResponseEntity<ProfilesWrapper> response = profilesController.getAllProfiles(pageable, testZoneId, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().profiles().size());
        assertNotNull(response.getBody().pagination());
        assertEquals(0, response.getBody().pagination().currentPage());
        assertEquals(20, response.getBody().pagination().pageSize());
        verify(profilesService).filterProfiles(filters, testZoneId, pageable);
    }

    @Test
    void testGetAllProfilesWithEmptyResult() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        ProfilesFiltersDTO filters = new ProfilesFiltersDTO(null, null, null, null, null);
        Page<Profile> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(profilesService.filterProfiles(any(), any(), any())).thenReturn(emptyPage);

        // When
        ResponseEntity<ProfilesWrapper> response = profilesController.getAllProfiles(pageable, testZoneId, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().profiles().isEmpty());
    }

    @Test
    void testGetProfileById() {
        // Given
        Profile profile = createTestProfile();
        when(profilesService.findById(testProfileId)).thenReturn(Optional.of(profile));

        // When
        ResponseEntity<ProfileWrapper> response = profilesController.getProfileById(testProfileId, testZoneId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Profile", response.getBody().profile().name());
        verify(profilesService).findById(testProfileId);
    }

    @Test
    void testGetProfileByIdNotFound() {
        // Given
        when(profilesService.findById(testProfileId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ProfilesExceptions.ProfileNotFoundExceptions.class,
                () -> profilesController.getProfileById(testProfileId, testZoneId));
        verify(profilesService).findById(testProfileId);
    }

    @Test
    void testCreateProfileWithNullJellyfinId() {
        // Given
        String profileName = "Test Profile";
        CreateProfileRequestDTO requestDTO = new CreateProfileRequestDTO(profileName, null);
        Profile savedProfile = createTestProfile();
        savedProfile.setJellyfinId(null);
        when(profilesService.save(any(Profile.class))).thenReturn(savedProfile);

        // When
        ResponseEntity<ProfileWrapper> response = profilesController.createProfile(requestDTO, testZoneId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().profile().jellyfinId());
    }

    @Test
    void testGetAllProfilesWithDifferentTimezone() {
        // Given
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        Pageable pageable = PageRequest.of(0, 20);
        ProfilesFiltersDTO filters = new ProfilesFiltersDTO(null, null, null, null, null);

        Profile profile = createTestProfile();
        Page<Profile> profilesPage = new PageImpl<>(List.of(profile), pageable, 1);

        when(profilesService.filterProfiles(any(), any(), any())).thenReturn(profilesPage);

        // When
        ResponseEntity<ProfilesWrapper> response = profilesController.getAllProfiles(pageable, tokyoZone, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().profiles().size());
        assertEquals(tokyoZone, response.getBody().profiles().getFirst().createdAt().getZone());
    }

    private Profile createTestProfile() {
        Profile profile = new Profile("Test Profile", testJellyfinId);
        // Use reflection or create a custom constructor for testing
        try {
            var idField = profile.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(profile, testProfileId);

            var createdAtField = profile.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(profile, Instant.now());

            var updatedAtField = profile.getClass().getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(profile, Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return profile;
    }

    private Profile createTestProfile(String name, String jellyfinId) {
        Profile profile = new Profile(name, jellyfinId);
        // Use reflection or create a custom constructor for testing
        try {
            var idField = profile.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(profile, testProfileId);

            var createdAtField = profile.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(profile, Instant.now());

            var updatedAtField = profile.getClass().getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(profile, Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return profile;
    }
}
