package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateProfileRequestDTOTest {

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
    void testValidDTO() {
        // Given
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO("Test Profile", UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<CreateProfileRequestDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidDTOWithNullJellyfinId() {
        // Given
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO("Test Profile", null);

        // When
        Set<ConstraintViolation<CreateProfileRequestDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankName() {
        // Given
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO("", UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<CreateProfileRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("cannot be blank")));
    }

    @Test
    void testNullName() {
        // Given
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO(null, UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<CreateProfileRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void testWhitespaceName() {
        // Given
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO("   ", UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<CreateProfileRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void testRecordEquality() {
        // Given
        String jellyfinId = UUID.randomUUID().toString();
        CreateProfileRequestDTO dto1 = new CreateProfileRequestDTO("Test", jellyfinId);
        CreateProfileRequestDTO dto2 = new CreateProfileRequestDTO("Test", jellyfinId);

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testRecordAccessors() {
        // Given
        String name = "My Profile";
        String jellyfinId = UUID.randomUUID().toString();
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO(name, jellyfinId);

        // Then
        assertEquals(name, dto.name());
        assertEquals(jellyfinId, dto.jellyfinId());
    }

    @Test
    void testRecordToString() {
        // Given
        String jellyfinId = UUID.randomUUID().toString();
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO("Test Profile", jellyfinId);

        // When
        String toString = dto.toString();

        // Then
        assertTrue(toString.contains("Test Profile"));
        assertTrue(toString.contains(jellyfinId.toString()));
    }

    @Test
    void testLongProfileName() {
        // Given
        String longName = "A".repeat(100);
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO(longName, UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<CreateProfileRequestDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLongProfileNameExceedingLimit() {
        // Given
        String longName = "A".repeat(101); // Assuming max size is 100
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO(longName, UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<CreateProfileRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("cannot exceed 100 characters")));
    }

    @Test
    void testSpecialCharactersInName() {
        // Given
        CreateProfileRequestDTO dto = new CreateProfileRequestDTO("Test@Profile#123", UUID.randomUUID().toString());

        // When
        Set<ConstraintViolation<CreateProfileRequestDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }
}
