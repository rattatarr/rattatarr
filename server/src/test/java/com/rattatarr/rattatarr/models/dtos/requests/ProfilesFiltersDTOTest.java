package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProfilesFiltersDTOTest {

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
    void testRecordConstructor() {
        // Given
        String name = "Test";
        LocalDateTime createdAtAfter = LocalDateTime.now().minusDays(7);
        LocalDateTime createdAtBefore = LocalDateTime.now();
        LocalDateTime updatedAtAfter = LocalDateTime.now().minusDays(3);
        LocalDateTime updatedAtBefore = LocalDateTime.now();

        // When
        ProfilesFiltersDTO dto = new ProfilesFiltersDTO(
                name,
                createdAtAfter,
                createdAtBefore,
                updatedAtAfter,
                updatedAtBefore
        );

        // Then
        assertEquals(name, dto.name());
        assertEquals(createdAtAfter, dto.createdAtAfter());
        assertEquals(createdAtBefore, dto.createdAtBefore());
        assertEquals(updatedAtAfter, dto.updatedAtAfter());
        assertEquals(updatedAtBefore, dto.updatedAtBefore());
    }

    @Test
    void testWithNullValues() {
        // When
        ProfilesFiltersDTO dto = new ProfilesFiltersDTO(null, null, null, null, null);

        // Then
        assertNull(dto.name());
        assertNull(dto.createdAtAfter());
        assertNull(dto.createdAtBefore());
        assertNull(dto.updatedAtAfter());
        assertNull(dto.updatedAtBefore());
    }

    @Test
    void testWithOnlyName() {
        // When
        ProfilesFiltersDTO dto = new ProfilesFiltersDTO("Test", null, null, null, null);

        // Then
        assertEquals("Test", dto.name());
        assertNull(dto.createdAtAfter());
        assertNull(dto.createdAtBefore());
        assertNull(dto.updatedAtAfter());
        assertNull(dto.updatedAtBefore());
    }

    @Test
    void testWithOnlyDateFilters() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        // When
        ProfilesFiltersDTO dto = new ProfilesFiltersDTO(null, yesterday, now, yesterday, now);

        // Then
        assertNull(dto.name());
        assertEquals(yesterday, dto.createdAtAfter());
        assertEquals(now, dto.createdAtBefore());
        assertEquals(yesterday, dto.updatedAtAfter());
        assertEquals(now, dto.updatedAtBefore());
    }

    @Test
    void testRecordEquality() {
        // Given
        LocalDateTime date1 = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 1, 31, 12, 0);

        ProfilesFiltersDTO dto1 = new ProfilesFiltersDTO("Test", date1, date2, date1, date2);
        ProfilesFiltersDTO dto2 = new ProfilesFiltersDTO("Test", date1, date2, date1, date2);

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testRecordInequality() {
        // Given
        LocalDateTime date = LocalDateTime.now();

        ProfilesFiltersDTO dto1 = new ProfilesFiltersDTO("Test1", date, date, date, date);
        ProfilesFiltersDTO dto2 = new ProfilesFiltersDTO("Test2", date, date, date, date);

        // Then
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testRecordToString() {
        // Given
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 12, 0);
        ProfilesFiltersDTO dto = new ProfilesFiltersDTO("TestProfile", date, date, date, date);

        // When
        String toString = dto.toString();

        // Then
        assertTrue(toString.contains("TestProfile"));
        assertTrue(toString.contains("2024"));
    }

    @Test
    void testWithPartialDateFilters() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        ProfilesFiltersDTO dto = new ProfilesFiltersDTO("Test", now, null, null, now);

        // Then
        assertEquals("Test", dto.name());
        assertEquals(now, dto.createdAtAfter());
        assertNull(dto.createdAtBefore());
        assertNull(dto.updatedAtAfter());
        assertEquals(now, dto.updatedAtBefore());
    }

    @Test
    void testBlankStringName() {
        // Given
        ProfilesFiltersDTO dto = new ProfilesFiltersDTO(" ", null, null, null, null);

        // When
        Set<ConstraintViolation<ProfilesFiltersDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("cannot be blank")));

    }
}
