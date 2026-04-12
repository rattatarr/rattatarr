package com.rattatarr.rattatarr.models.dtos.responses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaginationMetadataTest {

    @Test
    void testRecordConstructor() {
        // When
        PaginationMetadata metadata = new PaginationMetadata(
                1,
                20,
                100,
                5,
                false,
                false,
                true,
                true
        );

        // Then
        assertEquals(1, metadata.currentPage());
        assertEquals(20, metadata.pageSize());
        assertEquals(100, metadata.totalElements());
        assertEquals(5, metadata.totalPages());
        assertFalse(metadata.isFirst());
        assertFalse(metadata.isLast());
        assertTrue(metadata.hasNext());
        assertTrue(metadata.hasPrevious());
    }

    @Test
    void testFirstPage() {
        // When
        PaginationMetadata metadata = new PaginationMetadata(
                0,
                20,
                100,
                5,
                true,
                false,
                true,
                false
        );

        // Then
        assertEquals(0, metadata.currentPage());
        assertTrue(metadata.isFirst());
        assertFalse(metadata.isLast());
        assertTrue(metadata.hasNext());
        assertFalse(metadata.hasPrevious());
    }

    @Test
    void testLastPage() {
        // When
        PaginationMetadata metadata = new PaginationMetadata(
                4,
                20,
                100,
                5,
                false,
                true,
                false,
                true
        );

        // Then
        assertEquals(4, metadata.currentPage());
        assertFalse(metadata.isFirst());
        assertTrue(metadata.isLast());
        assertFalse(metadata.hasNext());
        assertTrue(metadata.hasPrevious());
    }

    @Test
    void testSinglePage() {
        // When
        PaginationMetadata metadata = new PaginationMetadata(
                0,
                20,
                10,
                1,
                true,
                true,
                false,
                false
        );

        // Then
        assertEquals(0, metadata.currentPage());
        assertEquals(1, metadata.totalPages());
        assertTrue(metadata.isFirst());
        assertTrue(metadata.isLast());
        assertFalse(metadata.hasNext());
        assertFalse(metadata.hasPrevious());
    }

    @Test
    void testEmptyResult() {
        // When
        PaginationMetadata metadata = new PaginationMetadata(
                0,
                20,
                0,
                0,
                true,
                true,
                false,
                false
        );

        // Then
        assertEquals(0, metadata.totalElements());
        assertEquals(0, metadata.totalPages());
    }

    @Test
    void testRecordEquality() {
        // Given
        PaginationMetadata meta1 = new PaginationMetadata(1, 10, 100, 10, false, false, true, true);
        PaginationMetadata meta2 = new PaginationMetadata(1, 10, 100, 10, false, false, true, true);

        // Then
        assertEquals(meta1, meta2);
        assertEquals(meta1.hashCode(), meta2.hashCode());
    }

    @Test
    void testRecordInequality() {
        // Given
        PaginationMetadata meta1 = new PaginationMetadata(1, 10, 100, 10, false, false, true, true);
        PaginationMetadata meta2 = new PaginationMetadata(2, 10, 100, 10, false, false, true, true);

        // Then
        assertNotEquals(meta1, meta2);
    }

    @Test
    void testRecordToString() {
        // Given
        PaginationMetadata metadata = new PaginationMetadata(2, 50, 250, 5, false, false, true, true);

        // When
        String toString = metadata.toString();

        // Then
        assertTrue(toString.contains("2"));
        assertTrue(toString.contains("50"));
        assertTrue(toString.contains("250"));
        assertTrue(toString.contains("5"));
    }

    @Test
    void testLargeDataset() {
        // When
        PaginationMetadata metadata = new PaginationMetadata(
                100,
                100,
                10000,
                100,
                false,
                true,
                false,
                true
        );

        // Then
        assertEquals(100, metadata.currentPage());
        assertEquals(10000, metadata.totalElements());
        assertEquals(100, metadata.totalPages());
    }

    @Test
    void testDifferentPageSizes() {
        // Given
        PaginationMetadata small = new PaginationMetadata(0, 10, 100, 10, true, false, true, false);
        PaginationMetadata medium = new PaginationMetadata(0, 20, 100, 5, true, false, true, false);
        PaginationMetadata large = new PaginationMetadata(0, 50, 100, 2, true, false, true, false);

        // Then
        assertEquals(10, small.pageSize());
        assertEquals(20, medium.pageSize());
        assertEquals(50, large.pageSize());
        assertEquals(10, small.totalPages());
        assertEquals(5, medium.totalPages());
        assertEquals(2, large.totalPages());
    }
}
