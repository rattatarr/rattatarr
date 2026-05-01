package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.responses.*;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import com.rattatarr.rattatarr.specifications.StatisticsSpecifications;
import com.rattatarr.rattatarr.utils.AsyncEntityQueryRunner;
import com.rattatarr.rattatarr.utils.ValueResolver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class YearRewindService {
    protected final Logger logger = LoggerFactory.getLogger(YearRewindService.class);

    private final EntityManagerFactory entityManagerFactory;
    private final ProfilesRepository profilesRepository;
    private final AsyncEntityQueryRunner queryRunner;
    private final ProfileStatisticsService profileStatisticsService;
    private final MediaItemViewHelper mediaItemViewHelper;

    public YearRewindService(
            EntityManagerFactory entityManagerFactory,
            ProfilesRepository profilesRepository,
            @Qualifier("statisticsExecutor") Executor statisticsExecutor,
            ProfileStatisticsService profileStatisticsService,
            MediaItemViewHelper mediaItemViewHelper) {
        this.entityManagerFactory = entityManagerFactory;
        this.profilesRepository = profilesRepository;
        this.queryRunner = new AsyncEntityQueryRunner(entityManagerFactory, statisticsExecutor, logger);
        this.profileStatisticsService = profileStatisticsService;
        this.mediaItemViewHelper = mediaItemViewHelper;
    }

    public YearRewindResponseDTO getRewind(
            UUID profileId,
            int year,
            float ratingThreshold,
            int minCount,
            int genresLimit,
            int actorsLimit,
            int directorsLimit,
            int producersLimit,
            String profileImageSize) {
        profilesRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        Instant from = LocalDate.of(year, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = LocalDate.of(year + 1, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant();

        CompletableFuture<OverallStatsDTO> overallFuture =
                queryRunner.query("overallStats", em -> profileStatisticsService.computeOverallStats(em, profileId, from, to));
        CompletableFuture<List<RatingDistributionDTO>> distributionFuture =
                queryRunner.query("ratingDistribution", em -> profileStatisticsService.computeRatingDistribution(em, profileId, from, to));
        CompletableFuture<List<RatingDistributionDTO>> distributionByIntegerFuture =
                queryRunner.query("ratingDistributionByInteger", em -> profileStatisticsService.computeRatingDistributionByInteger(em, profileId, from, to));
        CompletableFuture<List<MediaTypeBreakdownDTO>> mediaTypeFuture =
                queryRunner.query("mediaTypeBreakdown", em -> computeMediaTypeBreakdown(em, profileId, from, to));
        CompletableFuture<List<MediaTypeBreakdownDTO>> jellyfinMediaTypeFuture =
                queryRunner.query("jellyfinMediaTypeBreakdown", em -> profileStatisticsService.computeJellyfinMediaTypeBreakdown(em, profileId, from, to));
        CompletableFuture<List<GenreStatDTO>> genresByCountFuture =
                queryRunner.query("topGenresByCount", em -> profileStatisticsService.computeTopGenresByCount(em, profileId, genresLimit, from, to));
        CompletableFuture<List<GenreStatDTO>> genresByScoreFuture =
                queryRunner.query("topGenresByScore", em -> profileStatisticsService.computeTopGenresByScore(em, profileId, ratingThreshold, genresLimit, from, to));
        CompletableFuture<List<GenreStatDTO>> jellyfinGenresByCountFuture =
                queryRunner.query("jellyfinTopGenresByCount", em -> profileStatisticsService.computeJellyfinTopGenresByCount(em, profileId, genresLimit, from, to));
        CompletableFuture<List<PersonStatDTO>> directorsByCountFuture =
                queryRunner.query("directorsByCount", em -> profileStatisticsService.computeFavoriteDirectors(em, profileId, minCount, directorsLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, from, to));
        CompletableFuture<List<PersonStatDTO>> directorsByScoreFuture =
                queryRunner.query("directorsByScore", em -> profileStatisticsService.computeFavoriteDirectors(em, profileId, minCount, directorsLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, from, to));
        CompletableFuture<List<PersonStatDTO>> producersByCountFuture =
                queryRunner.query("producersByCount", em -> profileStatisticsService.computeFavoriteProducers(em, profileId, minCount, producersLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, from, to));
        CompletableFuture<List<PersonStatDTO>> producersByScoreFuture =
                queryRunner.query("producersByScore", em -> profileStatisticsService.computeFavoriteProducers(em, profileId, minCount, producersLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, from, to));
        CompletableFuture<List<PersonStatDTO>> actorsByCountFuture =
                queryRunner.query("actorsByCount", em -> profileStatisticsService.computeFavoriteActors(em, profileId, minCount, actorsLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, from, to));
        CompletableFuture<List<PersonStatDTO>> actorsByScoreFuture =
                queryRunner.query("actorsByScore", em -> profileStatisticsService.computeFavoriteActors(em, profileId, minCount, actorsLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, from, to));
        CompletableFuture<List<RatingActivityDTO>> monthlyFuture =
                queryRunner.query("monthlyActivity", em -> getMonthlyActivity(em, profileId, from, to));
        CompletableFuture<List<DayOfWeekActivityDTO>> dayOfWeekFuture =
                queryRunner.query("dayOfWeekActivity", em -> profileStatisticsService.computeDayOfWeekActivity(em, profileId, from, to));
        CompletableFuture<List<DayOfWeekActivityDTO>> jellyfinDayOfWeekFuture =
                queryRunner.query("jellyfinDayOfWeekActivity", em -> profileStatisticsService.computeJellyfinDayOfWeekActivity(em, profileId, from, to));
        CompletableFuture<List<DecadeStatDTO>> decadesFuture =
                queryRunner.query("decadePreferences", em -> profileStatisticsService.computeDecadePreferences(em, profileId, from, to));
        CompletableFuture<List<DecadeStatDTO>> jellyfinDecadesFuture =
                queryRunner.query("jellyfinDecadePreferences", em -> profileStatisticsService.computeJellyfinDecadePreferences(em, profileId, from, to));
        CompletableFuture<RuntimeStatsDTO> runtimeFuture =
                queryRunner.query("runtimeStats", em -> getRuntimeStats(em, profileId, from, to));
        CompletableFuture<YearRewindHighlightsDTO> highlightsFuture =
                queryRunner.query("highlights", em -> getHighlights(em, profileId, from, to));

        return new YearRewindResponseDTO(
                year,
                highlightsFuture.join(),
                overallFuture.join(),
                distributionFuture.join(),
                distributionByIntegerFuture.join(),
                mediaTypeFuture.join(),
                jellyfinMediaTypeFuture.join(),
                genresByCountFuture.join(),
                genresByScoreFuture.join(),
                jellyfinGenresByCountFuture.join(),
                directorsByCountFuture.join(),
                directorsByScoreFuture.join(),
                producersByCountFuture.join(),
                producersByScoreFuture.join(),
                actorsByCountFuture.join(),
                actorsByScoreFuture.join(),
                monthlyFuture.join(),
                dayOfWeekFuture.join(),
                jellyfinDayOfWeekFuture.join(),
                decadesFuture.join(),
                jellyfinDecadesFuture.join(),
                runtimeFuture.join());
    }

    public List<Integer> getAvailableYears(UUID profileId) {
        profilesRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            Set<Integer> combined = new TreeSet<>(Comparator.reverseOrder());
            combined.addAll(StatisticsSpecifications.queryAvailableRewindYears(em, profileId));
            combined.addAll(StatisticsSpecifications.queryAvailableWatchYears(em, profileId));
            return new ArrayList<>(combined);
        }
    }


    // Year-scoped media type breakdown: no library total enrichment needed.
    private List<MediaTypeBreakdownDTO> computeMediaTypeBreakdown(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryMediaTypeBreakdown(em, profileId, from, to);
        long totalRated = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();
        List<MediaTypeBreakdownDTO> breakdown = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            String mediaType = String.valueOf(tuple.get("mediaType"));
            double percentage = totalRated > 0 ? (count * 100.0 / totalRated) : 0.0;
            breakdown.add(new MediaTypeBreakdownDTO(
                    mediaType, count, count,
                    ValueResolver.round2(percentage),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return breakdown;
    }

    // Returns all months ASC with no limit (unlike the all-time version: last 12 DESC).
    private List<RatingActivityDTO> getMonthlyActivity(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryMonthlyActivityForYear(em, profileId, from, to);
        List<RatingActivityDTO> activity = new ArrayList<>();
        for (Tuple tuple : results) {
            activity.add(new RatingActivityDTO(
                    tuple.get("period", String.class),
                    tuple.get("count", Long.class),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return activity;
    }

    private RuntimeStatsDTO getRuntimeStats(EntityManager em, UUID profileId, Instant from, Instant to) {
        Tuple result = StatisticsSpecifications.queryRuntimeStats(em, profileId, from, to);
        Double avg = result.get("averageRuntime", Double.class);
        Integer longest = result.get("longestRuntime", Integer.class);
        Integer shortest = result.get("shortestRuntime", Integer.class);
        long movieTotal = StatisticsSpecifications.queryMoviesTotalRuntime(em, profileId, from, to);
        long seriesTotal = StatisticsSpecifications.querySeriesRuntimeTotal(em, profileId, from, to);
        return new RuntimeStatsDTO(
                avg != null ? (int) Math.round(avg) : 0,
                movieTotal + seriesTotal,
                longest != null ? longest : 0,
                shortest != null ? shortest : 0);
    }

    private YearRewindHighlightsDTO getHighlights(EntityManager em, UUID profileId, Instant from, Instant to) {
        YearRewindHighlightItemDTO firstRated = mapRatingHighlightItem(
                StatisticsSpecifications.queryFirstRatedItem(em, profileId, from, to));
        YearRewindHighlightItemDTO lastRated = mapRatingHighlightItem(
                StatisticsSpecifications.queryLastRatedItem(em, profileId, from, to));
        YearRewindHighlightItemDTO highestRated = mapRatingHighlightItem(
                StatisticsSpecifications.queryHighestRatedItem(em, profileId, from, to));
        YearRewindHighlightItemDTO lowestRated = mapRatingHighlightItem(
                StatisticsSpecifications.queryLowestRatedItem(em, profileId, from, to));

        YearRewindHighlightItemDTO firstWatched = mapWatchHighlightItem(
                StatisticsSpecifications.queryFirstWatchedItem(em, profileId, from, to));
        YearRewindHighlightItemDTO lastWatched = mapWatchHighlightItem(
                StatisticsSpecifications.queryLastWatchedItem(em, profileId, from, to));

        List<Tuple> busiestDayResult = StatisticsSpecifications.queryBusiestWatchDay(em, profileId, from, to);
        String busiestDay = null;
        Long busiestDayCount = null;
        if (!busiestDayResult.isEmpty()) {
            busiestDay = busiestDayResult.getFirst().get("date", String.class);
            busiestDayCount = busiestDayResult.getFirst().get("count", Long.class);
        }

        List<String> watchDates = StatisticsSpecifications.queryDistinctWatchDates(em, profileId, from, to);
        RewindStreakDTO longestStreak = computeLongestStreak(watchDates);

        long movieTotal = StatisticsSpecifications.queryMoviesTotalRuntime(em, profileId, from, to);
        long seriesTotal = StatisticsSpecifications.querySeriesRuntimeTotal(em, profileId, from, to);
        Long totalWatchTimeMinutes = movieTotal + seriesTotal > 0 ? movieTotal + seriesTotal : null;

        Long uniqueItems = StatisticsSpecifications.queryUniqueItemsWatched(em, profileId, from, to);
        Long uniqueItemsWatched = uniqueItems > 0 ? uniqueItems : null;

        Long moviesWatched = StatisticsSpecifications.queryMoviesWatched(em, profileId, from, to);
        Long totalMoviesWatched = moviesWatched > 0 ? moviesWatched : null;

        Long seriesWatched = StatisticsSpecifications.querySeriesWatched(em, profileId, from, to);
        Long totalSeriesWatched = seriesWatched > 0 ? seriesWatched : null;

        return new YearRewindHighlightsDTO(
                firstRated,
                lastRated,
                highestRated,
                lowestRated,
                firstWatched,
                lastWatched,
                busiestDay,
                busiestDayCount,
                longestStreak,
                totalWatchTimeMinutes,
                uniqueItemsWatched,
                totalMoviesWatched,
                totalSeriesWatched);
    }

    private YearRewindHighlightItemDTO mapRatingHighlightItem(List<Tuple> results) {
        if (results.isEmpty()) return null;
        Tuple tuple = results.getFirst();
        String rawPoster = tuple.get("posterImageUrl", String.class);
        return new YearRewindHighlightItemDTO(
                tuple.get("title", String.class),
                rawPoster != null ? mediaItemViewHelper.buildUrlFromPath(rawPoster, "w342") : null,
                tuple.get("rating", Float.class),
                tuple.get("eventAt", Instant.class),
                String.valueOf(tuple.get("mediaType")));
    }

    private YearRewindHighlightItemDTO mapWatchHighlightItem(List<Tuple> results) {
        if (results.isEmpty()) return null;
        Tuple tuple = results.getFirst();
        String rawPoster = tuple.get("posterImageUrl", String.class);
        return new YearRewindHighlightItemDTO(
                tuple.get("title", String.class),
                rawPoster != null ? mediaItemViewHelper.buildUrlFromPath(rawPoster, "w342") : null,
                null,
                tuple.get("eventAt", Instant.class),
                String.valueOf(tuple.get("mediaType")));
    }

    private RewindStreakDTO computeLongestStreak(List<String> sortedDates) {
        if (sortedDates.isEmpty()) return null;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String streakStart = sortedDates.getFirst();
        String streakEnd = sortedDates.getFirst();
        int streakLen = 1;

        String bestStart = streakStart;
        String bestEnd = streakEnd;
        int bestLen = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate prev = LocalDate.parse(sortedDates.get(i - 1), fmt);
            LocalDate curr = LocalDate.parse(sortedDates.get(i), fmt);

            if (curr.equals(prev.plusDays(1))) {
                streakEnd = sortedDates.get(i);
                streakLen++;
            } else {
                if (streakLen > bestLen) {
                    bestLen = streakLen;
                    bestStart = streakStart;
                    bestEnd = streakEnd;
                }
                streakStart = sortedDates.get(i);
                streakEnd = sortedDates.get(i);
                streakLen = 1;
            }
        }
        if (streakLen > bestLen) {
            bestLen = streakLen;
            bestStart = streakStart;
            bestEnd = streakEnd;
        }

        return new RewindStreakDTO(bestStart, bestEnd, bestLen);
    }
}
