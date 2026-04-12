package com.rattatarr.rattatarr.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BrokenMediaItemsExceptionsTest {

    @Test
    void testConstructorThrowsException() {
        assertThrows(Exception.class, () -> {
            var constructor = BrokenMediaItemsExceptions.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testBrokenMediaItemNotFoundExceptionsWithUUID() {
        UUID id = UUID.randomUUID();
        BrokenMediaItemsExceptions.BrokenMediaItemNotFoundExceptions exception =
                new BrokenMediaItemsExceptions.BrokenMediaItemNotFoundExceptions(id);
        assertTrue(exception.getMessage().contains(id.toString()));
        assertTrue(exception.getMessage().contains("Broken media item"));
        assertTrue(exception.getMessage().contains("not found"));
        assertEquals(HttpStatus.NOT_FOUND, exception.status());
    }

    @Test
    void testAllExceptionsInheritFromBase() {
        assertInstanceOf(
                BaseRattatarrExceptions.class,
                new BrokenMediaItemsExceptions.BrokenMediaItemNotFoundExceptions(UUID.randomUUID()));
    }
}
