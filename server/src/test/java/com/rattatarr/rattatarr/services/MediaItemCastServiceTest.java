package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditCastMemberResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemCast;
import com.rattatarr.rattatarr.models.entities.Person;
import com.rattatarr.rattatarr.repositories.MediaItemCastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaItemCastServiceTest {

    @Mock
    private MediaItemCastRepository repository;

    @InjectMocks
    private MediaItemCastService mediaItemCastService;

    private MediaItem mediaItem;
    private Person person;
    private TMDbCreditCastMemberResponseDTO castMember;

    @BeforeEach
    void setUp() {
        mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        person = new Person("John Doe", "12345", "/profile.jpg");
        castMember = new TMDbCreditCastMemberResponseDTO("12345", "John Doe", "Hero", "/profile.jpg", 1);
    }

    @Test
    void upsert_shouldCreateNewCastWhenNotExists() {
        // Given
        when(repository.findByMediaItemAndPersonAndCharacter(any(), any(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemCast.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItemCast result = mediaItemCastService.upsert(mediaItem, castMember, person, false);

        // Then
        assertNotNull(result);
        verify(repository, atLeastOnce()).findByMediaItemAndPersonAndCharacter(any(), any(), anyString());
        verify(repository, atLeastOnce()).save(any(MediaItemCast.class));
    }

    @Test
    void upsert_shouldReturnExistingCastWhenForceRefreshFalse() {
        // Given
        MediaItemCast existing = new MediaItemCast(mediaItem, person, "Hero", 1);
        when(repository.findByMediaItemAndPersonAndCharacter(any(), any(), anyString()))
                .thenReturn(Optional.of(existing));

        // When
        MediaItemCast result = mediaItemCastService.upsert(mediaItem, castMember, person, false);

        // Then
        assertNotNull(result);
        assertEquals(existing, result);
        verify(repository, never()).save(any());
    }

    @Test
    void upsert_shouldUpdateExistingCastWhenForceRefreshTrue() {
        // Given
        MediaItemCast existing = new MediaItemCast(mediaItem, person, "Hero", 5);
        when(repository.findByMediaItemAndPersonAndCharacter(any(), any(), anyString()))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(MediaItemCast.class))).thenReturn(existing);

        // When
        MediaItemCast result = mediaItemCastService.upsert(mediaItem, castMember, person, true);

        // Then
        assertNotNull(result);
        verify(repository).save(existing);
    }

    @Test
    void upsert_shouldCreateNewCast() {
        // Given
        MediaItemCast newCast = new MediaItemCast(mediaItem, person, "Hero", 1);
        when(repository.findByMediaItemAndPersonAndCharacter(any(), any(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemCast.class))).thenReturn(newCast);

        // When
        MediaItemCast result = mediaItemCastService.upsert(mediaItem, castMember, person, false);

        // Then
        assertNotNull(result);
        verify(repository, atLeastOnce()).findByMediaItemAndPersonAndCharacter(any(), any(), anyString());
    }

    @Test
    void upsertBatchFromTMDb_shouldHandleEmptyList() {
        // Given
        List<TMDbCreditCastMemberResponseDTO> emptyList = List.of();
        Map<String, Person> peopleMap = Map.of();

        // When
        mediaItemCastService.upsertBatchFromTMDb(emptyList, mediaItem, peopleMap);

        // Then
        verify(repository, never()).findByMediaItemId(any());
    }

    @Test
    void upsertBatchFromTMDb_shouldProcessCastMembers() {
        // Given
        when(repository.findByMediaItemId(any())).thenReturn(List.of());
        when(repository.saveAll(anyIterable())).thenReturn(List.of());

        Map<String, Person> peopleMap = Map.of("12345", person);
        List<TMDbCreditCastMemberResponseDTO> castMembers = List.of(castMember);

        // When/Then - method completes without error
        mediaItemCastService.upsertBatchFromTMDb(castMembers, mediaItem, peopleMap);

        verify(repository).findByMediaItemId(any());
    }
}
