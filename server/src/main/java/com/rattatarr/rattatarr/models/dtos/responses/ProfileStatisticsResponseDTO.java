package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileStatisticsResponseDTO(
        OverallStatsDTO overallStats,
        List<RatingDistributionDTO> ratingDistribution,
        List<RatingDistributionDTO> ratingDistributionByInteger,
        List<MediaTypeBreakdownDTO> mediaTypeBreakdown,
        List<MediaTypeBreakdownDTO> jellyfinMediaTypeBreakdown,
        List<GenreStatDTO> topGenresByCount,
        List<GenreStatDTO> topGenresByScore,
        List<GenreStatDTO> jellyfinTopGenresByCount,
        List<GenreStatDTO> jellyfinTopGenresByScore,
        List<PersonStatDTO> directorsByCount,
        List<PersonStatDTO> directorsByScore,
        List<PersonStatDTO> producersByCount,
        List<PersonStatDTO> producersByScore,
        List<PersonStatDTO> actorsByCount,
        List<PersonStatDTO> actorsByScore,
        List<DecadeStatDTO> decadePreferences,
        List<DecadeStatDTO> jellyfinDecadePreferences,
        List<RecentTrendsDTO> recentTrends,
        List<RecentTrendsDTO> jellyfinRecentTrends,
        List<RatingActivityDTO> monthlyActivity,
        RuntimeStatsDTO runtimeStats,
        RuntimeStatsDTO jellyfinRuntimeStats,
        RatingConsistencyDTO ratingConsistency,
        List<DayOfWeekActivityDTO> dayOfWeekActivity,
        List<DayOfWeekActivityDTO> jellyfinDayOfWeekActivity,
        List<RatingHeatmapYearDTO> ratingHeatmap,
        List<RatingHeatmapYearDTO> uniqueMediaPlayedHeatmap,
        List<GenreOverTimeYearDTO> genreOverTime,
        List<GenreOverTimeYearDTO> jellyfinGenreOverTime
) implements Serializable {
}
