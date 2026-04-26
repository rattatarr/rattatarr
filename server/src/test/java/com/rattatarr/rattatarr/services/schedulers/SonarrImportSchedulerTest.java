package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.services.SonarrService;
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
class SonarrImportSchedulerTest {

    @Mock
    private SonarrService sonarrService;

    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private SonarrImportScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(settingsService.getBooleanSetting(
                eq(SettingsService.SYNC_SONARR_ENABLED), eq(false)
        )).thenReturn(true);
    }

    @Test
    void scheduledSonarrImport_shouldRunImportWhenEnabled() {
        scheduler.scheduledSonarrImport();

        verify(settingsService).getBooleanSetting(eq(SettingsService.SYNC_SONARR_ENABLED), eq(false));
        verify(sonarrService).runImport();
    }

    @Test
    void scheduledSonarrImport_shouldSkipWhenDisabled() {
        when(settingsService.getBooleanSetting(
                eq(SettingsService.SYNC_SONARR_ENABLED), eq(false)
        )).thenReturn(false);

        scheduler.scheduledSonarrImport();

        verify(sonarrService, never()).runImport();
    }

    @Test
    void scheduledSonarrImport_shouldHandleImportError() {
        doThrow(new RuntimeException("Sonarr unreachable")).when(sonarrService).runImport();

        scheduler.scheduledSonarrImport();

        verify(sonarrService).runImport();
    }
}
