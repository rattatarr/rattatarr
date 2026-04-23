package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieLookupResponseDTO;
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

import java.util.UUID;

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
    void lookupMovieByTmdbId_shouldReturnLookupResult() {
        var dto = new RadarrMovieLookupResponseDTO("Inception", 2010, 27205, "tt1375666", true, null);
        when(radarrService.lookupByTmdbId(27205)).thenReturn(dto);

        var response = radarrController.lookupMovieByTmdbId(27205);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = response.getBody();
        assertNotNull(body);
        assertEquals("Inception", body.title());
        assertEquals(27205, body.tmdbId());
        verify(radarrService).lookupByTmdbId(27205);
    }

    @Test
    void importMovies_shouldCreateJobAndTriggerBackgroundImport() {
        var job = new BackgroundJob(JobType.RADARR_IMPORT, null);
        when(backgroundJobService.create(eq(JobType.RADARR_IMPORT), eq(null))).thenReturn(job);

        var result = radarrController.importMovies();

        assertNotNull(result);
        assertEquals(JobType.RADARR_IMPORT, result.type());
        assertEquals(JobStatus.PENDING, result.status());
        verify(backgroundJobService).create(JobType.RADARR_IMPORT, null);
        verify(radarrService).triggerBackgroundImport(job);
    }

    @Test
    void refreshRatings_shouldCreateJobAndTriggerBackgroundRefresh() {
        var job = new BackgroundJob(JobType.RADARR_RATINGS_REFRESH, null);
        when(backgroundJobService.create(eq(JobType.RADARR_RATINGS_REFRESH), eq(null))).thenReturn(job);

        var result = radarrController.refreshRatings();

        assertNotNull(result);
        assertEquals(JobType.RADARR_RATINGS_REFRESH, result.type());
        assertEquals(JobStatus.PENDING, result.status());
        verify(backgroundJobService).create(JobType.RADARR_RATINGS_REFRESH, null);
        verify(radarrService).triggerBackgroundRatingsRefresh(job);
    }
}
