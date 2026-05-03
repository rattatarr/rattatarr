package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.services.SonarrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SonarrImportSchedulerTest {

    @Mock
    private SonarrService sonarrService;

    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private SonarrImportScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_SONARR_ENABLED), eq(false))).thenReturn(true);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_SONARR_ANIME_ENABLED), eq(false))).thenReturn(false);
    }

    @Test
    void scheduledSonarrImport_shouldRunDefaultImportWhenEnabled() {
        scheduler.scheduledSonarrImport();

        verify(settingsService).getBooleanSetting(eq(SettingsService.SYNC_SONARR_ENABLED), eq(false));
        verify(sonarrService).runImport(ArrInstance.DEFAULT);
        verify(sonarrService, never()).runImport(ArrInstance.ANIME);
    }

    @Test
    void scheduledSonarrImport_shouldRunAnimeImportWhenEnabled() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_SONARR_ENABLED), eq(false))).thenReturn(false);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_SONARR_ANIME_ENABLED), eq(false))).thenReturn(true);

        scheduler.scheduledSonarrImport();

        verify(sonarrService, never()).runImport(ArrInstance.DEFAULT);
        verify(sonarrService).runImport(ArrInstance.ANIME);
    }

    @Test
    void scheduledSonarrImport_shouldRunBothWhenBothEnabled() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_SONARR_ANIME_ENABLED), eq(false))).thenReturn(true);

        scheduler.scheduledSonarrImport();

        verify(sonarrService).runImport(ArrInstance.DEFAULT);
        verify(sonarrService).runImport(ArrInstance.ANIME);
    }

    @Test
    void scheduledSonarrImport_shouldSkipWhenAllDisabled() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_SONARR_ENABLED), eq(false))).thenReturn(false);

        scheduler.scheduledSonarrImport();

        verify(sonarrService, never()).runImport(any());
    }

    @Test
    void scheduledSonarrImport_shouldHandleImportError() {
        doThrow(new RuntimeException("Sonarr unreachable")).when(sonarrService).runImport(ArrInstance.DEFAULT);

        scheduler.scheduledSonarrImport();

        verify(sonarrService).runImport(ArrInstance.DEFAULT);
    }
}
