package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.ProfilesFiltersDTO;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfilesRepository repository;

    @InjectMocks
    private ProfilesService service;

    private Profile testProfile;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testProfile = new Profile("Test Profile", UUID.randomUUID().toString());
    }

    @Test
    void testSaveValidProfile() {
        // Given
        when(repository.findOne(ArgumentMatchers.<Specification<Profile>>any())).thenReturn(Optional.empty());
        when(repository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Profile result = service.save(testProfile);

        // Then
        assertNotNull(result);
        verify(repository).save(testProfile);
    }

    @Test
    void testSaveProfileWithDuplicateName() {
        // Given
        when(repository.findOne(ArgumentMatchers.<Specification<Profile>>any())).thenReturn(Optional.of(testProfile));

        // When/Then
        assertThrows(ProfilesExceptions.DuplicateProfileNameExceptions.class,
                () -> service.save(testProfile));
    }

    @Test
    void testFindById() {
        // Given
        when(repository.findById(testId)).thenReturn(Optional.of(testProfile));

        // When
        Optional<Profile> result = service.findById(testId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProfile, result.get());
        verify(repository).findById(testId);
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        when(repository.findById(testId)).thenReturn(Optional.empty());

        // When
        Optional<Profile> result = service.findById(testId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        // Given
        List<Profile> profiles = List.of(testProfile, new Profile("Another", UUID.randomUUID().toString()));
        when(repository.findAll()).thenReturn(profiles);

        // When
        List<Profile> result = service.findAll();

        // Then
        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void testUpdate() {
        // Given
        Profile updated = new Profile("Updated Name", UUID.randomUUID().toString());
        when(repository.findById(testId)).thenReturn(Optional.of(testProfile));
        when(repository.findOne(ArgumentMatchers.<Specification<Profile>>any())).thenReturn(Optional.empty());
        when(repository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Optional<Profile> result = service.update(testId, updated);

        // Then
        assertTrue(result.isPresent());
        verify(repository).save(any(Profile.class));
    }

    @Test
    void testUpdateWithDuplicateName() {
        // Given
        Profile existing = new Profile("Existing", UUID.randomUUID().toString());
        Profile duplicate = new Profile("Duplicate", UUID.randomUUID().toString());
        Profile updated = new Profile("Duplicate", UUID.randomUUID().toString());

        when(repository.findById(testId)).thenReturn(Optional.of(existing));
        when(repository.findOne(ArgumentMatchers.<Specification<Profile>>any())).thenReturn(Optional.of(duplicate));

        // When/Then
        assertThrows(ProfilesExceptions.DuplicateProfileNameExceptions.class,
                () -> service.update(testId, updated));
    }

    @Test
    void testUpdateWithSameNameAllowed() {
        // Given
        Profile existing = new Profile("Same Name", UUID.randomUUID().toString());
        Profile updated = new Profile("Same Name", UUID.randomUUID().toString());

        when(repository.findById(testId)).thenReturn(Optional.of(existing));
        when(repository.save(any(Profile.class))).thenReturn(existing);

        // When
        Optional<Profile> result = service.update(testId, updated);

        // Then
        assertTrue(result.isPresent());
        verify(repository).save(any(Profile.class));
    }

    @Test
    void testDelete() {
        // Given
        when(repository.findById(testId)).thenReturn(Optional.of(testProfile));
        when(repository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        boolean result = service.delete(testId);

        // Then
        assertTrue(result);
        verify(repository).save(any(Profile.class));
    }

    @Test
    void testDeleteNotFound() {
        // Given
        when(repository.findById(testId)).thenReturn(Optional.empty());

        // When
        boolean result = service.delete(testId);

        // Then
        assertFalse(result);
        verify(repository, never()).save(any());
    }

    @Test
    void testFilterProfiles() {
        // Given
        ProfilesFiltersDTO filters = new ProfilesFiltersDTO(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20);
        ZoneId zoneId = ZoneId.of("UTC");

        List<Profile> profiles = List.of(testProfile);
        Page<Profile> page = new PageImpl<>(profiles, pageable, 1);

        when(repository.findAll(ArgumentMatchers.<Specification<Profile>>any(), any(Pageable.class))).thenReturn(page);

        // When
        Page<Profile> result = service.filterProfiles(filters, zoneId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(ArgumentMatchers.<Specification<Profile>>any(), eq(pageable));
    }

    @Test
    void testFilterProfilesWithNameFilter() {
        // Given
        ProfilesFiltersDTO filters = new ProfilesFiltersDTO("Test", null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20);
        ZoneId zoneId = ZoneId.of("UTC");

        Page<Profile> page = new PageImpl<>(List.of(testProfile), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<Profile>>any(), any(Pageable.class))).thenReturn(page);

        // When
        Page<Profile> result = service.filterProfiles(filters, zoneId, pageable);

        // Then
        assertNotNull(result);
        verify(repository).findAll(ArgumentMatchers.<Specification<Profile>>any(), any(Pageable.class));
    }

    @Test
    void testFilterProfilesWithEmptyResult() {
        // Given
        ProfilesFiltersDTO filters = new ProfilesFiltersDTO(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20);
        ZoneId zoneId = ZoneId.of("UTC");

        Page<Profile> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<Profile>>any(), any(Pageable.class))).thenReturn(emptyPage);

        // When
        Page<Profile> result = service.filterProfiles(filters, zoneId, pageable);

        // Then
        assertTrue(result.isEmpty());
    }
}
