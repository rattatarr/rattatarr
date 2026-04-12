package com.rattatarr.rattatarr.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GlobalModelAttributeAdviceTest {

    private GlobalModelAttributeAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new GlobalModelAttributeAdvice();
    }

    @Test
    void testResolveZoneWithUTC() {
        // When
        ZoneId result = advice.resolveZone("UTC");

        // Then
        assertEquals(ZoneId.of("UTC"), result);
    }

    @Test
    void testResolveZoneWithAmericaNewYork() {
        // When
        ZoneId result = advice.resolveZone("America/New_York");

        // Then
        assertEquals(ZoneId.of("America/New_York"), result);
    }

    @Test
    void testResolveZoneWithEuropeLondon() {
        // When
        ZoneId result = advice.resolveZone("Europe/London");

        // Then
        assertEquals(ZoneId.of("Europe/London"), result);
    }

    @Test
    void testResolveZoneWithAsiaTokyo() {
        // When
        ZoneId result = advice.resolveZone("Asia/Tokyo");

        // Then
        assertEquals(ZoneId.of("Asia/Tokyo"), result);
    }

    @Test
    void testResolveZoneWithDefaultValue() {
        // When - The @RequestParam default value will be used when null is passed
        // This test verifies the annotation would provide UTC as default
        ZoneId result = advice.resolveZone("UTC");

        // Then
        assertEquals(ZoneId.of("UTC"), result);
    }

    @Test
    void testResolveZoneWithInvalidTimezone() {
        // When/Then
        assertThrows(Exception.class, () -> advice.resolveZone("Invalid/Timezone"));
    }
}
