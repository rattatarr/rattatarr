package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.SettingsExceptions;
import com.rattatarr.rattatarr.models.SettingValue;
import com.rattatarr.rattatarr.models.entities.Setting;
import com.rattatarr.rattatarr.repositories.SettingsRepository;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@NullMarked
public class SettingsService extends BaseService<Setting, SettingsRepository> {
    public static final String JELLYFIN_BASE_URL = "jellyfin.base_url";
    public static final String JELLYFIN_API_KEY = "jellyfin.api_key";

    public static final String TMDB_BASE_URL = "tmdb.base_url";
    public static final String TMDB_API_KEY = "tmdb.api_key";
    public static final String TMDB_IMAGE_BASE_URL = "tmdb.image_base_url";

    public static final String SYNC_JELLYFIN_ENABLED = "sync.jellyfin_enabled";
    public static final String SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE = "sync.jellyfin_activity_backfill_complete";
    public static final String SYNC_STALE_THRESHOLD = "sync.stale_threshold";
    public static final String SYNC_AUTO_REFRESH_ON_READ = "sync.auto_refresh_on_read";

    public static final String RADARR_BASE_URL = "radarr.base_url";
    public static final String RADARR_API_KEY = "radarr.api_key";
    public static final String SYNC_RADARR_ENABLED = "sync.radarr_enabled";
    public static final String RADARR_ANIME_BASE_URL = "radarr.anime.base_url";
    public static final String RADARR_ANIME_API_KEY = "radarr.anime.api_key";
    public static final String SYNC_RADARR_ANIME_ENABLED = "sync.radarr.anime.enabled";

    public static final String SONARR_BASE_URL = "sonarr.base_url";
    public static final String SONARR_API_KEY = "sonarr.api_key";
    public static final String SYNC_SONARR_ENABLED = "sync.sonarr_enabled";
    public static final String SONARR_ANIME_BASE_URL = "sonarr.anime.base_url";
    public static final String SONARR_ANIME_API_KEY = "sonarr.anime.api_key";
    public static final String SYNC_SONARR_ANIME_ENABLED = "sync.sonarr.anime.enabled";

    public static final String AGENTS_OLLAMA_BASE_URL = "agents.ollama.base_url";
    public static final String AGENTS_OLLAMA_API_KEY = "agents.ollama.api_key";
    public static final String AGENTS_OLLAMA_MODEL = "agents.ollama.model";

    private final Map<String, SettingValue> settingsCache = new ConcurrentHashMap<>();

    public SettingsService(SettingsRepository repository) {
        super(repository);
    }

    @Override
    protected void updateEntity(Setting existing, Setting updated) {
    }

    @PostConstruct
    @Transactional
    public void loadSettingsIntoCache() {
        logger.info("Loading settings from database into cache...");
        List<Setting> settings = repository.findAll();
        settingsCache.clear();

        settings.forEach(setting ->
                settingsCache.put(setting.key(), new SettingValue(setting.value(), setting.description()))
        );

        logger.info("Loaded {} settings into cache.", settingsCache.size());

        initializeDefaultSettings();
    }

