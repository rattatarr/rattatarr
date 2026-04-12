package com.rattatarr.rattatarr.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProfileExceptionsTest {

    @Test
    void testConstructorThrowsException() {
        // When/Then
        assertThrows(Exception.class, () -> {
            var constructor = ProfilesExceptions.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testProfileNotFoundExceptionWithUUID() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        ProfilesExceptions.ProfileNotFoundExceptions exception =
                new ProfilesExceptions.ProfileNotFoundExceptions(id);

        // Then
        assertTrue(exception.getMessage().contains(id.toString()));
        assertTrue(exception.getMessage().contains("Profile"));
        assertTrue(exception.getMessage().contains("not found"));
        assertEquals(HttpStatus.NOT_FOUND, exception.status());
    }

    @Test
    void testProfileNotFoundExceptionWithName() {
        // Given
        String name = "TestProfile";

        // When
        ProfilesExceptions.ProfileNotFoundExceptions exception =
                new ProfilesExceptions.ProfileNotFoundExceptions(name);

        // Then
        assertTrue(exception.getMessage().contains(name));
        assertTrue(exception.getMessage().contains("Profile"));
        assertTrue(exception.getMessage().contains("not found"));
        assertEquals(HttpStatus.NOT_FOUND, exception.status());
    }

    @Test
    void testDuplicateProfileNameException() {
        // Given
        String name = "DuplicateProfile";

        // When
        ProfilesExceptions.DuplicateProfileNameExceptions exception =
                new ProfilesExceptions.DuplicateProfileNameExceptions(name);

        // Then
        assertTrue(exception.getMessage().contains(name));
        assertTrue(exception.getMessage().contains("already taken"));
        assertEquals(HttpStatus.CONFLICT, exception.status());
    }

    @Test
    void testInvalidProfileNameException() {
        // Given
        String reason = "Name contains invalid characters";

        // When
        ProfilesExceptions.InvalidProfileNameExceptions exception =
                new ProfilesExceptions.InvalidProfileNameExceptions(reason);

        // Then
        assertTrue(exception.getMessage().contains(reason));
        assertTrue(exception.getMessage().contains("Invalid profile name"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void testAllExceptionsInheritFromBase() {
        // When/Then
        assertInstanceOf(BaseRattatarrExceptions.class, new ProfilesExceptions.ProfileNotFoundExceptions(UUID.randomUUID()));
        assertInstanceOf(BaseRattatarrExceptions.class, new ProfilesExceptions.DuplicateProfileNameExceptions("test"));
        assertInstanceOf(BaseRattatarrExceptions.class, new ProfilesExceptions.InvalidProfileNameExceptions("test"));
    }

    @Test
    void testProfileNotFoundWithSpecialCharacters() {
        // Given
        String name = "Test@Profile#123";

        // When
        ProfilesExceptions.ProfileNotFoundExceptions exception =
                new ProfilesExceptions.ProfileNotFoundExceptions(name);

        // Then
        assertTrue(exception.getMessage().contains(name));
    }

    @Test
    void testDuplicateProfileNameWithEmptyString() {
        // Given
        String name = "";

        // When
        ProfilesExceptions.DuplicateProfileNameExceptions exception =
                new ProfilesExceptions.DuplicateProfileNameExceptions(name);

        // Then
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("already taken"));
    }
}
