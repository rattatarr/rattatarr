package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditCastMemberResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditsResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditCrewMemberResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.Person;
import com.rattatarr.rattatarr.repositories.PeopleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private PeopleRepository repository;
    @Mock
    private MediaItemCastService mediaItemCastService;
    @Mock
    private MediaItemCrewService mediaItemCrewService;

    @InjectMocks
    private PeopleService peopleService;

    private Person person;
    private MediaItem mediaItem;
    private TMDbCreditCastMemberResponseDTO castMember;
    private TMDbCreditCrewMemberResponseDTO crewMember;

    @BeforeEach
    void setUp() {
        person = new Person("John Doe", "12345", "/profile.jpg");
        mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        castMember = new TMDbCreditCastMemberResponseDTO("12345", "John Doe", "Actor", "/profile.jpg", 1);
        crewMember = new TMDbCreditCrewMemberResponseDTO("Jane Smith", "Director", "Director", "/profile2.jpg", "67890");
    }

    @Test
    void updateEntity_shouldUpdateNameAndProfilePath() {
        // Given
        Person existing = new Person("Old Name", "12345", "/old.jpg");
        Person updated = new Person("New Name", "12345", "/new.jpg");

        // When
        peopleService.updateEntity(existing, updated);

        // Then
        assertEquals("New Name", existing.name());
        assertEquals("/new.jpg", existing.profilePathUrl());
    }

    @Test
    void upsert_shouldCreateNewPersonWhenNotExists() {
        // Given
        when(repository.findByTMDbId("12345")).thenReturn(Optional.empty());
        when(repository.save(any(Person.class))).thenReturn(person);

        // When
        Person result = peopleService.upsert("12345", "John Doe", "/profile.jpg", false);

        // Then
        assertNotNull(result);
        verify(repository).findByTMDbId("12345");
        verify(repository).save(any(Person.class));
    }

    @Test
    void upsert_shouldReturnExistingPersonWhenForceRefreshFalse() {
        // Given
        when(repository.findByTMDbId("12345")).thenReturn(Optional.of(person));

        // When
        Person result = peopleService.upsert("12345", "John Doe", "/profile.jpg", false);

        // Then
        assertNotNull(result);
        assertEquals(person, result);
        verify(repository).findByTMDbId("12345");
        verify(repository, never()).save(any());
    }

    @Test
    void upsert_shouldUpdateExistingPersonWhenForceRefreshTrue() {
        // Given
        Person existingPerson = new Person("Old Name", "12345", "/old.jpg");
        when(repository.findByTMDbId("12345")).thenReturn(Optional.of(existingPerson));
        when(repository.save(any(Person.class))).thenReturn(existingPerson);

        // When
        Person result = peopleService.upsert("12345", "New Name", "/new.jpg", true);

        // Then
        assertNotNull(result);
        verify(repository).findByTMDbId("12345");
        verify(repository).save(existingPerson);
    }

    @Test
    void upsertBatchFromTMDbDTOs_shouldHandleEmptyCredits() {
        // Given
        TMDbCreditsResponseDTO emptyCredits = new TMDbCreditsResponseDTO(List.of(), List.of());

        // When
        peopleService.upsertBatchFromTMDbDTOs(emptyCredits, mediaItem);

        // Then
        verify(repository, never()).findAllByTMDbIdIn(anySet());
        verify(repository, never()).saveAll(anySet());
    }

    @Test
    void upsertBatchFromTMDbDTOs_shouldInsertNewPeopleAndLinkToCastAndCrew() {
        // Given
        TMDbCreditsResponseDTO credits = new TMDbCreditsResponseDTO(
                List.of(castMember),
                List.of(crewMember)
        );

        when(repository.findAllByTMDbIdIn(anySet())).thenReturn(Set.of());
        when(repository.saveAll(anySet())).thenReturn(List.of());

        // When
        peopleService.upsertBatchFromTMDbDTOs(credits, mediaItem);

        // Then
        verify(repository).findAllByTMDbIdIn(anySet());
        verify(repository).saveAll(anyIterable());
        verify(mediaItemCastService).upsertBatchFromTMDb(eq(credits.cast()), eq(mediaItem), anyMap());
        verify(mediaItemCrewService).upsertBatchFromTMDb(eq(credits.crew()), eq(mediaItem), anyMap());
    }

    @Test
    void upsertBatchFromTMDbDTOs_shouldSkipExistingPeople() {
        // Given
        Person existingCastPerson = new Person("John Doe", "12345", "/profile.jpg");
        TMDbCreditsResponseDTO credits = new TMDbCreditsResponseDTO(
                List.of(castMember),
                List.of(crewMember)
        );

        when(repository.findAllByTMDbIdIn(anySet())).thenReturn(Set.of(existingCastPerson));
        when(repository.saveAll(anyIterable())).thenReturn(List.of());

        // When
        peopleService.upsertBatchFromTMDbDTOs(credits, mediaItem);

        // Then
        verify(repository).findAllByTMDbIdIn(anySet());
        verify(repository).saveAll(anyIterable());
        verify(mediaItemCastService).upsertBatchFromTMDb(eq(credits.cast()), eq(mediaItem), anyMap());
        verify(mediaItemCrewService).upsertBatchFromTMDb(eq(credits.crew()), eq(mediaItem), anyMap());
    }

    @Test
    void upsertPeopleFromCredits_shouldProcessCastAndCrew() {
        // Given
        TMDbCreditsResponseDTO credits = new TMDbCreditsResponseDTO(
                List.of(castMember),
                List.of(crewMember)
        );

        when(repository.findByTMDbId(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Person.class))).thenReturn(person);

        // When
        peopleService.upsertPeopleFromCredits(credits, mediaItem, false);

        // Then
        verify(repository, times(2)).findByTMDbId(anyString());
        verify(repository, times(2)).save(any(Person.class));
        verify(mediaItemCastService).upsert(eq(mediaItem), eq(castMember), any(Person.class), eq(false));
        verify(mediaItemCrewService).upsert(eq(mediaItem), eq(crewMember), any(Person.class), eq(false));
    }
}
