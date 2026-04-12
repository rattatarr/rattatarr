package com.rattatarr.rattatarr.services.helpers;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.models.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaItemViewHelperTest {

    @Mock
    private TMDbClient tmdbClient;

    private MediaItemViewHelper helper;

    @BeforeEach
    void setUp() {
        helper = new MediaItemViewHelper(tmdbClient);
    }

    @Test
    void initializeCredits_shouldNotInitializeWhenFlagIsFalse() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);

        // When
        helper.initializeCredits(mediaItem, false);

        // Then
        verify(mediaItem, never()).cast();
        verify(mediaItem, never()).crew();
    }

    @Test
    void initializeCredits_shouldInitializeCastAndCrewWhenFlagIsTrue() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);

        MediaItemCast cast1 = mock(MediaItemCast.class);
        Person person1 = mock(Person.class);
        when(cast1.person()).thenReturn(person1);

        MediaItemCrew crew1 = mock(MediaItemCrew.class);
        Person person2 = mock(Person.class);
        when(crew1.person()).thenReturn(person2);

        Set<MediaItemCast> castSet = Set.of(cast1);
        Set<MediaItemCrew> crewSet = Set.of(crew1);

        when(mediaItem.cast()).thenReturn(castSet);
        when(mediaItem.crew()).thenReturn(crewSet);

        // When
        helper.initializeCredits(mediaItem, true);

        // Then
        verify(mediaItem, times(2)).cast();
        verify(mediaItem, times(2)).crew();
        verify(cast1).person();
        verify(crew1).person();
    }

    @Test
    void applyImageUrls_shouldApplyUrlsToMetadata() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);
        MediaItemMetadata metadata = mock(MediaItemMetadata.class);

        when(mediaItem.metadata()).thenReturn(metadata);
        when(metadata.posterImageUrl()).thenReturn("/poster.jpg");
        when(metadata.backdropImageUrl()).thenReturn("/backdrop.jpg");

        when(tmdbClient.getImageUrl("/poster.jpg", "w500")).thenReturn("https://image.tmdb.org/t/p/w500/poster.jpg");
        when(tmdbClient.getImageUrl("/backdrop.jpg", "w1280")).thenReturn("https://image.tmdb.org/t/p/w1280/backdrop.jpg");

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", false, false);

        // Then
        verify(metadata).setPosterImageUrl("https://image.tmdb.org/t/p/w500/poster.jpg");
        verify(metadata).setBackdropImageUrl("https://image.tmdb.org/t/p/w1280/backdrop.jpg");
        verify(tmdbClient).getImageUrl("/poster.jpg", "w500");
        verify(tmdbClient).getImageUrl("/backdrop.jpg", "w1280");
    }

    @Test
    void applyImageUrls_shouldHandleNullMetadata() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);

        when(mediaItem.metadata()).thenReturn(null);

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", false, false);

        // Then
        verify(mediaItem).metadata();
        verifyNoInteractions(tmdbClient);
    }

    @Test
    void applyImageUrls_shouldApplyUrlsToSeasonsWhenFlagIsTrue() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);
        MediaItemMetadata metadata = mock(MediaItemMetadata.class);

        MediaSeason season1 = mock(MediaSeason.class);
        MediaSeasonMetadata seasonMetadata = mock(MediaSeasonMetadata.class);

        when(season1.metadata()).thenReturn(seasonMetadata);
        when(seasonMetadata.posterImageUrl()).thenReturn("/season_poster.jpg");

        Set<MediaSeason> seasons = Set.of(season1);

        when(mediaItem.metadata()).thenReturn(metadata);
        when(metadata.posterImageUrl()).thenReturn(null);
        when(metadata.backdropImageUrl()).thenReturn(null);
        when(mediaItem.seasons()).thenReturn(seasons);
        when(mediaItem.cast()).thenReturn(new HashSet<>());
        when(mediaItem.crew()).thenReturn(new HashSet<>());

        when(tmdbClient.getImageUrl("/season_poster.jpg", "w500")).thenReturn("https://image.tmdb.org/t/p/w500/season_poster.jpg");

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", true, false);

        // Then
        verify(seasonMetadata).setPosterImageUrl("https://image.tmdb.org/t/p/w500/season_poster.jpg");
        verify(tmdbClient).getImageUrl("/season_poster.jpg", "w500");
    }

    @Test
    void applyImageUrls_shouldNotApplyUrlsToSeasonsWhenFlagIsFalse() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);
        MediaItemMetadata metadata = mock(MediaItemMetadata.class);

        when(mediaItem.metadata()).thenReturn(metadata);
        when(metadata.posterImageUrl()).thenReturn(null);
        when(metadata.backdropImageUrl()).thenReturn(null);

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", false, false);

        // Then
        verify(mediaItem, never()).seasons();
    }

    @Test
    void applyImageUrls_shouldApplyUrlsToCreditsWhenFlagIsTrue() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);
        MediaItemMetadata metadata = mock(MediaItemMetadata.class);

        MediaItemCast cast1 = mock(MediaItemCast.class);
        Person castPerson = mock(Person.class);
        when(cast1.person()).thenReturn(castPerson);
        when(castPerson.profilePathUrl()).thenReturn("/profile.jpg");

        MediaItemCrew crew1 = mock(MediaItemCrew.class);
        Person crewPerson = mock(Person.class);
        when(crew1.person()).thenReturn(crewPerson);
        when(crewPerson.profilePathUrl()).thenReturn("/crew_profile.jpg");

        Set<MediaItemCast> castSet = Set.of(cast1);
        Set<MediaItemCrew> crewSet = Set.of(crew1);

        when(mediaItem.metadata()).thenReturn(metadata);
        when(metadata.posterImageUrl()).thenReturn(null);
        when(metadata.backdropImageUrl()).thenReturn(null);
        when(mediaItem.seasons()).thenReturn(new HashSet<>());
        when(mediaItem.cast()).thenReturn(castSet);
        when(mediaItem.crew()).thenReturn(crewSet);

        when(tmdbClient.getImageUrl("/profile.jpg", "w185")).thenReturn("https://image.tmdb.org/t/p/w185/profile.jpg");
        when(tmdbClient.getImageUrl("/crew_profile.jpg", "w185")).thenReturn("https://image.tmdb.org/t/p/w185/crew_profile.jpg");

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", false, true);

        // Then
        verify(castPerson).setProfilePathUrl("https://image.tmdb.org/t/p/w185/profile.jpg");
        verify(crewPerson).setProfilePathUrl("https://image.tmdb.org/t/p/w185/crew_profile.jpg");
        verify(tmdbClient).getImageUrl("/profile.jpg", "w185");
        verify(tmdbClient).getImageUrl("/crew_profile.jpg", "w185");
    }

    @Test
    void applyImageUrls_shouldNotApplyUrlsToCreditsWhenFlagIsFalse() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);
        MediaItemMetadata metadata = mock(MediaItemMetadata.class);

        when(mediaItem.metadata()).thenReturn(metadata);
        when(metadata.posterImageUrl()).thenReturn(null);
        when(metadata.backdropImageUrl()).thenReturn(null);

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", false, false);

        // Then
        verify(mediaItem, never()).cast();
        verify(mediaItem, never()).crew();
    }

    @Test
    void applyImageUrls_shouldHandleNullPosterAndBackdropUrls() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);
        MediaItemMetadata metadata = mock(MediaItemMetadata.class);

        when(mediaItem.metadata()).thenReturn(metadata);
        when(metadata.posterImageUrl()).thenReturn(null);
        when(metadata.backdropImageUrl()).thenReturn(null);

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", false, false);

        // Then
        verify(metadata, never()).setPosterImageUrl(any());
        verify(metadata, never()).setBackdropImageUrl(any());
        verifyNoInteractions(tmdbClient);
    }

    @Test
    void applyImageUrls_shouldHandleNullPersonInCast() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);
        MediaItemMetadata metadata = mock(MediaItemMetadata.class);

        MediaItemCast cast1 = mock(MediaItemCast.class);
        when(cast1.person()).thenReturn(null);

        Set<MediaItemCast> castSet = Set.of(cast1);

        when(mediaItem.metadata()).thenReturn(metadata);
        when(metadata.posterImageUrl()).thenReturn(null);
        when(metadata.backdropImageUrl()).thenReturn(null);
        when(mediaItem.cast()).thenReturn(castSet);
        when(mediaItem.crew()).thenReturn(new HashSet<>());

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", false, true);

        // Then
        verifyNoInteractions(tmdbClient);
    }

    @Test
    void applyImageUrls_shouldHandleNullProfilePathUrl() {
        // Given
        MediaItem mediaItem = mock(MediaItem.class);
        MediaItemMetadata metadata = mock(MediaItemMetadata.class);

        MediaItemCast cast1 = mock(MediaItemCast.class);
        Person person = mock(Person.class);
        when(cast1.person()).thenReturn(person);
        when(person.profilePathUrl()).thenReturn(null);

        Set<MediaItemCast> castSet = Set.of(cast1);

        when(mediaItem.metadata()).thenReturn(metadata);
        when(metadata.posterImageUrl()).thenReturn(null);
        when(metadata.backdropImageUrl()).thenReturn(null);
        when(mediaItem.cast()).thenReturn(castSet);
        when(mediaItem.crew()).thenReturn(new HashSet<>());

        // When
        helper.applyImageUrls(mediaItem, "w500", "w1280", "w185", false, true);

        // Then
        verify(person, never()).setProfilePathUrl(any());
        verifyNoInteractions(tmdbClient);
    }

    @Test
    void buildUrlFromPath_shouldDelegateToTMDbClient() {
        // Given
        String path = "/image.jpg";
        String size = "w500";
        String expectedUrl = "https://image.tmdb.org/t/p/w500/image.jpg";

        when(tmdbClient.getImageUrl(path, size)).thenReturn(expectedUrl);

        // When
        String result = helper.buildUrlFromPath(path, size);

        // Then
        assertEquals(expectedUrl, result);
        verify(tmdbClient).getImageUrl(path, size);
    }
}
