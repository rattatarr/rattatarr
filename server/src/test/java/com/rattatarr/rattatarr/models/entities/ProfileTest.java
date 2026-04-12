package com.rattatarr.rattatarr.models.entities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        } catch (Exception e) {
            fail("Failed to create ValidatorFactory: " + e.getMessage());
        }
    }

    @Test
    void testConstructorWithNameAndJellyfinId() {
        // Given
        String name = "Test Profile";
        String jellyfinId = UUID.randomUUID().toString().toString();

        // When
        Profile profile = new Profile(name, jellyfinId);

        // Then
        assertEquals(name, profile.name());
        assertEquals(jellyfinId, profile.jellyfinId());
    }

    @Test
    void testConstructorWithNullJellyfinId() {
        // Given
        String name = "Test Profile";

        // When
        Profile profile = new Profile(name, null);

        // Then
        assertEquals(name, profile.name());
        assertNull(profile.jellyfinId());
    }

    @Test
    void testSetName() {
        // Given
        Profile profile = new Profile("Original Name", UUID.randomUUID().toString());
        String newName = "Updated Name";

        // When
        profile.setName(newName);

        // Then
        assertEquals(newName, profile.name());
    }

    @Test
    void testSetJellyfinId() {
        // Given
        Profile profile = new Profile("Test", null);
        String jellyfinId = UUID.randomUUID().toString();

        // When
        profile.setJellyfinId(jellyfinId);

        // Then
        assertEquals(jellyfinId, profile.jellyfinId());
    }

    @Test
    void testSoftDelete() {
        // Given
        String originalName = "Test Profile";
        Profile profile = new Profile(originalName, UUID.randomUUID().toString());

        // When
        profile.softDelete();

        // Then
        assertTrue(profile.isDeleted());
        assertNotNull(profile.deletedAt());
        assertNotEquals(originalName, profile.name());
        assertTrue(profile.name().contains(originalName));
        assertTrue(profile.name().contains("_deleted_at_"));
    }

    @Test
    void testSoftDeleteModifiesName() {
        // Given
        String originalName = "TestProfile";
        Profile profile = new Profile(originalName, UUID.randomUUID().toString());

        // When
        profile.softDelete();

        // Then
        assertTrue(profile.name().startsWith(originalName + "_deleted_at_"));
        assertTrue(profile.name().contains("T")); // Should contain timestamp
    }

    @Test
    void testSoftDeleteReplacesColonsInTimestamp() {
        // Given
        Profile profile = new Profile("Test", UUID.randomUUID().toString());

        // When
        profile.softDelete();

        // Then
        assertFalse(profile.name().contains(":"), "Name should not contain colons after soft delete");
        assertTrue(profile.name().contains("-"), "Timestamp colons should be replaced with hyphens");
    }

    @Test
    void testMultipleSoftDeletes() {
        // Given
        Profile profile = new Profile("Test", UUID.randomUUID().toString());

        // When
        profile.softDelete();
        String firstDeletedName = profile.name();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        profile.softDelete();
        String secondDeletedName = profile.name();

        // Then
        assertNotEquals(firstDeletedName, secondDeletedName);
        assertTrue(secondDeletedName.contains("_deleted_at_"));
    }

    @Test
    void testInheritsFromBaseEntity() {
        // Given
        Profile profile = new Profile("Test", UUID.randomUUID().toString());

        // Then
        assertInstanceOf(BaseEntity.class, profile);
    }

    @Test
    void testSetJellyfinIdToNull() {
        // Given
        Profile profile = new Profile("Test", UUID.randomUUID().toString());

        // When
        profile.setJellyfinId(null);

        // Then
        assertNull(profile.jellyfinId());
    }

    @Test
    void testNameWithSpecialCharacters() {
        // Given
        String specialName = "Test@Profile#123!";

        // When
        Profile profile = new Profile(specialName, UUID.randomUUID().toString());

        // Then
        assertEquals(specialName, profile.name());
    }

    @Test
    void testLongProfileName() {
        // Given
        String longName = "A".repeat(100);
        Profile profile = new Profile(longName, UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testExceedingMaxProfileNameLength() {
        // Given
        String longName = "A".repeat(101);
        Profile profile = new Profile(longName, UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("cannot exceed 100 characters")));
    }
}
