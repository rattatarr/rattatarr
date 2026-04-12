package com.rattatarr.rattatarr.models;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IMDbWatchlistRowTest {

    @Test
    void testIsImportableWithValidMovieData() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                8.5f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsImportableWithValidTVSeriesData() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0944947",
                9.0f,
                "TV Series",
                Instant.now(),
                MediaType.SERIES
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsImportableWithValidTVMiniSeriesData() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0185906",
                7.5f,
                "TV Mini Series",
                Instant.now(),
                MediaType.SERIES
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsImportableWithNullIMDbId() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                null,
                8.5f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsImportableWithEmptyIMDbId() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "",
                8.5f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsImportableWithBlankIMDbId() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "   ",
                8.5f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsImportableWithNullRating() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                null,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsImportableWithRatingBelowMinimum() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                0.5f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsImportableWithRatingAtMinimum() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                1.0f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsImportableWithRatingAboveMaximum() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                10.5f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsImportableWithRatingAtMaximum() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                10.0f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsImportableWithNullItemType() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                8.5f,
                null,
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsImportableWithUnsupportedItemType() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                8.5f,
                "Video Game",
                Instant.now(),
                null
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsImportableWithMovieCaseInsensitive() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                8.5f,
                "movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsImportableWithSeriesCaseInsensitive() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0944947",
                9.0f,
                "tv series",
                Instant.now(),
                MediaType.SERIES
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsImportableWithNullDateRated() {
        // Given - dateRated is nullable, should still be importable
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                8.5f,
                "Movie",
                null,
                MediaType.MOVIE
        );

        // When
        boolean result = row.isImportable();

        // Then
        assertTrue(result);
    }

    @Test
    void testRecordConstructor() {
        // Given
        String imdbId = "tt0468569";
        Float rating = 8.5f;
        String itemType = "Movie";
        Instant dateRated = Instant.now();
        MediaType mediaType = MediaType.MOVIE;

        // When
        IMDbWatchlistRow row = new IMDbWatchlistRow(imdbId, rating, itemType, dateRated, mediaType);

        // Then
        assertEquals(imdbId, row.IMDbId());
        assertEquals(rating, row.rating());
        assertEquals(itemType, row.itemType());
        assertEquals(dateRated, row.dateRated());
        assertEquals(mediaType, row.mediaType());
    }

    @Test
    void testRecordEquality() {
        // Given
        Instant instant = Instant.now();
        IMDbWatchlistRow row1 = new IMDbWatchlistRow("tt0468569", 8.5f, "Movie", instant, MediaType.MOVIE);
        IMDbWatchlistRow row2 = new IMDbWatchlistRow("tt0468569", 8.5f, "Movie", instant, MediaType.MOVIE);

        // Then
        assertEquals(row1, row2);
        assertEquals(row1.hashCode(), row2.hashCode());
    }

    @Test
    void testRecordInequality() {
        // Given
        Instant instant = Instant.now();
        IMDbWatchlistRow row1 = new IMDbWatchlistRow("tt0468569", 8.5f, "Movie", instant, MediaType.MOVIE);
        IMDbWatchlistRow row2 = new IMDbWatchlistRow("tt0468569", 9.0f, "Movie", instant, MediaType.MOVIE);

        // Then
        assertNotEquals(row1, row2);
    }

    @Test
    void testRecordToString() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow("tt0468569", 8.5f, "Movie", Instant.now(), MediaType.MOVIE);

        // When
        String toString = row.toString();

        // Then
        assertTrue(toString.contains("tt0468569"));
        assertTrue(toString.contains("8.5"));
        assertTrue(toString.contains("Movie"));
    }

    @Test
    void testMediaTypeMovie() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                8.5f,
                "Movie",
                Instant.now(),
                MediaType.MOVIE
        );

        // Then
        assertEquals(MediaType.MOVIE, row.mediaType());
    }

    @Test
    void testMediaTypeSeries() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0944947",
                9.0f,
                "TV Series",
                Instant.now(),
                MediaType.SERIES
        );

        // Then
        assertEquals(MediaType.SERIES, row.mediaType());
    }

    @Test
    void testMediaTypeNull() {
        // Given
        IMDbWatchlistRow row = new IMDbWatchlistRow(
                "tt0468569",
                8.5f,
                "Video Game",
                Instant.now(),
                null
        );

        // Then
        assertNull(row.mediaType());
    }
}
