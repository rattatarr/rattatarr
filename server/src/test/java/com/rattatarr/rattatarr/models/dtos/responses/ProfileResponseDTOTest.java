package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.models.entities.Profile;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProfileResponseDTOTest {

    @Test
    void testRecordConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        String jellyfinId = UUID.randomUUID().toString();
        ZonedDateTime now = ZonedDateTime.now();
        String name = "Test Profile";

        // When
        ProfileResponseDTO dto = new ProfileResponseDTO(id, now, now, name, jellyfinId);

        // Then
        assertEquals(id, dto.id());
        assertEquals(now, dto.createdAt());
        assertEquals(now, dto.updatedAt());
        assertEquals(name, dto.name());
        assertEquals(jellyfinId, dto.jellyfinId());
    }

    @Test
    void testFromEntity() {
        // Given
        UUID id = UUID.randomUUID();
        String jellyfinId = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        String name = "Test Profile";
        ZoneId zoneId = ZoneId.of("America/New_York");

        Profile profile = createProfile(id, createdAt, updatedAt, name, jellyfinId);

        // When
        ProfileResponseDTO dto = ProfileResponseDTO.fromEntity(profile, zoneId);

        // Then
        assertEquals(id, dto.id());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.updatedAt());
        assertEquals(name, dto.name());
        assertEquals(jellyfinId, dto.jellyfinId());
        assertEquals(zoneId, dto.createdAt().getZone());
        assertEquals(zoneId, dto.updatedAt().getZone());
    }

    @Test
    void testFromEntityWithUTC() {
        // Given
        UUID id = UUID.randomUUID();
        String jellyfinId = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        String name = "UTC Profile";
        ZoneId zoneId = ZoneId.of("UTC");

        Profile profile = createProfile(id, createdAt, updatedAt, name, jellyfinId);

        // When
        ProfileResponseDTO dto = ProfileResponseDTO.fromEntity(profile, zoneId);

        // Then
        assertEquals(zoneId, dto.createdAt().getZone());
        assertEquals(zoneId, dto.updatedAt().getZone());
    }

    @Test
    void testFromEntityWithTokyoTimezone() {
        // Given
        UUID id = UUID.randomUUID();
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        Profile profile = createProfile(id, Instant.now(), Instant.now(), "Tokyo Profile", UUID.randomUUID().toString());

        // When
        ProfileResponseDTO dto = ProfileResponseDTO.fromEntity(profile, tokyoZone);

        // Then
        assertEquals(tokyoZone, dto.createdAt().getZone());
        assertEquals(tokyoZone, dto.updatedAt().getZone());
    }

    @Test
    void testRecordEquality() {
        // Given
        UUID id = UUID.randomUUID();
        String jellyfinId = UUID.randomUUID().toString();
        ZonedDateTime now = ZonedDateTime.now();
        String name = "Test Profile";

        ProfileResponseDTO dto1 = new ProfileResponseDTO(id, now, now, name, jellyfinId);
        ProfileResponseDTO dto2 = new ProfileResponseDTO(id, now, now, name, jellyfinId);

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testRecordInequality() {
        // Given
        ZonedDateTime now = ZonedDateTime.now();
        ProfileResponseDTO dto1 = new ProfileResponseDTO(UUID.randomUUID(), now, now, "Name1", UUID.randomUUID().toString());
        ProfileResponseDTO dto2 = new ProfileResponseDTO(UUID.randomUUID(), now, now, "Name2", UUID.randomUUID().toString());

        // Then
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testRecordToString() {
        // Given
        UUID id = UUID.randomUUID();
        String jellyfinId = UUID.randomUUID().toString();
        ZonedDateTime now = ZonedDateTime.now();
        String name = "Test Profile";

        ProfileResponseDTO dto = new ProfileResponseDTO(id, now, now, name, jellyfinId);

        // When
        String toString = dto.toString();

        // Then
        assertTrue(toString.contains(id.toString()));
        assertTrue(toString.contains(name));
        assertTrue(toString.contains(jellyfinId.toString()));
    }

    @Test
    void testWithNullJellyfinId() {
        // Given
        Profile profile = createProfile(UUID.randomUUID(), Instant.now(), Instant.now(), "Test", null);

        // When
        ProfileResponseDTO dto = ProfileResponseDTO.fromEntity(profile, ZoneId.of("UTC"));

        // Then
        assertNull(dto.jellyfinId());
    }

    @Test
    void testTimezoneConversion() {
        // Given
        Instant instant = Instant.parse("2024-01-01T12:00:00Z");
        Profile profile = createProfile(UUID.randomUUID(), instant, instant, "Test", UUID.randomUUID().toString());
        ZoneId nyZone = ZoneId.of("America/New_York");

        // When
        ProfileResponseDTO dto = ProfileResponseDTO.fromEntity(profile, nyZone);

        // Then
        assertEquals(instant, dto.createdAt().toInstant());
        assertEquals("2024-01-01T07:00-05:00[America/New_York]", dto.createdAt().toString());
        assertEquals(nyZone, dto.createdAt().getZone());
    }

    @Test
    void testWithSpecialCharactersInName() {
        // Given
        String specialName = "Test@Profile#123!";
        Profile profile = createProfile(UUID.randomUUID(), Instant.now(), Instant.now(), specialName, UUID.randomUUID().toString());

        // When
        ProfileResponseDTO dto = ProfileResponseDTO.fromEntity(profile, ZoneId.of("UTC"));

        // Then
        assertEquals(specialName, dto.name());
    }

    private Profile createProfile(UUID id, Instant createdAt, Instant updatedAt, String name, String jellyfinId) {
        Profile profile = new Profile(name, jellyfinId);
        try {
            var idField = profile.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(profile, id);

            var createdAtField = profile.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(profile, createdAt);

            var updatedAtField = profile.getClass().getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(profile, updatedAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return profile;
    }
}
