package com.rattatarr.rattatarr.clients.radarr;

import com.rattatarr.rattatarr.clients.BaseClientConfig;
import com.rattatarr.rattatarr.exceptions.RadarrConfigExceptions;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.utils.URISanitizer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
@NullMarked
public class RadarrConfig extends BaseClientConfig {
    private final SettingsService settingsService;

    public RadarrConfig(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Nullable
    public String getBaseUrl() {
        return URISanitizer.removeTrailingSlash(settingsService.getSetting(SettingsService.RADARR_BASE_URL).value());
    }

    @Nullable
    public String getApiKey() {
        return settingsService.getSetting(SettingsService.RADARR_API_KEY).value();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(getBaseUrl()) && StringUtils.hasText(getApiKey());
    }

    public void throwIfNotConfigured() {
        if (!isConfigured()) {
            throw new RadarrConfigExceptions.RadarrIsNotConfiguredException(getBaseUrl(), getApiKey());
        }
    }

    public String buildUrl(String path) {
        throwIfNotConfigured();
        return getBaseUrl() + URISanitizer.pathEnsureLeadingSlash(path);
    }

    public String getAuthHeader() {
        return Objects.requireNonNull(getApiKey());
    }
}
