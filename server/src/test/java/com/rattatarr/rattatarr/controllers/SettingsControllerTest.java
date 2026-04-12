package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.models.SettingValue;
import com.rattatarr.rattatarr.models.dtos.requests.CreateSettingRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.CreateSettingsRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SettingWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SettingsWrapper;
import com.rattatarr.rattatarr.services.SettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsControllerTest {

    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private SettingsController controller;

    @Test
    void getAllSettings_shouldReturnAllSettings() {
        // Given
        SettingValue setting1 = new SettingValue("value1", "desc1");
        SettingValue setting2 = new SettingValue("value2", "desc2");
        Map<String, SettingValue> settingsMap = Map.of(
                "key1", setting1,
                "key2", setting2
        );

        when(settingsService.getAllSettings()).thenReturn(settingsMap);

        // When
        ResponseEntity<SettingsWrapper> response = controller.getAllSettings();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().settings().size());
        verify(settingsService).getAllSettings();
    }

    @Test
    void getSettingByKey_shouldReturnSetting() {
        // Given
        String key = "testKey";
        SettingValue setting = new SettingValue("value", "description");

        when(settingsService.getSetting(eq(key))).thenReturn(setting);

        // When
        ResponseEntity<SettingWrapper> response = controller.getSettingByKey(key);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().setting());
        verify(settingsService).getSetting(eq(key));
    }

    @Test
    void updateSettings_shouldUpdateSettingsAndReturnAll() {
        // Given
        CreateSettingRequestDTO settingDTO1 = new CreateSettingRequestDTO("key1", "newValue1", "newDesc1");
        CreateSettingRequestDTO settingDTO2 = new CreateSettingRequestDTO("key2", "newValue2", "newDesc2");
        CreateSettingsRequestDTO requestDTO = new CreateSettingsRequestDTO(List.of(settingDTO1, settingDTO2));

        SettingValue updatedSetting1 = new SettingValue("newValue1", "newDesc1");
        SettingValue updatedSetting2 = new SettingValue("newValue2", "newDesc2");
        Map<String, SettingValue> updatedSettingsMap = Map.of(
                "key1", updatedSetting1,
                "key2", updatedSetting2
        );

        when(settingsService.getAllSettings()).thenReturn(updatedSettingsMap);

        // When
        ResponseEntity<SettingsWrapper> response = controller.updateSettings(requestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(settingsService).updateSetting(eq("key1"), eq("newValue1"), eq("newDesc1"));
        verify(settingsService).updateSetting(eq("key2"), eq("newValue2"), eq("newDesc2"));
        verify(settingsService).getAllSettings();
    }

    @Test
    void updateSettings_shouldHandleEmptyList() {
        // Given
        CreateSettingsRequestDTO requestDTO = new CreateSettingsRequestDTO(List.of());
        Map<String, SettingValue> emptySettingsMap = Map.of();

        when(settingsService.getAllSettings()).thenReturn(emptySettingsMap);

        // When
        ResponseEntity<SettingsWrapper> response = controller.updateSettings(requestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(settingsService).getAllSettings();
    }
}
