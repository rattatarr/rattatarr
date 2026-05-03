package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.services.RadarrService;
import com.rattatarr.rattatarr.services.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RadarrImportSchedulerTest {

    @Mock
    private RadarrService radarrService;

    @Mock
    private SettingsService settingsService;

    private RadarrImportScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new RadarrImportScheduler(radarrService, settingsService, Duration.ZERO);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_RADARR_ENABLED), eq(false))).thenReturn(true);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_RADARR_ANIME_ENABLED), eq(false))).thenReturn(false);
    }

    @Test
    void scheduledRadarrImport_shouldRunDefaultImportWhenEnabled() {
        scheduler.scheduledRadarrImport();

        verify(settingsService).getBooleanSetting(eq(SettingsService.SYNC_RADARR_ENABLED), eq(false));
        verify(radarrService).runImport(ArrInstance.DEFAULT);
        verify(radarrService, never()).runImport(ArrInstance.ANIME);
    }

    @Test
    void scheduledRadarrImport_shouldRunAnimeImportWhenEnabled() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_RADARR_ENABLED), eq(false))).thenReturn(false);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_RADARR_ANIME_ENABLED), eq(false))).thenReturn(true);

        scheduler.scheduledRadarrImport();

        verify(radarrService, never()).runImport(ArrInstance.DEFAULT);
        verify(radarrService).runImport(ArrInstance.ANIME);
    }

    @Test
    void scheduledRadarrImport_shouldRunBothWhenBothEnabled() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_RADARR_ANIME_ENABLED), eq(false))).thenReturn(true);

        scheduler.scheduledRadarrImport();

        verify(radarrService).runImport(ArrInstance.DEFAULT);
        verify(radarrService).runImport(ArrInstance.ANIME);
    }

    @Test
    void scheduledRadarrImport_shouldSkipWhenAllDisabled() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_RADARR_ENABLED), eq(false))).thenReturn(false);

        scheduler.scheduledRadarrImport();

        verify(radarrService, never()).runImport(any());
    }

    @Test
    void scheduledRadarrImport_shouldHandleImportError() {
        doThrow(new RuntimeException("Radarr unreachable")).when(radarrService).runImport(ArrInstance.DEFAULT);

        scheduler.scheduledRadarrImport();

        verify(radarrService).runImport(ArrInstance.DEFAULT);
    }

    @Test
    void scheduledRadarrRatingsRefresh_shouldRunDefaultRefreshWhenEnabled() {
        scheduler.scheduledRadarrRatingsRefresh();

        verify(settingsService).getBooleanSetting(eq(SettingsService.SYNC_RADARR_ENABLED), eq(false));
        verify(radarrService).runRatingsRefresh(ArrInstance.DEFAULT);
        verify(radarrService, never()).runRatingsRefresh(ArrInstance.ANIME);
    }

    @Test
    void scheduledRadarrRatingsRefresh_shouldSkipWhenDisabled() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_RADARR_ENABLED), eq(false))).thenReturn(false);

        scheduler.scheduledRadarrRatingsRefresh();

        verify(radarrService, never()).runRatingsRefresh(any());
    }

    @Test
    void scheduledRadarrRatingsRefresh_shouldHandleRefreshError() {
        doThrow(new RuntimeException("Radarr unreachable")).when(radarrService).runRatingsRefresh(ArrInstance.DEFAULT);

        scheduler.scheduledRadarrRatingsRefresh();

        verify(radarrService).runRatingsRefresh(ArrInstance.DEFAULT);
    }
}
