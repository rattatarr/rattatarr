package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.SettingValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SettingsWrapperTest {

    @Test
    void fromMap_shouldCreateWrapperFromMap() {
        // Given
        SettingValue setting1 = new SettingValue("value1", "desc1");
        SettingValue setting2 = new SettingValue("value2", "desc2");
        Map<String, SettingValue> settingsMap = Map.of(
                "key1", setting1,
                "key2", setting2
        );

        // When
        SettingsWrapper wrapper = SettingsWrapper.fromMap(settingsMap);

        // Then
        assertNotNull(wrapper);
        assertEquals(2, wrapper.settings().size());
        assertTrue(wrapper.settings().stream()
                .anyMatch(s -> s.key().equals("key1") && s.value().equals("value1")));
        assertTrue(wrapper.settings().stream()
                .anyMatch(s -> s.key().equals("key2") && s.value().equals("value2")));
    }

    @Test
    void fromMap_shouldHandleEmptyMap() {
        // Given
        Map<String, SettingValue> emptyMap = Map.of();

        // When
        SettingsWrapper wrapper = SettingsWrapper.fromMap(emptyMap);

        // Then
        assertNotNull(wrapper);
        assertEquals(0, wrapper.settings().size());
    }
}
