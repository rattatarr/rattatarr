package com.rattatarr.rattatarr.models.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    @Test
    void testGettersAndSetters() {
        // Given
        TestEntity entity = new TestEntity();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        // When
        entity.id = id;
        entity.createdAt = now;
        entity.updatedAt = now;

        // Then
        assertEquals(id, entity.id());
        assertEquals(now, entity.createdAt());
        assertEquals(now, entity.updatedAt());
    }

    @Test
    void testIsDeleted() {
        // Given
        TestEntity entity = new TestEntity();

        // Then
        assertFalse(entity.isDeleted());
    }

    @Test
    void testSoftDelete() {
        // Given
        TestEntity entity = new TestEntity();

        // When
        entity.softDelete();

        // Then
        assertTrue(entity.isDeleted());
        assertNotNull(entity.deletedAt());
    }

    @Test
    void testDeletedAt() {
        // Given
        TestEntity entity = new TestEntity();
        Instant beforeDelete = Instant.now();

        // When
        entity.softDelete();
        Instant afterDelete = Instant.now();

        // Then
        assertNotNull(entity.deletedAt());
        assertTrue(entity.deletedAt().isAfter(beforeDelete.minusSeconds(1)));
        assertTrue(entity.deletedAt().isBefore(afterDelete.plusSeconds(1)));
    }

    @Test
    void testIsNotDeletedInitially() {
        // Given
        TestEntity entity = new TestEntity();

        // Then
        assertNull(entity.deletedAt());
        assertFalse(entity.isDeleted());
    }

    @Test
    void testMultipleSoftDeleteCalls() {
        // Given
        TestEntity entity = new TestEntity();

        // When
        entity.softDelete();
        Instant firstDeleteTime = entity.deletedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        entity.softDelete();
        Instant secondDeleteTime = entity.deletedAt();

        // Then
        assertNotEquals(firstDeleteTime, secondDeleteTime);
        assertTrue(secondDeleteTime.isAfter(firstDeleteTime));
    }

    @Test
    void testPrePersistShouldSetTimestamps() throws Exception {
        // Given
        TestEntity entity = new TestEntity();
        var method = BaseEntity.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);

        // When
        method.invoke(entity);

        // Then
        assertNotNull(entity.createdAt());
        assertNotNull(entity.updatedAt());
        assertEquals(entity.createdAt(), entity.updatedAt());
    }

    @Test
    void testPreUpdateShouldUpdateTimestamp() throws Exception {
        // Given
        TestEntity entity = new TestEntity();
        var onCreateMethod = BaseEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(entity);

        Instant originalCreatedAt = entity.createdAt();
        Instant originalUpdatedAt = entity.updatedAt();

        Thread.sleep(10);

        var onUpdateMethod = BaseEntity.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);

        // When
        onUpdateMethod.invoke(entity);

        // Then
        assertEquals(originalCreatedAt, entity.createdAt());
        assertNotEquals(originalUpdatedAt, entity.updatedAt());
        assertTrue(entity.updatedAt().isAfter(originalUpdatedAt));
    }

    // Test implementation
    private static class TestEntity extends BaseEntity {
    }
}
