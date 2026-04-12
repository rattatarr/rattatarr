package com.rattatarr.rattatarr.clients.tmdb;

import com.rattatarr.rattatarr.clients.BaseClientConfig;
import com.rattatarr.rattatarr.exceptions.TMDbConfigExceptions;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.utils.URISanitizer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@NullMarked
public class TMDbConfig extends BaseClientConfig {
    private final static String DEFAULT_IMAGE_SIZE = "w500";

    private final SettingsService settingsService;

    public TMDbConfig(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Nullable
    public String getBaseUrl() {
        return URISanitizer.removeTrailingSlash(settingsService.getSetting(SettingsService.TMDB_BASE_URL).value());
    }

    @Nullable
    public String getApiKey() {
        return settingsService.getSetting(SettingsService.TMDB_API_KEY).value();
    }

    @Nullable
    public String getImageBaseUrl() {
        return URISanitizer.removeTrailingSlash(settingsService.getSetting(SettingsService.TMDB_IMAGE_BASE_URL).value());
    }

    public boolean isConfigured() {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();
        String imageBaseUrl = getImageBaseUrl();

        return StringUtils.hasText(baseUrl) && StringUtils.hasText(apiKey) && StringUtils.hasText(imageBaseUrl);
    }

    public void throwIfNotConfigured() {
        if (!isConfigured())
            throw new TMDbConfigExceptions.TMDbIsNotConfiguredException(
                    getBaseUrl(),
                    getApiKey(),
                    getImageBaseUrl()
            );
    }

    public String buildUrl(String path) {
        throwIfNotConfigured();
        return getBaseUrl() + URISanitizer.pathEnsureLeadingSlash(path);
    }

    public String getAuthHeader() {
        return String.format("Bearer %s",
                getApiKey()
        );
    }

    public String getImageUrl(String imagePath, @Nullable String size) {
        throwIfNotConfigured();
        return String.format("%s%s%s",
                getImageBaseUrl(),
                StringUtils.hasText(size) ? URISanitizer.pathEnsureLeadingSlash(size) : URISanitizer.pathEnsureLeadingSlash(DEFAULT_IMAGE_SIZE),
                URISanitizer.pathEnsureLeadingSlash(imagePath)
        );
    }
}
