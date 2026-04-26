package com.rattatarr.rattatarr.clients.sonarr;

import com.rattatarr.rattatarr.clients.BaseClientConfig;
import com.rattatarr.rattatarr.exceptions.SonarrConfigExceptions;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.utils.URISanitizer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
@NullMarked
public class SonarrConfig extends BaseClientConfig {
    private final SettingsService settingsService;

    public SonarrConfig(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Nullable
    public String getBaseUrl() {
        return URISanitizer.removeTrailingSlash(settingsService.getSetting(SettingsService.SONARR_BASE_URL).value());
    }

    @Nullable
    public String getApiKey() {
        return settingsService.getSetting(SettingsService.SONARR_API_KEY).value();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(getBaseUrl()) && StringUtils.hasText(getApiKey());
    }

    public void throwIfNotConfigured() {
        if (!isConfigured()) {
            throw new SonarrConfigExceptions.SonarrIsNotConfiguredException(getBaseUrl(), getApiKey());
        }
    }

    public String buildUrl(String path) {
        throwIfNotConfigured();
        return getBaseUrl() + "/api/v3" + URISanitizer.pathEnsureLeadingSlash(path);
    }

    public String getAuthHeader() {
        return Objects.requireNonNull(getApiKey());
    }
}
