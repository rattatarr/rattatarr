package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.CreateSettingsRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SettingWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SettingsWrapper;
import com.rattatarr.rattatarr.services.SettingsService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/settings")
@ApiVersion("v1")
@NullMarked
public class SettingsController extends BaseController {
    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<SettingsWrapper> getAllSettings() {
        logger.debug("Fetching all settings");

        return ResponseEntity.ok(SettingsWrapper.fromMap(
                settingsService.getAllSettings()
        ));
    }

    @GetMapping("/{key}")
    public ResponseEntity<SettingWrapper> getSettingByKey(
            @PathVariable String key
    ) {
        logger.debug("Fetching setting with key: {}", key);

        return ResponseEntity.ok(SettingWrapper.fromMap(
                Map.of(key, settingsService.getSetting(key))
        ));
    }

    @PutMapping()
    public ResponseEntity<SettingsWrapper> updateSettings(
            @RequestBody CreateSettingsRequestDTO requestDTO
    ) {
        logger.debug("Updating settings: {}", requestDTO.settings());

        requestDTO.settings().forEach(setting ->
                settingsService.updateSetting(
                        setting.key(),
                        setting.value(),
                        setting.description()
                )
        );

        return getAllSettings();
    }

}
