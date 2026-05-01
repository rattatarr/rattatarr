package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record YearRewindHighlightsDTO(
        YearRewindHighlightItemDTO firstRated,
        YearRewindHighlightItemDTO lastRated,
        YearRewindHighlightItemDTO highestRated,
        YearRewindHighlightItemDTO lowestRated,
        YearRewindHighlightItemDTO firstWatched,
        YearRewindHighlightItemDTO lastWatched,
        String busiestDay,
        Long busiestDayCount,
        RewindStreakDTO longestWatchStreak,
        Long totalWatchTimeMinutes,
        Long uniqueItemsWatched,
        Long totalMoviesWatched,
        Long totalSeriesWatched
) {}
