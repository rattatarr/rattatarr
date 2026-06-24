package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.sonarr.SonarrClient;
import com.rattatarr.rattatarr.clients.sonarr.responses.SonarrSeriesResponseDTO;
import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SonarrServiceTest {

    @Mock
    private SonarrClient defaultClient;

    @Mock
    private SonarrClient animeClient;

    @Mock
    private TMDbService tmDbService;

    @Mock
    private BackgroundJobService backgroundJobService;

    @Mock
    private Executor tmdbApiExecutor;

    private SonarrService sonarrService;

    private MediaItem seriesItem;

    private SonarrSeriesResponseDTO seriesWithTmdbId;
    private SonarrSeriesResponseDTO seriesNoTmdbId;

    @BeforeEach
    void setUp() {
        sonarrService = new SonarrService(
                defaultClient, animeClient,
                tmDbService, backgroundJobService, tmdbApiExecutor
        );

        seriesItem = new MediaItem(
                MediaType.SERIES, "Breaking Bad", null, "1396", null,
                2008, null, Set.of(), Set.of(), Set.of(), Set.of()
        );
        ReflectionTestUtils.setField(seriesItem, "id", UUID.randomUUID());

        seriesWithTmdbId = new SonarrSeriesResponseDTO(1, "Breaking Bad", 2008, 81189, 1396);
        seriesNoTmdbId = new SonarrSeriesResponseDTO(2, "No TMDb Show", 2020, 99999, null);

        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(tmdbApiExecutor).execute(any());
    }

    // --- importAllSeries ---

    @Test
    void importAllSeries_withValidSeries_shouldImportEach() {
        when(tmDbService.importMediaItem("1396", MediaType.SERIES)).thenReturn(seriesItem);

        sonarrService.importAllSeries(List.of(seriesWithTmdbId));

        verify(tmDbService).importMediaItem("1396", MediaType.SERIES);
    }

    @Test
    void importAllSeries_withMissingTmdbId_shouldSkip() {
        sonarrService.importAllSeries(List.of(seriesNoTmdbId));

        verify(tmDbService, never()).importMediaItem(anyString(), any());
    }

    @Test
    void importAllSeries_withEmptyList_shouldNotProcess() {
        sonarrService.importAllSeries(List.of());

        verify(tmDbService, never()).importMediaItem(anyString(), any());
    }

    @Test
    void importAllSeries_withMixedList_shouldImportOnlyThoseWithTmdbId() {
        when(tmDbService.importMediaItem("1396", MediaType.SERIES)).thenReturn(seriesItem);

        sonarrService.importAllSeries(List.of(seriesWithTmdbId, seriesNoTmdbId));

        verify(tmDbService, times(1)).importMediaItem("1396", MediaType.SERIES);
        verify(tmDbService, never()).importMediaItem(isNull(), any());
    }

    // --- runImport ---

    @Test
    void runImport_whenNotConfigured_shouldSkip() {
        when(defaultClient.isConfigured()).thenReturn(false);

        sonarrService.runImport(ArrInstance.DEFAULT);

        verify(defaultClient, never()).getMonitoredInternalSeries();
    }

    @Test
    void runImport_whenConfigured_shouldFetchAndImport() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(defaultClient.getMonitoredInternalSeries()).thenReturn(List.of(seriesWithTmdbId));
        when(tmDbService.importMediaItem("1396", MediaType.SERIES)).thenReturn(seriesItem);

        sonarrService.runImport(ArrInstance.DEFAULT);

        verify(defaultClient).getMonitoredInternalSeries();
        verify(tmDbService).importMediaItem("1396", MediaType.SERIES);
    }

    // --- triggerBackgroundImport ---

    @Test
    void triggerBackgroundImport_onSuccess_shouldMarkCompleted() {
        BackgroundJob job = new BackgroundJob(JobType.SONARR_IMPORT, null);
        ReflectionTestUtils.setField(job, "id", UUID.randomUUID());
        when(defaultClient.isConfigured()).thenReturn(true);
        when(defaultClient.getMonitoredInternalSeries()).thenReturn(List.of(seriesWithTmdbId));
        when(tmDbService.importMediaItem("1396", MediaType.SERIES)).thenReturn(seriesItem);

        sonarrService.triggerBackgroundImport(job, ArrInstance.DEFAULT);

        verify(backgroundJobService).markRunning(job);
        verify(backgroundJobService).markCompleted(eq(job), anyString());
        verify(backgroundJobService, never()).markFailed(any(), any());
    }

    @Test
    void triggerBackgroundImport_onFailure_shouldMarkFailed() {
        BackgroundJob job = new BackgroundJob(JobType.SONARR_IMPORT, null);
        ReflectionTestUtils.setField(job, "id", UUID.randomUUID());
        when(defaultClient.isConfigured()).thenReturn(true);
        when(defaultClient.getMonitoredInternalSeries()).thenThrow(new RuntimeException("Connection refused"));

        sonarrService.triggerBackgroundImport(job, ArrInstance.DEFAULT);

        verify(backgroundJobService).markRunning(job);
        verify(backgroundJobService).markFailed(eq(job), anyString());
        verify(backgroundJobService, never()).markCompleted(any(), any());
    }
}
