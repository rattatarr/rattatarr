package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.SettingsExceptions;
import com.rattatarr.rattatarr.models.SettingValue;
import com.rattatarr.rattatarr.models.entities.Setting;
import com.rattatarr.rattatarr.repositories.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private SettingsRepository repository;

    @InjectMocks
    private SettingsService service;

    private Setting testSetting;

    @BeforeEach
    void setUp() {
        testSetting = new Setting("test.key", "test value", "Test description");
    }

    @Test
    void testLoadSettingsIntoCache() {
        // Given
        Setting setting1 = new Setting("key1", "value1", "desc1");
        Setting setting2 = new Setting("key2", "value2", "desc2");
        List<Setting> settings = List.of(setting1, setting2);

        when(repository.findAll()).thenReturn(settings);
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.loadSettingsIntoCache();

        // Then
        verify(repository).findAll();
        Map<String, SettingValue> allSettings = service.getAllSettings();
        assertTrue(allSettings.containsKey("key1"));
        assertTrue(allSettings.containsKey("key2"));
        assertEquals("value1", allSettings.get("key1").value());
        assertEquals("value2", allSettings.get("key2").value());
    }

    @Test
    void testGetSetting_FromCache() {
        // Given
        when(repository.findAll()).thenReturn(List.of(testSetting));
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.loadSettingsIntoCache();

        // When
        SettingValue result = service.getSetting("test.key");

        // Then
        assertNotNull(result);
        assertEquals("test value", result.value());
        assertEquals("Test description", result.description());
        // Verify repository was not queried again (cached)
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetSetting_NotInCacheButInDatabase() {
        // Given
        when(repository.findAll()).thenReturn(List.of());
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.loadSettingsIntoCache();

        when(repository.findByKey("new.key")).thenReturn(Optional.of(testSetting));

        // When
        SettingValue result = service.getSetting("new.key");

        // Then
        assertNotNull(result);
        assertEquals("test value", result.value());
        verify(repository).findByKey("new.key");
    }

    @Test
    void testGetSetting_NotFound() {
        // Given
        when(repository.findAll()).thenReturn(List.of());
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.loadSettingsIntoCache();

        when(repository.findByKey("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThrows(SettingsExceptions.SettingNotFoundException.class,
                () -> service.getSetting("nonexistent"));
        verify(repository).findByKey("nonexistent");
    }

    @Test
    void testGetAllSettings() {
        // Given
        Setting setting1 = new Setting("key1", "value1", "desc1");
        Setting setting2 = new Setting("key2", "value2", "desc2");
        when(repository.findAll()).thenReturn(List.of(setting1, setting2));
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.loadSettingsIntoCache();

        // When
        Map<String, SettingValue> allSettings = service.getAllSettings();

        // Then
        assertNotNull(allSettings);
        assertTrue(allSettings.size() >= 2); // At least our 2 settings, plus defaults
        assertTrue(allSettings.containsKey("key1"));
        assertTrue(allSettings.containsKey("key2"));
    }

    @Test
    void testUpdateSetting_Success() {
        // Given
        when(repository.findAll()).thenReturn(List.of(testSetting));
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.loadSettingsIntoCache();

        when(repository.findByKey("test.key")).thenReturn(Optional.of(testSetting));

        // When
        service.updateSetting("test.key", "new value", "new description");

        // Then
        ArgumentCaptor<Setting> settingCaptor = ArgumentCaptor.forClass(Setting.class);
        verify(repository, atLeast(1)).save(settingCaptor.capture());

        // Get the last saved setting (the update)
        List<Setting> savedSettings = settingCaptor.getAllValues();
        Setting updatedSetting = savedSettings.get(savedSettings.size() - 1);
        assertEquals("new value", updatedSetting.value());
        assertEquals("new description", updatedSetting.description());

        // Verify cache was updated
        SettingValue cachedValue = service.getSetting("test.key");
        assertEquals("new value", cachedValue.value());
        assertEquals("new description", cachedValue.description());
    }

    @Test
    void testUpdateSetting_NullDescription() {
        // Given
        when(repository.findAll()).thenReturn(List.of(testSetting));
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.loadSettingsIntoCache();

        when(repository.findByKey("test.key")).thenReturn(Optional.of(testSetting));

        // When
        service.updateSetting("test.key", "new value", null);

        // Then
        ArgumentCaptor<Setting> settingCaptor = ArgumentCaptor.forClass(Setting.class);
        verify(repository, atLeast(1)).save(settingCaptor.capture());

        List<Setting> savedSettings = settingCaptor.getAllValues();
        Setting updatedSetting = savedSettings.get(savedSettings.size() - 1);
        assertEquals("new value", updatedSetting.value());
        assertEquals("Test description", updatedSetting.description()); // Original description preserved
    }

    @Test
    void testUpdateSetting_NotFound() {
        // Given
        when(repository.findAll()).thenReturn(List.of());
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.loadSettingsIntoCache();

        when(repository.findByKey("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThrows(SettingsExceptions.SettingNotFoundException.class,
                () -> service.updateSetting("nonexistent", "value", "desc"));
        verify(repository).findByKey("nonexistent");
    }

    @Test
    void testCreateSettingIfNotExists_NewSetting() {
        // Given
        when(repository.findByKey("new.setting")).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.createSettingIfNotExists("new.setting", "default value", "Default description");

        // Then
        ArgumentCaptor<Setting> settingCaptor = ArgumentCaptor.forClass(Setting.class);
        verify(repository).save(settingCaptor.capture());

        Setting savedSetting = settingCaptor.getValue();
        assertEquals("new.setting", savedSetting.key());
        assertEquals("default value", savedSetting.value());
        assertEquals("Default description", savedSetting.description());
    }

    @Test
    void testCreateSettingIfNotExists_AlreadyExists() {
        // Given
        when(repository.findByKey("existing.setting")).thenReturn(Optional.of(testSetting));

        // When
        service.createSettingIfNotExists("existing.setting", "value", "desc");

        // Then
        verify(repository, never()).save(any(Setting.class));
    }

    @Test
    void testInitializeDefaultSettings() {
        // Given
        when(repository.findAll()).thenReturn(List.of());
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.loadSettingsIntoCache();

        // Then - verify default settings were created
        verify(repository, atLeastOnce()).save(argThat(setting ->
                SettingsService.JELLYFIN_BASE_URL.equals(setting.key()) ||
                SettingsService.JELLYFIN_API_KEY.equals(setting.key()) ||
                SettingsService.TMDB_BASE_URL.equals(setting.key()) ||
                SettingsService.TMDB_API_KEY.equals(setting.key()) ||
                SettingsService.TMDB_IMAGE_BASE_URL.equals(setting.key())
        ));

        // Verify settings are in cache
        Map<String, SettingValue> allSettings = service.getAllSettings();
        assertTrue(allSettings.containsKey(SettingsService.TMDB_BASE_URL));
        assertEquals("https://api.themoviedb.org", allSettings.get(SettingsService.TMDB_BASE_URL).value());
    }
}
