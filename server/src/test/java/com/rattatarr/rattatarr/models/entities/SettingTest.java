package com.rattatarr.rattatarr.models.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingTest {

    @Test
    void constructor_shouldCreateSetting() {
        // Given & When
        Setting setting = new Setting("app.name", "Rattatarr", "Application name");

        // Then
        assertEquals("app.name", setting.key());
        assertEquals("Rattatarr", setting.value());
        assertEquals("Application name", setting.description());
    }

    @Test
    void setters_shouldUpdateValues() {
        // Given
        Setting setting = new Setting("key1", "value1", "desc1");

        // When
        setting.setValue("newValue");
        setting.setDescription("newDescription");

        // Then
        assertEquals("newValue", setting.value());
        assertEquals("newDescription", setting.description());
    }

    @Test
    void constructor_shouldAcceptNullDescription() {
        // Given & When
        Setting setting = new Setting("key", "value", null);

        // Then
        assertNull(setting.description());
    }
}
