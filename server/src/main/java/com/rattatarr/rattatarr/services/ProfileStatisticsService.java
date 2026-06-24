package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.responses.*;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class ProfileStatisticsService {
    protected final Logger logger = LoggerFactory.getLogger(ProfileStatisticsService.class);

    private final AsyncEntityQueryRunner asyncQueryRunner;
    private final ProfilesRepository profilesRepository;
    private final TMDbClient tmdbClient;

    public ProfileStatisticsService(
            EntityManagerFactory entityManagerFactory,
            ProfilesRepository profilesRepository,
            @Qualifier("statisticsExecutor") Executor statisticsExecutor,
            TMDbClient tmdbClient) {
        this.asyncQueryRunner = new AsyncEntityQueryRunner(entityManagerFactory, statisticsExecutor, logger);
        this.profilesRepository = profilesRepository;
        this.tmdbClient = tmdbClient;
    }

    public ProfileStatisticsResponseDTO getStatistics(
            UUID profileId,
            Float ratingThreshold,
            Integer minCount,
            Integer genresLimit,
            Integer actorsLimit,
            Integer directorsLimit,
            Integer producersLimit,
            Integer genreOverTimeLimit,
            String profileImageSize) {
        Profile profile = profilesRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        logger.debug(
                "Getting statistics for profile '{}' ({}), threshold: {}, minCount: {}, genresLimit: {}, actorsLimit: {}, directorsLimit: {}, producersLimit: {}, genreOverTimeLimit: {}, profileImageSize: {}",
                profile.name(), profileId, ratingThreshold, minCount, genresLimit, actorsLimit, directorsLimit,
                producersLimit, genreOverTimeLimit, profileImageSize);

        CompletableFuture<OverallStatsDTO> overallFuture =
                asyncQueryRunner.query("overallStats", em -> getOverallStats(em, profileId));
        CompletableFuture<List<RatingDistributionDTO>> distributionFuture =
                asyncQueryRunner.query("ratingDistribution", em -> computeRatingDistribution(em, profileId, null, null));
        CompletableFuture<List<RatingDistributionDTO>> distributionByIntegerFuture =
                asyncQueryRunner.query("ratingDistributionByInteger", em -> computeRatingDistributionByInteger(em, profileId, null, null));
        CompletableFuture<List<MediaTypeBreakdownDTO>> mediaTypeFuture =
                asyncQueryRunner.query("mediaTypeBreakdown", em -> getMediaTypeBreakdown(em, profileId));
        CompletableFuture<List<MediaTypeBreakdownDTO>> jellyfinMediaTypeFuture =
                asyncQueryRunner.query("jellyfinMediaTypeBreakdown", em -> computeJellyfinMediaTypeBreakdown(em, profileId, null, null));
        CompletableFuture<List<GenreStatDTO>> genresByCountFuture =
                asyncQueryRunner.query("topGenresByCount", em -> computeTopGenresByCount(em, profileId, genresLimit, null, null));
        CompletableFuture<List<GenreStatDTO>> genresByScoreFuture =
                asyncQueryRunner.query("topGenresByScore", em -> computeTopGenresByScore(em, profileId, ratingThreshold, genresLimit, null, null));
        CompletableFuture<List<GenreStatDTO>> jellyfinGenresByCountFuture =
                asyncQueryRunner.query("jellyfinTopGenresByCount", em -> computeJellyfinTopGenresByCount(em, profileId, genresLimit, null, null));
        CompletableFuture<List<PersonStatDTO>> directorsByCountFuture =
                asyncQueryRunner.query("favoriteDirectorsCount", em -> computeFavoriteDirectors(em, profileId, minCount, directorsLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, null, null));
        CompletableFuture<List<PersonStatDTO>> directorsByScoreFuture =
                asyncQueryRunner.query("favoriteDirectorsScore", em -> computeFavoriteDirectors(em, profileId, minCount, directorsLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, null, null));
        CompletableFuture<List<PersonStatDTO>> producersByCountFuture =
                asyncQueryRunner.query("favoriteProducersCount", em -> computeFavoriteProducers(em, profileId, minCount, producersLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, null, null));
        CompletableFuture<List<PersonStatDTO>> producersByScoreFuture =
                asyncQueryRunner.query("favoriteProducersScore", em -> computeFavoriteProducers(em, profileId, minCount, producersLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, null, null));
        CompletableFuture<List<PersonStatDTO>> actorsByCountFuture =
                asyncQueryRunner.query("favoriteActorsCount", em -> computeFavoriteActors(em, profileId, minCount, actorsLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, null, null));
        CompletableFuture<List<PersonStatDTO>> actorsByScoreFuture =
                asyncQueryRunner.query("favoriteActorsScore", em -> computeFavoriteActors(em, profileId, minCount, actorsLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, null, null));
        CompletableFuture<List<DecadeStatDTO>> decadesFuture =
                asyncQueryRunner.query("decadePreferences", em -> computeDecadePreferences(em, profileId, null, null));
        CompletableFuture<List<DecadeStatDTO>> jellyfinDecadesFuture =
                asyncQueryRunner.query("jellyfinDecadePreferences", em -> computeJellyfinDecadePreferences(em, profileId, null, null));
        CompletableFuture<List<RecentTrendsDTO>> trendsFuture =
                asyncQueryRunner.query("recentTrends", em -> getRecentTrends(em, profileId));
        CompletableFuture<List<RecentTrendsDTO>> jellyfinTrendsFuture =
                asyncQueryRunner.query("jellyfinRecentTrends", em -> getJellyfinRecentTrends(em, profileId));
        CompletableFuture<List<RatingActivityDTO>> monthlyFuture =
                asyncQueryRunner.query("monthlyActivity", em -> getMonthlyActivity(em, profileId));
        CompletableFuture<RuntimeStatsDTO> runtimeFuture =
                asyncQueryRunner.query("runtimeStats", em -> getRuntimeStats(em, profileId));
        CompletableFuture<RuntimeStatsDTO> jellyfinRuntimeFuture =
                asyncQueryRunner.query("jellyfinRuntimeStats", em -> getJellyfinRuntimeStats(em, profileId));
        CompletableFuture<RatingConsistencyDTO> consistencyFuture =
                asyncQueryRunner.query("ratingConsistency", em -> getRatingConsistency(em, profileId));
        CompletableFuture<List<DayOfWeekActivityDTO>> dayOfWeekFuture =
                asyncQueryRunner.query("dayOfWeekActivity", em -> computeDayOfWeekActivity(em, profileId, null, null));
        CompletableFuture<List<DayOfWeekActivityDTO>> jellyfinDayOfWeekFuture =
                asyncQueryRunner.query("jellyfinDayOfWeekActivity", em -> computeJellyfinDayOfWeekActivity(em, profileId, null, null));
        CompletableFuture<List<RatingHeatmapYearDTO>> heatmapFuture =
                asyncQueryRunner.query("ratingHeatmap", em -> getRatingHeatmap(em, profileId));
        CompletableFuture<List<RatingHeatmapYearDTO>> uniqueMediaPlayedHeatmapFuture =
                asyncQueryRunner.query("uniqueMediaPlayedHeatmap", em -> getUniqueMediaPlayedHeatmap(em, profileId));
        CompletableFuture<List<GenreOverTimeYearDTO>> genreOverTimeFuture =
                asyncQueryRunner.query("genreOverTime", em -> getGenreOverTime(em, profileId, genreOverTimeLimit));
        CompletableFuture<List<GenreOverTimeYearDTO>> jellyfinGenreOverTimeFuture =
                asyncQueryRunner.query("jellyfinGenreOverTime", em -> getJellyfinGenreOverTime(em, profileId, genreOverTimeLimit));


        return new ProfileStatisticsResponseDTO(
                overallFuture.join(),
                distributionFuture.join(),
                distributionByIntegerFuture.join(),
                mediaTypeFuture.join(),
                jellyfinMediaTypeFuture.join(),
                genresByCountFuture.join(),
                genresByScoreFuture.join(),
                jellyfinGenresByCountFuture.join(),
                java.util.Collections.emptyList(),
                directorsByCountFuture.join(),
                directorsByScoreFuture.join(),
                producersByCountFuture.join(),
                producersByScoreFuture.join(),
                actorsByCountFuture.join(),
                actorsByScoreFuture.join(),
                decadesFuture.join(),
                jellyfinDecadesFuture.join(),
                trendsFuture.join(),
                jellyfinTrendsFuture.join(),
                monthlyFuture.join(),
                runtimeFuture.join(),
                jellyfinRuntimeFuture.join(),
                consistencyFuture.join(),
                dayOfWeekFuture.join(),
                jellyfinDayOfWeekFuture.join(),
                heatmapFuture.join(),
                uniqueMediaPlayedHeatmapFuture.join(),
                genreOverTimeFuture.join(),
                jellyfinGenreOverTimeFuture.join());
    }

    // -------------------------------------------------------------------------
    // Public compute methods — shared with YearRewindService via injection.
    // Each accepts nullable from/to for optional date-range scoping.
    // -------------------------------------------------------------------------

    public OverallStatsDTO computeOverallStats(EntityManager em, UUID profileId, Instant from, Instant to) {
        Tuple result = StatisticsSpecifications.queryOverallStats(em, profileId, from, to);
        Long total = result.get("totalRatings", Long.class);
        Double avg = result.get("averageRating", Double.class);
        Float min = result.get("minRating", Float.class);
        Float max = result.get("maxRating", Float.class);
        return new OverallStatsDTO(
                total != null ? total : 0L,
                null,
                ValueResolver.round2(avg),
                min != null ? min : 0.0f,
                max != null ? max : 0.0f);
    }

    public List<RatingDistributionDTO> computeRatingDistribution(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryRatingDistribution(em, profileId, from, to);
        long totalCount = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();
        List<RatingDistributionDTO> distribution = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
            distribution.add(new RatingDistributionDTO(
                    tuple.get("range", String.class),
                    count,
                    ValueResolver.round2(percentage)));
        }
        return distribution;
    }

    public List<RatingDistributionDTO> computeRatingDistributionByInteger(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryRatingDistributionByInteger(em, profileId, from, to);
        Map<Integer, Long> countByBucket = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            Integer bucket = tuple.get("bucket", Integer.class);
            Long count = tuple.get("count", Long.class);
            if (bucket != null) countByBucket.put(bucket, count != null ? count : 0L);
        }
        long totalCount = countByBucket.values().stream().mapToLong(Long::longValue).sum();
        List<RatingDistributionDTO> distribution = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            long count = countByBucket.getOrDefault(i, 0L);
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
            distribution.add(new RatingDistributionDTO(String.valueOf(i), count, ValueResolver.round2(percentage)));
        }
        return distribution;
    }

    public List<MediaTypeBreakdownDTO> computeJellyfinMediaTypeBreakdown(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinMediaTypeBreakdown(em, profileId, from, to);
        long totalWatched = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();
        List<MediaTypeBreakdownDTO> breakdown = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            String mediaType = String.valueOf(tuple.get("mediaType"));
            double percentage = totalWatched > 0 ? (count * 100.0 / totalWatched) : 0.0;
            breakdown.add(new MediaTypeBreakdownDTO(
                    mediaType, count, count,
                    ValueResolver.round2(percentage),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return breakdown;
    }

    public List<GenreStatDTO> computeTopGenresByCount(EntityManager em, UUID profileId, int limit, Instant from, Instant to) {
        return mapGenreTuples(StatisticsSpecifications.queryTopGenresBy(em, profileId, 0f, limit, StatisticsSpecifications.SortBy.COUNT, from, to));
    }

    public List<GenreStatDTO> computeTopGenresByScore(EntityManager em, UUID profileId, float ratingThreshold, int limit, Instant from, Instant to) {
        return mapGenreTuples(StatisticsSpecifications.queryTopGenresBy(em, profileId, ratingThreshold, limit, StatisticsSpecifications.SortBy.SCORE, from, to));
    }

    public List<GenreStatDTO> computeJellyfinTopGenresByCount(EntityManager em, UUID profileId, int limit, Instant from, Instant to) {
        return mapGenreTuples(StatisticsSpecifications.queryJellyfinTopGenresBy(em, profileId, limit, StatisticsSpecifications.SortBy.COUNT, from, to));
    }

    public List<PersonStatDTO> computeFavoriteDirectors(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy, Instant from, Instant to) {
        return mapPersonTuples(
                StatisticsSpecifications.queryFavoriteCrewByJob(em, profileId, "Director", minCount, limit, sortBy, from, to),
                profileImageSize);
    }

    public List<PersonStatDTO> computeFavoriteProducers(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy, Instant from, Instant to) {
        return mapPersonTuples(
                StatisticsSpecifications.queryFavoriteCrewByJob(em, profileId, "Producer", minCount, limit, sortBy, from, to),
                profileImageSize);
    }

    public List<PersonStatDTO> computeFavoriteActors(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy, Instant from, Instant to) {
        return mapPersonTuples(
                StatisticsSpecifications.queryFavoriteActors(em, profileId, minCount, limit, sortBy, from, to),
                profileImageSize);
    }

    public List<DecadeStatDTO> computeDecadePreferences(EntityManager em, UUID profileId, Instant from, Instant to) {
        return mapDecadeTuples(StatisticsSpecifications.queryDecadePreferences(em, profileId, from, to));
    }

    public List<DecadeStatDTO> computeJellyfinDecadePreferences(EntityManager em, UUID profileId, Instant from, Instant to) {
        return mapDecadeTuples(StatisticsSpecifications.queryJellyfinDecadePreferences(em, profileId, from, to));
    }

    public List<DayOfWeekActivityDTO> computeDayOfWeekActivity(EntityManager em, UUID profileId, Instant from, Instant to) {
        return mapDayOfWeekTuples(StatisticsSpecifications.queryDayOfWeekActivity(em, profileId, from, to));
    }

    public List<DayOfWeekActivityDTO> computeJellyfinDayOfWeekActivity(EntityManager em, UUID profileId, Instant from, Instant to) {
        return mapDayOfWeekTuples(StatisticsSpecifications.queryJellyfinDayOfWeekActivity(em, profileId, from, to));
    }

    // All-time version enriches with total library item count.
    private OverallStatsDTO getOverallStats(EntityManager em, UUID profileId) {
        Tuple result = StatisticsSpecifications.queryOverallStats(em, profileId, null, null);
        Long totalItems = StatisticsSpecifications.queryTotalItems(em);
        Long total = result.get("totalRatings", Long.class);
        Double avg = result.get("averageRating", Double.class);
        Float min = result.get("minRating", Float.class);
        Float max = result.get("maxRating", Float.class);
        return new OverallStatsDTO(
                total != null ? total : 0L,
                totalItems,
                ValueResolver.round2(avg),
                min != null ? min : 0.0f,
                max != null ? max : 0.0f);
    }

    // All-time version enriches each type with total items in library.
    private List<MediaTypeBreakdownDTO> getMediaTypeBreakdown(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryMediaTypeBreakdown(em, profileId, null, null);
        List<Tuple> totals = StatisticsSpecifications.queryMediaTypeTotal(em);

        Map<String, Long> totalByType = new HashMap<>();
        for (Tuple t : totals) {
            totalByType.put(String.valueOf(t.get("mediaType")), t.get("totalCount", Long.class));
        }

        long totalRated = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();
        List<MediaTypeBreakdownDTO> breakdown = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            String mediaType = String.valueOf(tuple.get("mediaType"));
            double percentage = totalRated > 0 ? (count * 100.0 / totalRated) : 0.0;
            breakdown.add(new MediaTypeBreakdownDTO(
                    mediaType,
                    count,
                    totalByType.getOrDefault(mediaType, 0L),
                    ValueResolver.round2(percentage),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return breakdown;
    }

    private List<RecentTrendsDTO> getRecentTrends(EntityManager em, UUID profileId) {
        Instant now = Instant.now();
        return List.of(
                buildTrend(em, profileId, now.minus(30, ChronoUnit.DAYS), now, "30_DAYS"),
                buildTrend(em, profileId, now.minus(90, ChronoUnit.DAYS), now, "90_DAYS"),
                buildTrend(em, profileId, now.minus(365, ChronoUnit.DAYS), now, "365_DAYS"));
    }

    private List<RecentTrendsDTO> getJellyfinRecentTrends(EntityManager em, UUID profileId) {
        Instant now = Instant.now();
        return List.of(
                buildJellyfinTrend(em, profileId, now.minus(30, ChronoUnit.DAYS), now, "30_DAYS"),
                buildJellyfinTrend(em, profileId, now.minus(90, ChronoUnit.DAYS), now, "90_DAYS"),
                buildJellyfinTrend(em, profileId, now.minus(365, ChronoUnit.DAYS), now, "365_DAYS"));
    }

    private RecentTrendsDTO buildTrend(EntityManager em, UUID profileId, Instant start, Instant end, String label) {
        Tuple result = StatisticsSpecifications.queryTrendForPeriod(em, profileId, start, end);
        Long count = result.get("count", Long.class);
        Double avg = result.get("averageRating", Double.class);
        return new RecentTrendsDTO(label, count != null ? count : 0L, ValueResolver.round2(avg));
    }

    private RecentTrendsDTO buildJellyfinTrend(EntityManager em, UUID profileId, Instant start, Instant end, String label) {
        Tuple result = StatisticsSpecifications.queryJellyfinTrendForPeriod(em, profileId, start, end);
        Long count = result.get("count", Long.class);
        Double avg = result.get("averageRating", Double.class);
        return new RecentTrendsDTO(label, count != null ? count : 0L, ValueResolver.round2(avg));
    }

    private List<RatingActivityDTO> getMonthlyActivity(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryMonthlyActivity(em, profileId);
        List<RatingActivityDTO> activity = new ArrayList<>();
        for (Tuple tuple : results) {
            activity.add(new RatingActivityDTO(
                    tuple.get("period", String.class),
                    tuple.get("count", Long.class),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return activity;
    }

    private RuntimeStatsDTO getRuntimeStats(EntityManager em, UUID profileId) {
        Tuple result = StatisticsSpecifications.queryRatingRuntimeStats(em, profileId);
        Double avg = result.get("averageRuntime", Double.class);
        Integer longest = result.get("longestRuntime", Integer.class);
        Integer shortest = result.get("shortestRuntime", Integer.class);
        long movieTotal = StatisticsSpecifications.queryRatedMoviesTotalRuntime(em, profileId);
        long seriesTotal = StatisticsSpecifications.queryRatedSeriesRuntimeTotal(em, profileId);
        return new RuntimeStatsDTO(
                avg != null ? (int) Math.round(avg) : 0,
                movieTotal + seriesTotal,
                longest != null ? longest : 0,
                shortest != null ? shortest : 0);
    }

    private RuntimeStatsDTO getJellyfinRuntimeStats(EntityManager em, UUID profileId) {
        Tuple result = StatisticsSpecifications.queryRuntimeStats(em, profileId, null, null);
        Double avg = result.get("averageRuntime", Double.class);
        Integer longest = result.get("longestRuntime", Integer.class);
        Integer shortest = result.get("shortestRuntime", Integer.class);
        long movieTotal = StatisticsSpecifications.queryMoviesTotalRuntime(em, profileId, null, null);
        long seriesTotal = StatisticsSpecifications.querySeriesRuntimeTotal(em, profileId, null, null);
        return new RuntimeStatsDTO(
                avg != null ? (int) Math.round(avg) : 0,
                movieTotal + seriesTotal,
                longest != null ? longest : 0,
                shortest != null ? shortest : 0);
    }

    private RatingConsistencyDTO getRatingConsistency(EntityManager em, UUID profileId) {
        List<Float> ratings = StatisticsSpecifications.queryAllRatings(em, profileId);

        if (ratings.isEmpty()) {
            return new RatingConsistencyDTO(0.0, 0.0, "NO_DATA");
        }

        double mean = ratings.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
        double variance =
                ratings.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        String level;
        if (stdDev < 1.0) {
            level = "VERY_CONSISTENT";
        } else if (stdDev < 1.5) {
            level = "CONSISTENT";
        } else if (stdDev < 2.0) {
            level = "MODERATE";
        } else if (stdDev < 2.5) {
            level = "VARIED";
        } else {
            level = "VERY_VARIED";
        }

        return new RatingConsistencyDTO(
                Math.round(stdDev * 100.0) / 100.0, Math.round(variance * 100.0) / 100.0, level);
    }

    private List<RatingHeatmapYearDTO> getRatingHeatmap(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryRatingHeatmap(em, profileId);

        // Group days by year (first 4 characters of "YYYY-MM-DD"), preserving ascending order.
        Map<Integer, List<RatingHeatmapDayDTO>> byYear = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String date = tuple.get("date", String.class);
            Long count = tuple.get("count", Long.class);
            if (date == null || count == null) continue;
            int year = Integer.parseInt(date.substring(0, 4));
            byYear.computeIfAbsent(year, y -> new ArrayList<>())
                    .add(new RatingHeatmapDayDTO(date, count));
        }

        List<RatingHeatmapYearDTO> heatmap = new ArrayList<>();
        byYear.forEach((year, days) -> heatmap.add(new RatingHeatmapYearDTO(year, days)));
        return heatmap;
    }

    private List<RatingHeatmapYearDTO> getUniqueMediaPlayedHeatmap(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryUniqueMediaPlayedHeatmap(em, profileId);

        Map<Integer, List<RatingHeatmapDayDTO>> byYear = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String date = tuple.get("date", String.class);
            Long count = tuple.get("count", Long.class);
            if (date == null || count == null) continue;
            int year = Integer.parseInt(date.substring(0, 4));
            byYear.computeIfAbsent(year, y -> new ArrayList<>())
                    .add(new RatingHeatmapDayDTO(date, count));
        }

        List<RatingHeatmapYearDTO> heatmap = new ArrayList<>();
        byYear.forEach((year, days) -> heatmap.add(new RatingHeatmapYearDTO(year, days)));
        return heatmap;
    }

    private List<GenreOverTimeYearDTO> getGenreOverTime(EntityManager em, UUID profileId, int limit) {
        List<Tuple> results = StatisticsSpecifications.queryGenreOverTime(em, profileId, limit);

        // Group rows by year, keeping the top-limit genres per year (query already orders by
        // year ASC, count DESC so insertion order gives us the ranked genres).
        Map<Integer, List<GenreYearStatDTO>> byYear = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String yearStr = tuple.get("year", String.class);
            if (yearStr == null) continue;
            int year = Integer.parseInt(yearStr);
            List<GenreYearStatDTO> genres = byYear.computeIfAbsent(year, y -> new ArrayList<>());
            if (genres.size() < limit) {
                genres.add(new GenreYearStatDTO(
                        tuple.get("genreName", String.class),
                        tuple.get("count", Long.class),
                        ValueResolver.round2(tuple.get("averageRating", Double.class))));
            }
        }

        List<GenreOverTimeYearDTO> overTime = new ArrayList<>();
        byYear.forEach((year, genres) -> overTime.add(new GenreOverTimeYearDTO(year, genres)));
        return overTime;
    }

    private List<GenreOverTimeYearDTO> getJellyfinGenreOverTime(EntityManager em, UUID profileId, int limit) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinGenreOverTime(em, profileId, limit);

        Map<Integer, List<GenreYearStatDTO>> byYear = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String yearStr = tuple.get("year", String.class);
            if (yearStr == null) continue;
            int year = Integer.parseInt(yearStr);
            List<GenreYearStatDTO> genres = byYear.computeIfAbsent(year, y -> new ArrayList<>());
            if (genres.size() < limit) {
                genres.add(new GenreYearStatDTO(
                        tuple.get("genreName", String.class),
                        tuple.get("count", Long.class),
                        ValueResolver.round2(tuple.get("averageRating", Double.class))));
            }
        }

        List<GenreOverTimeYearDTO> overTime = new ArrayList<>();
        byYear.forEach((year, genres) -> overTime.add(new GenreOverTimeYearDTO(year, genres)));
        return overTime;
    }

    private List<GenreStatDTO> mapGenreTuples(List<Tuple> results) {
        List<GenreStatDTO> genreStats = new ArrayList<>();
        for (Tuple tuple : results) {
            genreStats.add(new GenreStatDTO(
                    tuple.get("genreName", String.class),
                    tuple.get("count", Long.class),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return genreStats;
    }

    private List<PersonStatDTO> mapPersonTuples(List<Tuple> results, String profileImageSize) {
        List<PersonStatDTO> people = new ArrayList<>();
        for (Tuple tuple : results) {
            String rawPath = tuple.get("profilePathUrl", String.class);
            String imageUrl = rawPath != null ? tmdbClient.getImageUrl(rawPath, profileImageSize) : null;
            people.add(new PersonStatDTO(
                    tuple.get("personId", UUID.class),
                    tuple.get("name", String.class),
                    imageUrl,
                    ValueResolver.round2(tuple.get("averageRating", Double.class)),
                    tuple.get("itemCount", Long.class)));
        }
        return people;
    }

    private List<DecadeStatDTO> mapDecadeTuples(List<Tuple> results) {
        List<DecadeStatDTO> decadeStats = new ArrayList<>();
        for (Tuple tuple : results) {
            decadeStats.add(new DecadeStatDTO(
                    tuple.get("decade", Integer.class),
                    tuple.get("count", Long.class),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return decadeStats;
    }

    private List<DayOfWeekActivityDTO> mapDayOfWeekTuples(List<Tuple> results) {
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        List<DayOfWeekActivityDTO> activity = new ArrayList<>();
        for (Tuple tuple : results) {
            String dayNum = tuple.get("dayOfWeek", String.class);
            if (dayNum != null) {
                activity.add(new DayOfWeekActivityDTO(dayNames[Integer.parseInt(dayNum)], tuple.get("count", Long.class)));
            }
        }
        return activity;
    }
}
