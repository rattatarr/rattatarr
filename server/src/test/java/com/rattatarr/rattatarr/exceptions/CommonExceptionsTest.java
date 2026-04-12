package com.rattatarr.rattatarr.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommonExceptionsTest {

    @Test
    void testConstructorThrowsException() {
        // When/Then
        assertThrows(Exception.class, () -> {
            var constructor = CommonExceptions.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testResourceNotFoundExceptionWithUUID() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        CommonExceptions.ResourceNotFoundExceptions exception =
                new CommonExceptions.ResourceNotFoundExceptions("Profile", id);

        // Then
        assertTrue(exception.getMessage().contains(id.toString()));
        assertTrue(exception.getMessage().contains("Profile"));
        assertTrue(exception.getMessage().contains("not found"));
        assertEquals(HttpStatus.NOT_FOUND, exception.status());
    }

    @Test
    void testResourceNotFoundExceptionWithMessage() {
        // Given
        String message = "Custom resource not found";

        // When
        CommonExceptions.ResourceNotFoundExceptions exception =
                new CommonExceptions.ResourceNotFoundExceptions(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.status());
    }

    @Test
    void testInvalidRequestException() {
        // Given
        String message = "Invalid input data";

        // When
        CommonExceptions.InvalidRequestExceptions exception =
                new CommonExceptions.InvalidRequestExceptions(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void testInvalidRequestExceptionWithCause() {
        // Given
        String message = "Invalid input data";
        Throwable cause = new RuntimeException("Root cause");

        // When
        CommonExceptions.InvalidRequestExceptions exception =
                new CommonExceptions.InvalidRequestExceptions(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void testInvalidRequestExceptionWithNullCause() {
        // Given
        String message = "Invalid input data";

        // When
        CommonExceptions.InvalidRequestExceptions exception =
                new CommonExceptions.InvalidRequestExceptions(message, null);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void testInvalidTimezoneException() {
        // Given
        String timezone = "Invalid/Timezone";

        // When
        CommonExceptions.InvalidTimezoneExceptions exception =
                new CommonExceptions.InvalidTimezoneExceptions(timezone);

        // Then
        assertTrue(exception.getMessage().contains(timezone));
        assertTrue(exception.getMessage().contains("Invalid timezone"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void testUnauthorizedException() {
        // Given
        String message = "Invalid token";

        // When
        CommonExceptions.UnauthorizedExceptions exception =
                new CommonExceptions.UnauthorizedExceptions(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.status());
    }

    @Test
    void testForbiddenException() {
        // Given
        String message = "Access denied";

        // When
        CommonExceptions.ForbiddenExceptions exception =
                new CommonExceptions.ForbiddenExceptions(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.status());
    }

    @Test
    void testConflictException() {
        // Given
        String message = "Resource already exists";

        // When
        CommonExceptions.ConflictExceptions exception =
                new CommonExceptions.ConflictExceptions(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.status());
    }

    @Test
    void testAllExceptionsInheritFromBase() {
        // When/Then
        assertInstanceOf(BaseRattatarrExceptions.class, new CommonExceptions.ResourceNotFoundExceptions("test", UUID.randomUUID()));
        assertInstanceOf(BaseRattatarrExceptions.class, new CommonExceptions.InvalidRequestExceptions("test"));
        assertInstanceOf(BaseRattatarrExceptions.class, new CommonExceptions.InvalidTimezoneExceptions("test"));
        assertInstanceOf(BaseRattatarrExceptions.class, new CommonExceptions.UnauthorizedExceptions("test"));
        assertInstanceOf(BaseRattatarrExceptions.class, new CommonExceptions.ForbiddenExceptions("test"));
        assertInstanceOf(BaseRattatarrExceptions.class, new CommonExceptions.ConflictExceptions("test"));
    }

    @Test
    void testInvalidTimezoneExceptionExtendsInvalidRequest() {
        // When
        CommonExceptions.InvalidTimezoneExceptions exception =
                new CommonExceptions.InvalidTimezoneExceptions("test");

        // Then
        assertInstanceOf(CommonExceptions.InvalidRequestExceptions.class, exception);
    }
}
