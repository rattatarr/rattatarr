package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record YearRewindResponseDTO(
        int year,
        YearRewindHighlightsDTO highlights,
        OverallStatsDTO overallStats,
        List<RatingDistributionDTO> ratingDistribution,
        List<RatingDistributionDTO> ratingDistributionByInteger,
        List<MediaTypeBreakdownDTO> mediaTypeBreakdown,
        List<MediaTypeBreakdownDTO> jellyfinMediaTypeBreakdown,
        List<GenreStatDTO> topGenresByCount,
        List<GenreStatDTO> topGenresByScore,
        List<GenreStatDTO> jellyfinTopGenresByCount,
        List<PersonStatDTO> directorsByCount,
        List<PersonStatDTO> directorsByScore,
        List<PersonStatDTO> producersByCount,
        List<PersonStatDTO> producersByScore,
        List<PersonStatDTO> actorsByCount,
        List<PersonStatDTO> actorsByScore,
        List<RatingActivityDTO> monthlyActivity,
        List<DayOfWeekActivityDTO> dayOfWeekActivity,
        List<DayOfWeekActivityDTO> jellyfinDayOfWeekActivity,
        List<DecadeStatDTO> decadePreferences,
        List<DecadeStatDTO> jellyfinDecadePreferences,
        RuntimeStatsDTO runtimeStats
) {}