    private void initializeDefaultSettings() {
        // Jellyfin settings
        createSettingIfNotExists(JELLYFIN_BASE_URL, null, "Jellyfin server base URL");
        createSettingIfNotExists(JELLYFIN_API_KEY, null, "Jellyfin API key");

        // TMDb settings
        createSettingIfNotExists(TMDB_BASE_URL, "https://api.themoviedb.org", "TMDb API base URL");
        createSettingIfNotExists(TMDB_API_KEY, null, "TMDb API key");
        createSettingIfNotExists(TMDB_IMAGE_BASE_URL, "https://image.tmdb.org/t/p", "TMDb Image base URL");

        // Sync settings
        createSettingIfNotExists(SYNC_JELLYFIN_ENABLED, "true", "Enable scheduled Jellyfin media synchronization");
        createSettingIfNotExists(SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE, "false", "Whether initial Jellyfin activity log backfill has completed");
        createSettingIfNotExists(SYNC_STALE_THRESHOLD, "P2D", "Staleness threshold for TMDb items (ISO-8601 duration)");
        createSettingIfNotExists(SYNC_AUTO_REFRESH_ON_READ, "false", "Auto-refresh stale series when viewing them");

        // Radarr settings
        createSettingIfNotExists(RADARR_BASE_URL, null, "Radarr server base URL (e.g. http://radarr:7878)");
        createSettingIfNotExists(RADARR_API_KEY, null, "Radarr API key");
        createSettingIfNotExists(SYNC_RADARR_ENABLED, "true", "Enable scheduled Radarr import synchronization");
        createSettingIfNotExists(RADARR_ANIME_BASE_URL, null, "Radarr Anime server base URL (e.g. http://radarr-anime:7878)");
        createSettingIfNotExists(RADARR_ANIME_API_KEY, null, "Radarr Anime API key");
        createSettingIfNotExists(SYNC_RADARR_ANIME_ENABLED, "false", "Enable scheduled Radarr Anime import synchronization");

        // Sonarr settings
        createSettingIfNotExists(SONARR_BASE_URL, null, "Sonarr server base URL (e.g. http://sonarr:8989)");
        createSettingIfNotExists(SONARR_API_KEY, null, "Sonarr API key");
        createSettingIfNotExists(SYNC_SONARR_ENABLED, "true", "Enable scheduled Sonarr import synchronization");
        createSettingIfNotExists(SONARR_ANIME_BASE_URL, null, "Sonarr Anime server base URL (e.g. http://sonarr-anime:8989)");
        createSettingIfNotExists(SONARR_ANIME_API_KEY, null, "Sonarr Anime API key");
        createSettingIfNotExists(SYNC_SONARR_ANIME_ENABLED, "false", "Enable scheduled Sonarr Anime import synchronization");

        // Ollama agent settings
        createSettingIfNotExists(AGENTS_OLLAMA_BASE_URL, null, "Ollama base URL (e.g. http://localhost:11434)");
        createSettingIfNotExists(AGENTS_OLLAMA_API_KEY, null, "Ollama API key / bearer token (optional)");
        createSettingIfNotExists(AGENTS_OLLAMA_MODEL, "llama3.2", "Ollama model name");
    }

    protected void createSettingIfNotExists(String key, @Nullable String value, String description) {
        if (repository.findByKey(key).isEmpty()) {
            Setting setting = new Setting(key, value, description);
            repository.save(setting);
            settingsCache.put(key, new SettingValue(value, description));
            logger.info("Created setting '{}' with default value.", key);
        }
    }

    @Transactional(readOnly = true)
    public SettingValue getSetting(String key) {
        SettingValue settingValue = settingsCache.get(key);
        if (settingValue == null) {
            Setting setting = repository.findByKey(key)
                    .orElseThrow(() -> new SettingsExceptions.SettingNotFoundException(key));
            settingValue = new SettingValue(setting.value(), setting.description());
            settingsCache.put(key, settingValue);
        }
        return settingValue;
    }

    @Transactional(readOnly = true)
    public Map<String, SettingValue> getAllSettings() {
        return Map.copyOf(settingsCache);
    }

    @Transactional
    public void updateSetting(String key, @Nullable String value, @Nullable String description) {
        Setting setting = repository.findByKey(key)
                .orElseThrow(() -> new SettingsExceptions.SettingNotFoundException(key));
        if (description == null) description = setting.description();

        setting.setValue(value);
        setting.setDescription(description);
        repository.save(setting);

        settingsCache.put(key, new SettingValue(value, description));
        logger.info("Updated setting '{}' to new value.", key);
    }

    /**
     * Get a setting value as a Duration.
     * Parses ISO-8601 duration strings (e.g., "PT6H", "P2D").
     */
    @Transactional(readOnly = true)
    public Duration getDurationSetting(String key, Duration defaultValue) {
        try {
            SettingValue settingValue = getSetting(key);
            if (settingValue.value() == null || settingValue.value().isBlank()) {
                logger.warn("Setting '{}' has no value, using default: {}", key, defaultValue);
                return defaultValue;
            }
            return Duration.parse(settingValue.value());
        } catch (SettingsExceptions.SettingNotFoundException e) {
            logger.warn("Setting '{}' not found, using default: {}", key, defaultValue);
            return defaultValue;
        } catch (Exception e) {
            logger.error("Failed to parse duration setting '{}', using default: {}", key, defaultValue, e);
            return defaultValue;
        }
    }

    @Transactional(readOnly = true)
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        try {
            SettingValue settingValue = getSetting(key);
            if (settingValue.value() == null || settingValue.value().isBlank()) {
                logger.warn("Setting '{}' has no value, using default: {}", key, defaultValue);
                return defaultValue;
            }
            return Boolean.parseBoolean(settingValue.value());
        } catch (SettingsExceptions.SettingNotFoundException e) {
            logger.warn("Setting '{}' not found, using default: {}", key, defaultValue);
            return defaultValue;
        } catch (Exception e) {
            logger.error("Failed to parse boolean setting '{}', using default: {}", key, defaultValue, e);
            return defaultValue;
        }
    }
}
