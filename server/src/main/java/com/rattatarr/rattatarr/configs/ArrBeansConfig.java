package com.rattatarr.rattatarr.configs;

import com.rattatarr.rattatarr.clients.radarr.RadarrClient;
import com.rattatarr.rattatarr.clients.radarr.RadarrConfig;
import com.rattatarr.rattatarr.clients.sonarr.SonarrClient;
import com.rattatarr.rattatarr.clients.sonarr.SonarrConfig;
import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.services.SettingsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ArrBeansConfig {
    @Bean("radarrDefaultConfig")
    public RadarrConfig radarrDefaultConfig(SettingsService settingsService) {
        return new RadarrConfig(settingsService, ArrInstance.DEFAULT);
    }

    @Bean("radarrAnimeConfig")
    public RadarrConfig radarrAnimeConfig(SettingsService settingsService) {
        return new RadarrConfig(settingsService, ArrInstance.ANIME);
    }

    @Bean("radarrDefaultClient")
    public RadarrClient radarrDefaultClient(
            RestClient restClient,
            RestClientProperties properties,
            @Qualifier("radarrDefaultConfig") RadarrConfig config) {
        return new RadarrClient(restClient, properties, config);
    }

    @Bean("radarrAnimeClient")
    public RadarrClient radarrAnimeClient(
            RestClient restClient,
            RestClientProperties properties,
            @Qualifier("radarrAnimeConfig") RadarrConfig config) {
        return new RadarrClient(restClient, properties, config);
    }

    @Bean("sonarrDefaultConfig")
    public SonarrConfig sonarrDefaultConfig(SettingsService settingsService) {
        return new SonarrConfig(settingsService, ArrInstance.DEFAULT);
    }

    @Bean("sonarrAnimeConfig")
    public SonarrConfig sonarrAnimeConfig(SettingsService settingsService) {
        return new SonarrConfig(settingsService, ArrInstance.ANIME);
    }

    @Bean("sonarrDefaultClient")
    public SonarrClient sonarrDefaultClient(
            RestClient restClient,
            RestClientProperties properties,
            @Qualifier("sonarrDefaultConfig") SonarrConfig config) {
        return new SonarrClient(restClient, properties, config);
    }

    @Bean("sonarrAnimeClient")
    public SonarrClient sonarrAnimeClient(
            RestClient restClient,
            RestClientProperties properties,
            @Qualifier("sonarrAnimeConfig") SonarrConfig config) {
        return new SonarrClient(restClient, properties, config);
    }
}
