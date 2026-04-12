package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditCrewMemberResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemCrew;
import com.rattatarr.rattatarr.models.entities.Person;
import com.rattatarr.rattatarr.repositories.MediaItemCrewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaItemCrewServiceTest {

    @Mock
    private MediaItemCrewRepository repository;

    @InjectMocks
    private MediaItemCrewService mediaItemCrewService;

    private MediaItem mediaItem;
    private Person person;
    private TMDbCreditCrewMemberResponseDTO crewMember;

    @BeforeEach
    void setUp() {
        mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        person = new Person("Jane Smith", "67890", "/profile.jpg");
        crewMember = new TMDbCreditCrewMemberResponseDTO("Jane Smith", "Director", "Director", "/profile.jpg", "67890");
    }

    @Test
    void upsert_shouldCreateNewCrewWhenNotExists() {
        // Given
        when(repository.findByMediaItemAndPersonAndJobAndDepartment(mediaItem, person, "Director", "Director"))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemCrew.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItemCrew result = mediaItemCrewService.upsert(mediaItem, crewMember, person, false);

        // Then
        assertNotNull(result);
        verify(repository).findByMediaItemAndPersonAndJobAndDepartment(mediaItem, person, "Director", "Director");
        verify(repository).save(any(MediaItemCrew.class));
    }

    @Test
    void upsert_shouldReturnExistingCrewWhenForceRefreshFalse() {
        // Given
        MediaItemCrew existing = new MediaItemCrew(mediaItem, person, "Director", "Director");
        when(repository.findByMediaItemAndPersonAndJobAndDepartment(mediaItem, person, "Director", "Director"))
                .thenReturn(Optional.of(existing));

        // When
        MediaItemCrew result = mediaItemCrewService.upsert(mediaItem, crewMember, person, false);

        // Then
        assertNotNull(result);
        assertEquals(existing, result);
        verify(repository, never()).save(any());
    }

    @Test
    void upsert_shouldUpdateExistingCrewWhenForceRefreshTrue() {
        // Given
        MediaItemCrew existing = new MediaItemCrew(mediaItem, person, "Director", "Director");
        when(repository.findByMediaItemAndPersonAndJobAndDepartment(mediaItem, person, "Director", "Director"))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(MediaItemCrew.class))).thenReturn(existing);

        // When
        MediaItemCrew result = mediaItemCrewService.upsert(mediaItem, crewMember, person, true);

        // Then
        assertNotNull(result);
        verify(repository).save(existing);
    }

    @Test
    void upsert_shouldCreateNewCrew() {
        // Given
        MediaItemCrew newCrew = new MediaItemCrew(mediaItem, person, "Director", "Director");
        when(repository.findByMediaItemAndPersonAndJobAndDepartment(mediaItem, person, "Director", "Director"))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemCrew.class))).thenReturn(newCrew);

        // When
        mediaItemCrewService.upsert(mediaItem, crewMember, person, false);

        // Then
        verify(repository).findByMediaItemAndPersonAndJobAndDepartment(mediaItem, person, "Director", "Director");
    }

    @Test
    void upsertBatchFromTMDb_shouldHandleEmptyList() {
        // Given
        List<TMDbCreditCrewMemberResponseDTO> emptyList = List.of();
        Map<String, Person> peopleMap = Map.of();

        // When
        mediaItemCrewService.upsertBatchFromTMDb(emptyList, mediaItem, peopleMap);

        // Then
        verify(repository, never()).findByMediaItemId(any());
    }

    @Test
    void upsertBatchFromTMDb_shouldInsertNewCrew() {
        // Given
        when(repository.findByMediaItemId(any())).thenReturn(List.of());
        when(repository.saveAll(anyIterable())).thenReturn(List.of());

        Map<String, Person> peopleMap = Map.of("67890", person);
        List<TMDbCreditCrewMemberResponseDTO> crewMembers = List.of(crewMember);

        // When
        mediaItemCrewService.upsertBatchFromTMDb(crewMembers, mediaItem, peopleMap);

        // Then
        verify(repository).findByMediaItemId(any());
        verify(repository).saveAll(anyIterable());
    }
}
