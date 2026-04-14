package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.ProfileStatisticsRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.OverallStatsDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ProfileStatisticsResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfileStatisticsWrapper;
import com.rattatarr.rattatarr.services.ProfileStatisticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileStatisticsControllerTest {

    @Mock
    private ProfileStatisticsService profileStatisticsService;

    @InjectMocks
    private ProfileStatisticsController controller;

    @Test
    void getProfileStatistics_WithValidRequest_ShouldReturnStatistics() {
        UUID profileId = UUID.randomUUID();
        ProfileStatisticsRequestDTO requestDTO =
                new ProfileStatisticsRequestDTO(profileId, 8.0f, 3, 5, 20, 8, 6, 5, "w185");

        OverallStatsDTO overallStats = new OverallStatsDTO(100L, 50L, 7.5, 1.0f, 10.0f);
        ProfileStatisticsResponseDTO statistics = emptyStatistics(overallStats);

        when(profileStatisticsService.getStatistics(profileId, 8.0f, 3, 5, 20, 8, 6, 5, "w185")).thenReturn(statistics);

        ResponseEntity<ProfileStatisticsWrapper> response = controller.getProfileStatistics(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().statistics().overallStats().totalRatings());

        verify(profileStatisticsService).getStatistics(profileId, 8.0f, 3, 5, 20, 8, 6, 5, "w185");
    }

    @Test
    void getProfileStatistics_WithDefaultValues_ShouldUseDefaults() {
        UUID profileId = UUID.randomUUID();
        ProfileStatisticsRequestDTO requestDTO =
                new ProfileStatisticsRequestDTO(profileId, null, null, null, null, null, null, null, null);

        OverallStatsDTO overallStats = new OverallStatsDTO(0L, 0L, 0.0, 0.0f, 0.0f);
        ProfileStatisticsResponseDTO statistics = emptyStatistics(overallStats);

        // All limits default to 10, ratingThreshold to 7.0, minCount to 1, genreOverTimeLimit to 5, profileImageSize to w185
        when(profileStatisticsService.getStatistics(profileId, 7.0f, 1, 10, 10, 10, 10, 5, "w185")).thenReturn(statistics);

        ResponseEntity<ProfileStatisticsWrapper> response = controller.getProfileStatistics(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(profileStatisticsService).getStatistics(profileId, 7.0f, 1, 10, 10, 10, 10, 5, "w185");
    }

    @Test
    void getProfileStatistics_WhenProfileNotFound_ShouldThrowException() {
        UUID profileId = UUID.randomUUID();
        ProfileStatisticsRequestDTO requestDTO =
                new ProfileStatisticsRequestDTO(profileId, 8.0f, 3, 5, 20, 8, 6, 5, "w185");

        when(profileStatisticsService.getStatistics(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        assertThrows(
                ProfilesExceptions.ProfileNotFoundExceptions.class,
                () -> controller.getProfileStatistics(requestDTO));
    }

    private ProfileStatisticsResponseDTO emptyStatistics(OverallStatsDTO overallStats) {
        return new ProfileStatisticsResponseDTO(
                overallStats,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
    }
}
