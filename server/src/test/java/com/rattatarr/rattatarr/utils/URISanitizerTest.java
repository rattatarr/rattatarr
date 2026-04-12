package com.rattatarr.rattatarr.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class URISanitizerTest {

    @Test
    void removeTrailingSlash_shouldRemoveSlashFromUrl() {
        // Given
        String url = "https://example.com/";

        // When
        String result = URISanitizer.removeTrailingSlash(url);

        // Then
        assertEquals("https://example.com", result);
    }

    @Test
    void removeTrailingSlash_shouldReturnSameUrlWhenNoTrailingSlash() {
        // Given
        String url = "https://example.com";

        // When
        String result = URISanitizer.removeTrailingSlash(url);

        // Then
        assertEquals("https://example.com", result);
    }

    @Test
    void removeTrailingSlash_shouldHandleNull() {
        // Given
        String url = null;

        // When
        String result = URISanitizer.removeTrailingSlash(url);

        // Then
        assertNull(result);
    }

    @Test
    void removeTrailingSlash_shouldHandleEmptyString() {
        // Given
        String url = "";

        // When
        String result = URISanitizer.removeTrailingSlash(url);

        // Then
        assertEquals("", result);
    }

    @Test
    void removeTrailingSlash_shouldHandleMultipleTrailingSlashes() {
        // Given
        String url = "https://example.com/path/";

        // When
        String result = URISanitizer.removeTrailingSlash(url);

        // Then
        assertEquals("https://example.com/path", result);
    }

    @Test
    void pathEnsureLeadingSlash_shouldAddLeadingSlash() {
        // Given
        String path = "api/endpoint";

        // When
        String result = URISanitizer.pathEnsureLeadingSlash(path);

        // Then
        assertEquals("/api/endpoint", result);
    }

    @Test
    void pathEnsureLeadingSlash_shouldReturnSamePathWhenLeadingSlashExists() {
        // Given
        String path = "/api/endpoint";

        // When
        String result = URISanitizer.pathEnsureLeadingSlash(path);

        // Then
        assertEquals("/api/endpoint", result);
    }

    @Test
    void pathEnsureLeadingSlash_shouldHandleNull() {
        // Given
        String path = null;

        // When
        String result = URISanitizer.pathEnsureLeadingSlash(path);

        // Then
        assertNull(result);
    }

    @Test
    void pathEnsureLeadingSlash_shouldHandleEmptyString() {
        // Given
        String path = "";

        // When
        String result = URISanitizer.pathEnsureLeadingSlash(path);

        // Then
        assertEquals("/", result);
    }

    @Test
    void pathEnsureLeadingSlash_shouldHandleSingleSlash() {
        // Given
        String path = "/";

        // When
        String result = URISanitizer.pathEnsureLeadingSlash(path);

        // Then
        assertEquals("/", result);
    }
}
