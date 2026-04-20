package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.services.RadarrService;
import com.rattatarr.rattatarr.services.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RadarrImportSchedulerTest {

    @Mock
    private RadarrService radarrService;

    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private RadarrImportScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(settingsService.getBooleanSetting(
                eq(SettingsService.SYNC_RADARR_ENABLED), eq(false)
        )).thenReturn(true);
    }

    @Test
    void scheduledRadarrImport_shouldRunImportWhenEnabled() {
        scheduler.scheduledRadarrImport();

        verify(settingsService).getBooleanSetting(eq(SettingsService.SYNC_RADARR_ENABLED), eq(false));
        verify(radarrService).runImport();
    }

    @Test
    void scheduledRadarrImport_shouldSkipWhenDisabled() {
        when(settingsService.getBooleanSetting(
                eq(SettingsService.SYNC_RADARR_ENABLED), eq(false)
        )).thenReturn(false);

        scheduler.scheduledRadarrImport();

        verify(radarrService, never()).runImport();
    }

    @Test
    void scheduledRadarrImport_shouldHandleImportError() {
        doThrow(new RuntimeException("Radarr unreachable")).when(radarrService).runImport();

        // Should not propagate — caught internally
        scheduler.scheduledRadarrImport();

        verify(radarrService).runImport();
    }
}
