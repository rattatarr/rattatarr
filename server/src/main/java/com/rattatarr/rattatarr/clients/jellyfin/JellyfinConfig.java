package com.rattatarr.rattatarr.clients.jellyfin;

import com.rattatarr.rattatarr.clients.BaseClientConfig;
import com.rattatarr.rattatarr.exceptions.JellyfinConfigExceptions;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.utils.URISanitizer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@NullMarked
public class JellyfinConfig extends BaseClientConfig {
    private final SettingsService settingsService;

    public JellyfinConfig(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Nullable
    public String getBaseUrl() {
        return URISanitizer.removeTrailingSlash(settingsService.getSetting(SettingsService.JELLYFIN_BASE_URL).value());
    }

    @Nullable
    public String getApiKey() {
        return settingsService.getSetting(SettingsService.JELLYFIN_API_KEY).value();
    }

    public boolean isConfigured() {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        return StringUtils.hasText(baseUrl) && StringUtils.hasText(apiKey);
    }

    public void throwIfNotConfigured() {
        if (!isConfigured()) {
            throw new JellyfinConfigExceptions.JellyfinIsNotConfiguredException(
                    getBaseUrl(),
                    getApiKey()
            );
        }
    }


    public String buildUrl(String path) {
        throwIfNotConfigured();
        return getBaseUrl() + (path.startsWith("/") ? path : "/" + path);
    }

    public String getAuthHeader() {
        return String.format("MediaBrowser Token=\"%s\"",
                getApiKey()
        );
    }


}
