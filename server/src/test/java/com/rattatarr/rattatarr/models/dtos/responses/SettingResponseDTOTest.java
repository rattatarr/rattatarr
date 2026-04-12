package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.models.entities.Setting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingResponseDTOTest {

    @Test
    void fromEntity_shouldConvertSettingToDTO() {
        // Given
        Setting setting = new Setting("app.name", "Rattatarr", "Application name");

        // When
        SettingResponseDTO dto = SettingResponseDTO.fromEntity(setting);

        // Then
        assertNotNull(dto);
        assertEquals("app.name", dto.key());
        assertEquals("Rattatarr", dto.value());
        assertEquals("Application name", dto.description());
    }

    @Test
    void constructor_shouldCreateDTO() {
        // Given & When
        SettingResponseDTO dto = new SettingResponseDTO("key1", "value1", "desc1");

        // Then
        assertEquals("key1", dto.key());
        assertEquals("value1", dto.value());
        assertEquals("desc1", dto.description());
    }
}
