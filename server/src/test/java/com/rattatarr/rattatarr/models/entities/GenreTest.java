package com.rattatarr.rattatarr.models.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenreTest {

    @Test
    void constructor_shouldCreateGenre() {
        // Given & When
        Genre genre = new Genre("Action");

        // Then
        assertEquals("Action", genre.name());
    }

    @Test
    void setName_shouldUpdateName() {
        // Given
        Genre genre = new Genre("Action");

        // When
        genre.setName("Drama");

        // Then
        assertEquals("Drama", genre.name());
    }

    @Test
    void constructor_shouldAcceptNullName() {
        // Given & When
        Genre genre = new Genre(null);

        // Then
        assertNull(genre.name());
    }
}
