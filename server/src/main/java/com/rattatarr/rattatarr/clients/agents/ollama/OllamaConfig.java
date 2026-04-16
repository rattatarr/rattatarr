package com.rattatarr.rattatarr.clients.agents.ollama;

import com.rattatarr.rattatarr.exceptions.AgentExceptions;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.utils.URISanitizer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@NullMarked
public class OllamaConfig {

    private final SettingsService settingsService;

    public OllamaConfig(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Nullable
    public String getBaseUrl() {
        return URISanitizer.removeTrailingSlash(
                settingsService.getSetting(SettingsService.AGENTS_OLLAMA_BASE_URL).value());
    }

    @Nullable
    public String getApiKey() {
        return settingsService.getSetting(SettingsService.AGENTS_OLLAMA_API_KEY).value();
    }

    public String getModel() {
        String model = settingsService.getSetting(SettingsService.AGENTS_OLLAMA_MODEL).value();
        return StringUtils.hasText(model) ? model : "llama3.2";
    }

    public boolean isConfigured() {
        return StringUtils.hasText(getBaseUrl());
    }

    public void throwIfNotConfigured() {
        if (!isConfigured()) {
            throw new AgentExceptions.AgentNotConfiguredException("ollama");
        }
    }
}
