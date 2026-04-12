package com.rattatarr.rattatarr.clients.jellyfin;

import com.rattatarr.rattatarr.exceptions.JellyfinConfigExceptions;
import com.rattatarr.rattatarr.models.SettingValue;
import com.rattatarr.rattatarr.services.SettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JellyfinConfigTest {

    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private JellyfinConfig jellyfinConfig;

    @Test
    void getBaseUrl_withSettingPresent_shouldReturnUrl() {
        // Given
        String expectedUrl = "http://localhost:8096";
        SettingValue settingValue = new SettingValue(expectedUrl, "Jellyfin server base URL");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = jellyfinConfig.getBaseUrl();

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        verify(settingsService).getSetting(SettingsService.JELLYFIN_BASE_URL);
    }

    @Test
    void getBaseUrl_withSettingMissing_shouldReturnNull() {
        // Given
        SettingValue settingValue = new SettingValue(null, "Jellyfin server base URL");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = jellyfinConfig.getBaseUrl();

        // Then
        assertNull(actualUrl);
        verify(settingsService).getSetting(SettingsService.JELLYFIN_BASE_URL);
    }

    @Test
    void getBaseUrl_withTrailingSlash_shouldRemoveTrailingSlash() {
        // Given
        String urlWithTrailingSlash = "http://localhost:8096/";
        String expectedUrl = "http://localhost:8096";
        SettingValue settingValue = new SettingValue(urlWithTrailingSlash, "Jellyfin server base URL");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = jellyfinConfig.getBaseUrl();

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        assertFalse(actualUrl.endsWith("/"));
        verify(settingsService).getSetting(SettingsService.JELLYFIN_BASE_URL);
    }

    @Test
    void getApiKey_withSettingPresent_shouldReturnKey() {
        // Given
        String expectedApiKey = "test-api-key-12345";
        SettingValue settingValue = new SettingValue(expectedApiKey, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(settingValue);

        // When
        String actualApiKey = jellyfinConfig.getApiKey();

        // Then
        assertNotNull(actualApiKey);
        assertEquals(expectedApiKey, actualApiKey);
        verify(settingsService).getSetting(SettingsService.JELLYFIN_API_KEY);
    }

    @Test
    void getApiKey_withSettingMissing_shouldReturnNull() {
        // Given
        SettingValue settingValue = new SettingValue(null, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(settingValue);

        // When
        String actualApiKey = jellyfinConfig.getApiKey();

        // Then
        assertNull(actualApiKey);
        verify(settingsService).getSetting(SettingsService.JELLYFIN_API_KEY);
    }

    @Test
    void isConfigured_withBothUrlAndKey_shouldReturnTrue() {
        // Given
        SettingValue urlSetting = new SettingValue("http://localhost:8096", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        boolean isConfigured = jellyfinConfig.isConfigured();

        // Then
        assertTrue(isConfigured);
        verify(settingsService).getSetting(SettingsService.JELLYFIN_BASE_URL);
        verify(settingsService).getSetting(SettingsService.JELLYFIN_API_KEY);
    }

    @Test
    void isConfigured_withMissingUrl_shouldReturnFalse() {
        // Given
        SettingValue urlSetting = new SettingValue(null, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        boolean isConfigured = jellyfinConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withMissingKey_shouldReturnFalse() {
        // Given
        SettingValue urlSetting = new SettingValue("http://localhost:8096", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue(null, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        boolean isConfigured = jellyfinConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withBothMissing_shouldReturnFalse() {
        // Given
        SettingValue urlSetting = new SettingValue(null, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue(null, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        boolean isConfigured = jellyfinConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withEmptyUrl_shouldReturnFalse() {
        // Given
        SettingValue urlSetting = new SettingValue("", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        boolean isConfigured = jellyfinConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withEmptyKey_shouldReturnFalse() {
        // Given
        SettingValue urlSetting = new SettingValue("http://localhost:8096", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        boolean isConfigured = jellyfinConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withBlankUrl_shouldReturnFalse() {
        // Given
        SettingValue urlSetting = new SettingValue("   ", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        boolean isConfigured = jellyfinConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withBlankKey_shouldReturnFalse() {
        // Given
        SettingValue urlSetting = new SettingValue("http://localhost:8096", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("   ", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        boolean isConfigured = jellyfinConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void buildUrl_withValidConfig_shouldCombineUrlAndPath() {
        // Given
        String baseUrl = "http://localhost:8096";
        String path = "/Users";
        String expectedUrl = "http://localhost:8096/Users";
        SettingValue urlSetting = new SettingValue(baseUrl, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        String actualUrl = jellyfinConfig.buildUrl(path);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void buildUrl_withLeadingSlashInPath_shouldHandleCorrectly() {
        // Given
        String baseUrl = "http://localhost:8096";
        String path = "/Users/123/Items";
        String expectedUrl = "http://localhost:8096/Users/123/Items";
        SettingValue urlSetting = new SettingValue(baseUrl, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        String actualUrl = jellyfinConfig.buildUrl(path);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        // Ensure no double slashes
        assertFalse(actualUrl.contains("//Users"));
    }

    @Test
    void buildUrl_withoutLeadingSlashInPath_shouldAddSlash() {
        // Given
        String baseUrl = "http://localhost:8096";
        String path = "Users/123/Items";
        String expectedUrl = "http://localhost:8096/Users/123/Items";
        SettingValue urlSetting = new SettingValue(baseUrl, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        String actualUrl = jellyfinConfig.buildUrl(path);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        assertTrue(actualUrl.contains("/Users"));
    }

    @Test
    void buildUrl_whenNotConfigured_shouldThrowException() {
        // Given
        SettingValue urlSetting = new SettingValue(null, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue(null, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class,
                () -> jellyfinConfig.buildUrl("/Users"));
    }

    @Test
    void buildUrl_withMissingUrl_shouldThrowException() {
        // Given
        SettingValue urlSetting = new SettingValue(null, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class,
                () -> jellyfinConfig.buildUrl("/Users"));
    }

    @Test
    void buildUrl_withMissingApiKey_shouldThrowException() {
        // Given
        SettingValue urlSetting = new SettingValue("http://localhost:8096", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue(null, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class,
                () -> jellyfinConfig.buildUrl("/Users"));
    }

    @Test
    void getAuthHeader_withValidConfig_shouldFormatCorrectly() {
        // Given
        String apiKey = "test-api-key-12345";
        String expectedHeader = "MediaBrowser Token=\"test-api-key-12345\"";
        SettingValue apiKeySetting = new SettingValue(apiKey, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        String actualHeader = jellyfinConfig.getAuthHeader();

        // Then
        assertNotNull(actualHeader);
        assertEquals(expectedHeader, actualHeader);
        assertTrue(actualHeader.startsWith("MediaBrowser Token="));
        assertTrue(actualHeader.contains(apiKey));
    }

    @Test
    void getAuthHeader_whenNotConfigured_shouldFormatWithNull() {
        // Given
        SettingValue apiKeySetting = new SettingValue(null, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When
        String actualHeader = jellyfinConfig.getAuthHeader();

        // Then
        // Note: getAuthHeader() doesn't call throwIfNotConfigured(), so it formats with null
        assertNotNull(actualHeader);
        assertEquals("MediaBrowser Token=\"null\"", actualHeader);
    }

    @Test
    void throwIfNotConfigured_whenConfigured_shouldNotThrow() {
        // Given
        SettingValue urlSetting = new SettingValue("http://localhost:8096", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        assertDoesNotThrow(() -> jellyfinConfig.throwIfNotConfigured());
    }

    @Test
    void throwIfNotConfigured_whenNotConfigured_shouldThrowJellyfinIsNotConfiguredException() {
        // Given
        SettingValue urlSetting = new SettingValue(null, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue(null, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        JellyfinConfigExceptions.JellyfinIsNotConfiguredException exception = assertThrows(
                JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class,
                () -> jellyfinConfig.throwIfNotConfigured()
        );

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Jellyfin is not properly configured"));
    }

    @Test
    void throwIfNotConfigured_withMissingUrl_shouldThrowException() {
        // Given
        SettingValue urlSetting = new SettingValue(null, "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class,
                () -> jellyfinConfig.throwIfNotConfigured());
    }

    @Test
    void throwIfNotConfigured_withMissingApiKey_shouldThrowException() {
        // Given
        SettingValue urlSetting = new SettingValue("http://localhost:8096", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue(null, "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class,
                () -> jellyfinConfig.throwIfNotConfigured());
    }

    @Test
    void throwIfNotConfigured_withEmptyUrl_shouldThrowException() {
        // Given
        SettingValue urlSetting = new SettingValue("", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class,
                () -> jellyfinConfig.throwIfNotConfigured());
    }

    @Test
    void throwIfNotConfigured_withEmptyApiKey_shouldThrowException() {
        // Given
        SettingValue urlSetting = new SettingValue("http://localhost:8096", "Jellyfin server base URL");
        SettingValue apiKeySetting = new SettingValue("", "Jellyfin API key");
        when(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL)).thenReturn(urlSetting);
        when(settingsService.getSetting(SettingsService.JELLYFIN_API_KEY)).thenReturn(apiKeySetting);

        // When/Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class,
                () -> jellyfinConfig.throwIfNotConfigured());
    }
}
