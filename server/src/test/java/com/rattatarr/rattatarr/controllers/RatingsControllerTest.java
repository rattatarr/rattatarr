package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.RatingMediaType;
import com.rattatarr.rattatarr.models.dtos.requests.DeleteRateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.RateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import com.rattatarr.rattatarr.services.IMDbImportService;
import com.rattatarr.rattatarr.services.ProfilesService;
import com.rattatarr.rattatarr.services.RatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingsControllerTest {

    @Mock
    private RatingService ratingService;

    @Mock
    private IMDbImportService imDbImportService;

    @Mock
    private ProfilesService profilesService;

    @Mock
    private BackgroundJobService backgroundJobService;

    @InjectMocks
    private RatingsController controller;

    @Test
    void rateMediaItem_shouldReturnSuccessResponse() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        RateRequestDTO request = new RateRequestDTO(
                profileId,
                mediaItemId,
                RatingMediaType.MEDIA_ITEM,
                8.5f
        );

        doNothing().when(ratingService).rate(any(RateRequestDTO.class));

        // When
        ResponseEntity<GenericResponseDTO> response = controller.getSeries(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().status());
        assertEquals("Rating submitted successfully", response.getBody().message());
        verify(ratingService).rate(eq(request));
    }

    @Test
    void rateMediaSeason_shouldReturnSuccessResponse() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaSeasonId = UUID.randomUUID();
        RateRequestDTO request = new RateRequestDTO(
                profileId,
                mediaSeasonId,
                RatingMediaType.MEDIA_SEASON,
                9.0f
        );

        doNothing().when(ratingService).rate(any(RateRequestDTO.class));

        // When
        ResponseEntity<GenericResponseDTO> response = controller.getSeries(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().status());
        assertEquals("Rating submitted successfully", response.getBody().message());
        verify(ratingService).rate(eq(request));
    }

    @Test
    void rateWithMinimumRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        RateRequestDTO request = new RateRequestDTO(
                profileId,
                mediaItemId,
                RatingMediaType.MEDIA_ITEM,
                1.0f
        );

        doNothing().when(ratingService).rate(any(RateRequestDTO.class));

        // When
        ResponseEntity<GenericResponseDTO> response = controller.getSeries(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().status());
        verify(ratingService).rate(eq(request));
    }

    @Test
    void rateWithMaximumRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        RateRequestDTO request = new RateRequestDTO(
                profileId,
                mediaItemId,
                RatingMediaType.MEDIA_ITEM,
                10.0f
        );

        doNothing().when(ratingService).rate(any(RateRequestDTO.class));

        // When
        ResponseEntity<GenericResponseDTO> response = controller.getSeries(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().status());
        verify(ratingService).rate(eq(request));
    }

    @Test
    void rateWithHalfStarRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        RateRequestDTO request = new RateRequestDTO(
                profileId,
                mediaItemId,
                RatingMediaType.MEDIA_ITEM,
                7.5f
        );

        doNothing().when(ratingService).rate(any(RateRequestDTO.class));

        // When
        ResponseEntity<GenericResponseDTO> response = controller.getSeries(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().status());
        verify(ratingService).rate(eq(request));
    }

    @Test
    void deleteMediaItemRating_shouldReturnSuccessResponse() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        DeleteRateRequestDTO request = new DeleteRateRequestDTO(
                profileId,
                mediaItemId,
                RatingMediaType.MEDIA_ITEM
        );

        doNothing().when(ratingService).deleteRating(any(DeleteRateRequestDTO.class));

        // When
        ResponseEntity<GenericResponseDTO> response = controller.deleteRating(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().status());
        assertEquals("Rating deleted successfully", response.getBody().message());
        verify(ratingService).deleteRating(eq(request));
    }

    @Test
    void deleteMediaSeasonRating_shouldReturnSuccessResponse() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaSeasonId = UUID.randomUUID();
        DeleteRateRequestDTO request = new DeleteRateRequestDTO(
                profileId,
                mediaSeasonId,
                RatingMediaType.MEDIA_SEASON
        );

        doNothing().when(ratingService).deleteRating(any(DeleteRateRequestDTO.class));

        // When
        ResponseEntity<GenericResponseDTO> response = controller.deleteRating(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().status());
        assertEquals("Rating deleted successfully", response.getBody().message());
        verify(ratingService).deleteRating(eq(request));
    }

    @Test
    void deleteRating_shouldCallServiceExactlyOnce() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        DeleteRateRequestDTO request = new DeleteRateRequestDTO(
                profileId,
                entityId,
                RatingMediaType.MEDIA_ITEM
        );

        doNothing().when(ratingService).deleteRating(any(DeleteRateRequestDTO.class));

        // When
        controller.deleteRating(request);

        // Then
        verify(ratingService, times(1)).deleteRating(eq(request));
    }

    @Test
    void rate_shouldCallServiceExactlyOnce() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        RateRequestDTO request = new RateRequestDTO(
                profileId,
                entityId,
                RatingMediaType.MEDIA_ITEM,
                5.0f
        );

        doNothing().when(ratingService).rate(any(RateRequestDTO.class));

        // When
        controller.getSeries(request);

        // Then
        verify(ratingService, times(1)).rate(eq(request));
    }

    private BackgroundJob stubJob(UUID profileId) {
        BackgroundJob job = new BackgroundJob(JobType.CSV_IMPORT, profileId);
        when(backgroundJobService.create(eq(JobType.CSV_IMPORT), eq(profileId))).thenReturn(job);
        return job;
    }

    @Test
    void importImdbCsv_withValidFile_shouldReturnJobResponse() {
        // Given
        UUID profileId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ratings.csv",
                "text/csv",
                "Const,Your Rating,Date Rated\ntt0111161,9,2024-01-15".getBytes()
        );

        when(imDbImportService.parseCSV(any())).thenReturn(List.of());
        stubJob(profileId);
        doNothing().when(imDbImportService).triggerBackgroundImportRatings(any(), any(UUID.class), any(BackgroundJob.class));

        // When
        BackgroundJobResponseDTO response = controller.importImdbCsv(file, profileId);

        // Then
        assertNotNull(response);
        assertEquals(JobType.CSV_IMPORT, response.type());
        assertEquals(JobStatus.PENDING, response.status());
    }

    @Test
    void importImdbCsv_withValidProfileId_shouldCallServiceCorrectly() {
        // Given
        UUID profileId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ratings.csv",
                "text/csv",
                "Const,Your Rating,Date Rated\ntt0111161,9,2024-01-15".getBytes()
        );

        when(imDbImportService.parseCSV(any())).thenReturn(List.of());
        stubJob(profileId);
        doNothing().when(imDbImportService).triggerBackgroundImportRatings(any(), any(UUID.class), any(BackgroundJob.class));

        // When
        controller.importImdbCsv(file, profileId);

        // Then
        verify(imDbImportService).parseCSV(eq(file));
        verify(imDbImportService).triggerBackgroundImportRatings(any(), eq(profileId), any(BackgroundJob.class));
    }

    @Test
    void importImdbCsv_shouldCallTriggerBackgroundImportRatings() {
        // Given
        UUID profileId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imdb_ratings.csv",
                "text/csv",
                "Const,Your Rating,Date Rated\ntt0111161,9,2024-01-15\ntt0468569,8,2024-01-16".getBytes()
        );

        when(imDbImportService.parseCSV(any())).thenReturn(List.of());
        stubJob(profileId);
        doNothing().when(imDbImportService).triggerBackgroundImportRatings(any(), any(UUID.class), any(BackgroundJob.class));

        // When
        controller.importImdbCsv(file, profileId);

        // Then
        verify(imDbImportService).parseCSV(eq(file));
        verify(imDbImportService, times(1)).triggerBackgroundImportRatings(any(), any(UUID.class), any(BackgroundJob.class));
    }

    @Test
    void exportRatingsCsv_withValidProfile_shouldReturnCsvAttachment() {
        // Given
        UUID profileId = UUID.randomUUID();
        Profile profile = new Profile("John", null);
        byte[] csv = "title,media type\nInception,MOVIE\n".getBytes();
        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(ratingService.exportProfileMediaRatingsCsv(eq(profile))).thenReturn(csv);

        // When
        ResponseEntity<byte[]> response = controller.exportRatingsCsv(profileId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.parseMediaType("text/csv"), response.getHeaders().getContentType());
        assertNotNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("attachment"));
        assertArrayEquals(csv, response.getBody());
        verify(profilesService).findByIdOrThrow(eq(profileId), any());
        verify(ratingService).exportProfileMediaRatingsCsv(eq(profile));
    }
}
