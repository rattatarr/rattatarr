package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import com.rattatarr.rattatarr.services.SonarrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SonarrControllerTest {

    @Mock
    private SonarrService sonarrService;

    @Mock
    private BackgroundJobService backgroundJobService;

    @InjectMocks
    private SonarrController sonarrController;

    @Test
    void testConnection_whenSuccessful_shouldReturnSuccess() {
        when(sonarrService.testConnection(ArrInstance.DEFAULT)).thenReturn(true);

        var result = sonarrController.testConnection(ArrInstance.DEFAULT);

        assertEquals(HttpStatus.OK, result.status());
        verify(sonarrService).testConnection(ArrInstance.DEFAULT);
    }

    @Test
    void testConnection_whenFailed_shouldReturnFailure() {
        when(sonarrService.testConnection(ArrInstance.DEFAULT)).thenReturn(false);

        var result = sonarrController.testConnection(ArrInstance.DEFAULT);

        assertEquals(HttpStatus.BAD_REQUEST, result.status());
        verify(sonarrService).testConnection(ArrInstance.DEFAULT);
    }

    @Test
    void importSeries_shouldCreateJobAndTriggerBackgroundImport() {
        var job = new BackgroundJob(JobType.SONARR_IMPORT, null);
        when(backgroundJobService.create(eq(JobType.SONARR_IMPORT), eq(null))).thenReturn(job);

        var result = sonarrController.importSeries(ArrInstance.DEFAULT);

        assertNotNull(result);
        assertEquals(JobType.SONARR_IMPORT, result.type());
        assertEquals(JobStatus.PENDING, result.status());
        verify(backgroundJobService).create(JobType.SONARR_IMPORT, null);
        verify(sonarrService).triggerBackgroundImport(job, ArrInstance.DEFAULT);
    }

    @Test
    void importSeries_anime_shouldCreateAnimeJobAndTriggerBackgroundImport() {
        var job = new BackgroundJob(JobType.SONARR_ANIME_IMPORT, null);
        when(backgroundJobService.create(eq(JobType.SONARR_ANIME_IMPORT), eq(null))).thenReturn(job);

        var result = sonarrController.importSeries(ArrInstance.ANIME);

        assertNotNull(result);
        assertEquals(JobType.SONARR_ANIME_IMPORT, result.type());
        verify(backgroundJobService).create(JobType.SONARR_ANIME_IMPORT, null);
        verify(sonarrService).triggerBackgroundImport(job, ArrInstance.ANIME);
    }
}
