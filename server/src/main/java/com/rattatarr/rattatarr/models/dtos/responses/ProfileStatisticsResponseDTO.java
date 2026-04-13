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
        List<GenreStatDTO> topGenresByCount,
        List<GenreStatDTO> topGenresByScore,
        List<PersonStatDTO> directorsByCount,
        List<PersonStatDTO> directorsByScore,
        List<PersonStatDTO> producersByCount,
        List<PersonStatDTO> producersByScore,
        List<PersonStatDTO> actorsByCount,
        List<PersonStatDTO> actorsByScore,
        List<DecadeStatDTO> decadePreferences,
        List<RecentTrendsDTO> recentTrends,
        List<RatingActivityDTO> monthlyActivity,
        RuntimeStatsDTO runtimeStats,
        RatingConsistencyDTO ratingConsistency,
        List<DayOfWeekActivityDTO> dayOfWeekActivity,
        List<RatingHeatmapYearDTO> ratingHeatmap,
        List<RatingHeatmapYearDTO> uniqueMediaPlayedHeatmap,
        List<GenreOverTimeYearDTO> genreOverTime
) implements Serializable {
}
