package com.rattatarr.rattatarr.clients.tmdb;

import com.rattatarr.rattatarr.exceptions.TMDbConfigExceptions;
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
class TMDbConfigTest {

    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private TMDbConfig tmDbConfig;

    // ==================== getBaseUrl() Tests ====================

    @Test
    void getBaseUrl_withSettingPresent_shouldReturnUrl() {
        // Given
        String expectedUrl = "https://api.themoviedb.org";
        SettingValue settingValue = new SettingValue(expectedUrl, "TMDb base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = tmDbConfig.getBaseUrl();

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        verify(settingsService).getSetting(SettingsService.TMDB_BASE_URL);
    }

    @Test
    void getBaseUrl_withSettingMissing_shouldReturnNull() {
        // Given
        SettingValue settingValue = new SettingValue(null, "TMDb base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = tmDbConfig.getBaseUrl();

        // Then
        assertNull(actualUrl);
        verify(settingsService).getSetting(SettingsService.TMDB_BASE_URL);
    }

    @Test
    void getBaseUrl_withTrailingSlash_shouldRemoveTrailingSlash() {
        // Given
        String urlWithTrailingSlash = "https://api.themoviedb.org/";
        String expectedUrl = "https://api.themoviedb.org";
        SettingValue settingValue = new SettingValue(urlWithTrailingSlash, "TMDb base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = tmDbConfig.getBaseUrl();

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        assertFalse(actualUrl.endsWith("/"));
        verify(settingsService).getSetting(SettingsService.TMDB_BASE_URL);
    }

    // ==================== getApiKey() Tests ====================

    @Test
    void getApiKey_withSettingPresent_shouldReturnKey() {
        // Given
        String expectedApiKey = "test-api-key-12345";
        SettingValue settingValue = new SettingValue(expectedApiKey, "TMDb API key");
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(settingValue);

        // When
        String actualApiKey = tmDbConfig.getApiKey();

        // Then
        assertNotNull(actualApiKey);
        assertEquals(expectedApiKey, actualApiKey);
        verify(settingsService).getSetting(SettingsService.TMDB_API_KEY);
    }

    @Test
    void getApiKey_withSettingMissing_shouldReturnNull() {
        // Given
        SettingValue settingValue = new SettingValue(null, "TMDb API key");
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(settingValue);

        // When
        String actualApiKey = tmDbConfig.getApiKey();

        // Then
        assertNull(actualApiKey);
        verify(settingsService).getSetting(SettingsService.TMDB_API_KEY);
    }

    // ==================== getImageBaseUrl() Tests ====================

    @Test
    void getImageBaseUrl_withSettingPresent_shouldReturnUrl() {
        // Given
        String expectedUrl = "https://image.tmdb.org/t/p";
        SettingValue settingValue = new SettingValue(expectedUrl, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = tmDbConfig.getImageBaseUrl();

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        verify(settingsService).getSetting(SettingsService.TMDB_IMAGE_BASE_URL);
    }

    @Test
    void getImageBaseUrl_withSettingMissing_shouldReturnNull() {
        // Given
        SettingValue settingValue = new SettingValue(null, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = tmDbConfig.getImageBaseUrl();

        // Then
        assertNull(actualUrl);
        verify(settingsService).getSetting(SettingsService.TMDB_IMAGE_BASE_URL);
    }

    @Test
    void getImageBaseUrl_withTrailingSlash_shouldRemoveTrailingSlash() {
        // Given
        String urlWithTrailingSlash = "https://image.tmdb.org/t/p/";
        String expectedUrl = "https://image.tmdb.org/t/p";
        SettingValue settingValue = new SettingValue(urlWithTrailingSlash, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(settingValue);

        // When
        String actualUrl = tmDbConfig.getImageBaseUrl();

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        assertFalse(actualUrl.endsWith("/"));
        verify(settingsService).getSetting(SettingsService.TMDB_IMAGE_BASE_URL);
    }

    // ==================== isConfigured() Tests ====================

    @Test
    void isConfigured_withAllSettings_shouldReturnTrue() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertTrue(isConfigured);
        verify(settingsService).getSetting(SettingsService.TMDB_BASE_URL);
        verify(settingsService).getSetting(SettingsService.TMDB_API_KEY);
        verify(settingsService).getSetting(SettingsService.TMDB_IMAGE_BASE_URL);
    }

    @Test
    void isConfigured_withMissingBaseUrl_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue(null, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withMissingApiKey_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue(null, "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withMissingImageBaseUrl_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(null, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withAllMissing_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue(null, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue(null, "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(null, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withEmptyBaseUrl_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withEmptyApiKey_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withEmptyImageBaseUrl_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withBlankBaseUrl_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("   ", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withBlankApiKey_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("   ", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    @Test
    void isConfigured_withBlankImageBaseUrl_shouldReturnFalse() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("   ", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        boolean isConfigured = tmDbConfig.isConfigured();

        // Then
        assertFalse(isConfigured);
    }

    // ==================== buildUrl() Tests ====================

    @Test
    void buildUrl_withValidConfig_shouldCombineUrlAndPath() {
        // Given
        String baseUrl = "https://api.themoviedb.org";
        String path = "/3/movie/123";
        String expectedUrl = "https://api.themoviedb.org/3/movie/123";
        SettingValue baseUrlSetting = new SettingValue(baseUrl, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        String actualUrl = tmDbConfig.buildUrl(path);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void buildUrl_withLeadingSlashInPath_shouldHandleCorrectly() {
        // Given
        String baseUrl = "https://api.themoviedb.org";
        String path = "/3/tv/456";
        String expectedUrl = "https://api.themoviedb.org/3/tv/456";
        SettingValue baseUrlSetting = new SettingValue(baseUrl, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        String actualUrl = tmDbConfig.buildUrl(path);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        assertFalse(actualUrl.contains("//3"));
    }

    @Test
    void buildUrl_withoutLeadingSlashInPath_shouldAddSlash() {
        // Given
        String baseUrl = "https://api.themoviedb.org";
        String path = "3/search/movie";
        String expectedUrl = "https://api.themoviedb.org/3/search/movie";
        SettingValue baseUrlSetting = new SettingValue(baseUrl, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        String actualUrl = tmDbConfig.buildUrl(path);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        assertTrue(actualUrl.contains("/3/search"));
    }

    @Test
    void buildUrl_whenNotConfigured_shouldThrowException() {
        // Given
        SettingValue baseUrlSetting = new SettingValue(null, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue(null, "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(null, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When/Then
        assertThrows(TMDbConfigExceptions.TMDbIsNotConfiguredException.class,
                () -> tmDbConfig.buildUrl("/3/movie/123"));
    }

    @Test
    void buildUrl_withMissingBaseUrl_shouldThrowException() {
        // Given
        SettingValue baseUrlSetting = new SettingValue(null, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When/Then
        assertThrows(TMDbConfigExceptions.TMDbIsNotConfiguredException.class,
                () -> tmDbConfig.buildUrl("/3/movie/123"));
    }

    // ==================== getAuthHeader() Tests ====================

    @Test
    void getAuthHeader_withValidConfig_shouldFormatCorrectly() {
        // Given
        String apiKey = "test-api-key-12345";
        String expectedHeader = "Bearer test-api-key-12345";
        SettingValue apiKeySetting = new SettingValue(apiKey, "TMDb API key");
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);

        // When
        String actualHeader = tmDbConfig.getAuthHeader();

        // Then
        assertNotNull(actualHeader);
        assertEquals(expectedHeader, actualHeader);
        assertTrue(actualHeader.startsWith("Bearer "));
        assertTrue(actualHeader.contains(apiKey));
    }

    @Test
    void getAuthHeader_whenNotConfigured_shouldFormatWithNull() {
        // Given
        SettingValue apiKeySetting = new SettingValue(null, "TMDb API key");
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);

        // When
        String actualHeader = tmDbConfig.getAuthHeader();

        // Then
        assertNotNull(actualHeader);
        assertEquals("Bearer null", actualHeader);
    }

    // ==================== getImageUrl() Tests ====================

    @Test
    void getImageUrl_withValidConfigAndDefaultSize_shouldFormatCorrectly() {
        // Given
        String imageBaseUrl = "https://image.tmdb.org/t/p";
        String imagePath = "/abc123.jpg";
        String expectedUrl = "https://image.tmdb.org/t/p/w500/abc123.jpg";
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(imageBaseUrl, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        String actualUrl = tmDbConfig.getImageUrl(imagePath, null);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void getImageUrl_withValidConfigAndCustomSize_shouldFormatCorrectly() {
        // Given
        String imageBaseUrl = "https://image.tmdb.org/t/p";
        String imagePath = "/xyz789.jpg";
        String size = "w780";
        String expectedUrl = "https://image.tmdb.org/t/p/w780/xyz789.jpg";
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(imageBaseUrl, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        String actualUrl = tmDbConfig.getImageUrl(imagePath, size);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
        assertTrue(actualUrl.contains(size));
    }

    @Test
    void getImageUrl_withOriginalSize_shouldFormatCorrectly() {
        // Given
        String imageBaseUrl = "https://image.tmdb.org/t/p";
        String imagePath = "/poster.jpg";
        String size = "original";
        String expectedUrl = "https://image.tmdb.org/t/p/original/poster.jpg";
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(imageBaseUrl, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When
        String actualUrl = tmDbConfig.getImageUrl(imagePath, size);

        // Then
        assertNotNull(actualUrl);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void getImageUrl_whenNotConfigured_shouldThrowException() {
        // Given
        SettingValue baseUrlSetting = new SettingValue(null, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue(null, "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(null, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When/Then
        assertThrows(TMDbConfigExceptions.TMDbIsNotConfiguredException.class,
                () -> tmDbConfig.getImageUrl("/poster.jpg", null));
    }

    // ==================== throwIfNotConfigured() Tests ====================

    @Test
    void throwIfNotConfigured_whenConfigured_shouldNotThrow() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When/Then
        assertDoesNotThrow(() -> tmDbConfig.throwIfNotConfigured());
    }

    @Test
    void throwIfNotConfigured_whenNotConfigured_shouldThrowTMDbIsNotConfiguredException() {
        // Given
        SettingValue baseUrlSetting = new SettingValue(null, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue(null, "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(null, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When/Then
        TMDbConfigExceptions.TMDbIsNotConfiguredException exception = assertThrows(
                TMDbConfigExceptions.TMDbIsNotConfiguredException.class,
                () -> tmDbConfig.throwIfNotConfigured()
        );

        assertNotNull(exception);
    }

    @Test
    void throwIfNotConfigured_withMissingBaseUrl_shouldThrowException() {
        // Given
        SettingValue baseUrlSetting = new SettingValue(null, "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When/Then
        assertThrows(TMDbConfigExceptions.TMDbIsNotConfiguredException.class,
                () -> tmDbConfig.throwIfNotConfigured());
    }

    @Test
    void throwIfNotConfigured_withMissingApiKey_shouldThrowException() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue(null, "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue("https://image.tmdb.org/t/p", "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When/Then
        assertThrows(TMDbConfigExceptions.TMDbIsNotConfiguredException.class,
                () -> tmDbConfig.throwIfNotConfigured());
    }

    @Test
    void throwIfNotConfigured_withMissingImageBaseUrl_shouldThrowException() {
        // Given
        SettingValue baseUrlSetting = new SettingValue("https://api.themoviedb.org", "TMDb base URL");
        SettingValue apiKeySetting = new SettingValue("test-api-key-12345", "TMDb API key");
        SettingValue imageUrlSetting = new SettingValue(null, "TMDb image base URL");
        when(settingsService.getSetting(SettingsService.TMDB_BASE_URL)).thenReturn(baseUrlSetting);
        when(settingsService.getSetting(SettingsService.TMDB_API_KEY)).thenReturn(apiKeySetting);
        when(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL)).thenReturn(imageUrlSetting);

        // When/Then
        assertThrows(TMDbConfigExceptions.TMDbIsNotConfiguredException.class,
                () -> tmDbConfig.throwIfNotConfigured());
    }
}
