package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieLookupResponseDTO;
import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import com.rattatarr.rattatarr.services.RadarrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RadarrControllerTest {

    @Mock
    private RadarrService radarrService;

    @Mock
    private BackgroundJobService backgroundJobService;

    @InjectMocks
    private RadarrController radarrController;

    @Test
    void testConnection_whenSuccessful_shouldReturnSuccess() {
        when(radarrService.testConnection(ArrInstance.DEFAULT)).thenReturn(true);

        var result = radarrController.testConnection(ArrInstance.DEFAULT);

        assertEquals(HttpStatus.OK, result.status());
        verify(radarrService).testConnection(ArrInstance.DEFAULT);
    }

    @Test
    void testConnection_whenFailed_shouldReturnFailure() {
        when(radarrService.testConnection(ArrInstance.DEFAULT)).thenReturn(false);

        var result = radarrController.testConnection(ArrInstance.DEFAULT);

        assertEquals(HttpStatus.BAD_REQUEST, result.status());
        verify(radarrService).testConnection(ArrInstance.DEFAULT);
    }

    @Test
    void lookupMovieByTmdbId_shouldReturnLookupResult() {
        var dto = new RadarrMovieLookupResponseDTO("Inception", 2010, 27205, "tt1375666", true, null);
        when(radarrService.lookupByTmdbId(27205, ArrInstance.DEFAULT)).thenReturn(dto);

        var response = radarrController.lookupMovieByTmdbId(27205, ArrInstance.DEFAULT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = response.getBody();
        assertNotNull(body);
        assertEquals("Inception", body.title());
        assertEquals(27205, body.tmdbId());
        verify(radarrService).lookupByTmdbId(27205, ArrInstance.DEFAULT);
    }

    @Test
    void importMovies_shouldCreateJobAndTriggerBackgroundImport() {
        var job = new BackgroundJob(JobType.RADARR_IMPORT, null);
        when(backgroundJobService.create(eq(JobType.RADARR_IMPORT), eq(null))).thenReturn(job);

        var result = radarrController.importMovies(ArrInstance.DEFAULT);

        assertNotNull(result);
        assertEquals(JobType.RADARR_IMPORT, result.type());
        assertEquals(JobStatus.PENDING, result.status());
        verify(backgroundJobService).create(JobType.RADARR_IMPORT, null);
        verify(radarrService).triggerBackgroundImport(job, ArrInstance.DEFAULT);
    }

    @Test
    void importMovies_anime_shouldCreateAnimeJobAndTriggerBackgroundImport() {
        var job = new BackgroundJob(JobType.RADARR_ANIME_IMPORT, null);
        when(backgroundJobService.create(eq(JobType.RADARR_ANIME_IMPORT), eq(null))).thenReturn(job);

        var result = radarrController.importMovies(ArrInstance.ANIME);

        assertNotNull(result);
        assertEquals(JobType.RADARR_ANIME_IMPORT, result.type());
        verify(backgroundJobService).create(JobType.RADARR_ANIME_IMPORT, null);
        verify(radarrService).triggerBackgroundImport(job, ArrInstance.ANIME);
    }

    @Test
    void refreshRatings_shouldCreateJobAndTriggerBackgroundRefresh() {
        var job = new BackgroundJob(JobType.RADARR_RATINGS_REFRESH, null);
        when(backgroundJobService.create(eq(JobType.RADARR_RATINGS_REFRESH), eq(null))).thenReturn(job);

        var result = radarrController.refreshRatings(ArrInstance.DEFAULT);

        assertNotNull(result);
        assertEquals(JobType.RADARR_RATINGS_REFRESH, result.type());
        assertEquals(JobStatus.PENDING, result.status());
        verify(backgroundJobService).create(JobType.RADARR_RATINGS_REFRESH, null);
        verify(radarrService).triggerBackgroundRatingsRefresh(job, ArrInstance.DEFAULT);
    }

    @Test
    void refreshRatings_anime_shouldCreateAnimeJobAndTriggerBackgroundRefresh() {
        var job = new BackgroundJob(JobType.RADARR_ANIME_RATINGS_REFRESH, null);
        when(backgroundJobService.create(eq(JobType.RADARR_ANIME_RATINGS_REFRESH), eq(null))).thenReturn(job);

        var result = radarrController.refreshRatings(ArrInstance.ANIME);

        assertNotNull(result);
        assertEquals(JobType.RADARR_ANIME_RATINGS_REFRESH, result.type());
        verify(backgroundJobService).create(JobType.RADARR_ANIME_RATINGS_REFRESH, null);
        verify(radarrService).triggerBackgroundRatingsRefresh(job, ArrInstance.ANIME);
    }
}
