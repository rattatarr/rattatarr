package com.rattatarr.rattatarr.clients.sonarr;

import com.rattatarr.rattatarr.clients.BaseClientConfig;
import com.rattatarr.rattatarr.exceptions.SonarrConfigExceptions;
import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.utils.URISanitizer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.util.Objects;

@NullMarked
public class SonarrConfig extends BaseClientConfig {
    private final SettingsService settingsService;
    private final ArrInstance instance;

    public SonarrConfig(SettingsService settingsService, ArrInstance instance) {
        this.settingsService = settingsService;
        this.instance = instance;
    }

    @Nullable
    public String getBaseUrl() {
        return URISanitizer.removeTrailingSlash(settingsService.getSetting(baseUrlKey()).value());
    }

    @Nullable
    public String getApiKey() {
        return settingsService.getSetting(apiKeyKey()).value();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(getBaseUrl()) && StringUtils.hasText(getApiKey());
    }

    public void throwIfNotConfigured() {
        if (!isConfigured()) {
            throw new SonarrConfigExceptions.SonarrIsNotConfiguredException(instance, getBaseUrl(), getApiKey());
        }
    }

    public String buildUrl(String path) {
        throwIfNotConfigured();
        return getBaseUrl() + "/api/v3" + URISanitizer.pathEnsureLeadingSlash(path);
    }

    public String getAuthHeader() {
        return Objects.requireNonNull(getApiKey());
    }

    private String baseUrlKey() {
        return instance == ArrInstance.ANIME ? SettingsService.SONARR_ANIME_BASE_URL : SettingsService.SONARR_BASE_URL;
    }

    private String apiKeyKey() {
        return instance == ArrInstance.ANIME ? SettingsService.SONARR_ANIME_API_KEY : SettingsService.SONARR_API_KEY;
    }
}
