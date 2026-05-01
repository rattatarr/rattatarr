package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.responses.*;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import com.rattatarr.rattatarr.specifications.StatisticsSpecifications;
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
import java.util.function.Function;

@Service
public class YearRewindService {
    protected final Logger logger = LoggerFactory.getLogger(YearRewindService.class);

    private final EntityManagerFactory entityManagerFactory;
    private final ProfilesRepository profilesRepository;
    private final Executor statisticsExecutor;
    private final MediaItemViewHelper mediaItemViewHelper;

    public YearRewindService(
            EntityManagerFactory entityManagerFactory,
            ProfilesRepository profilesRepository,
            @Qualifier("statisticsExecutor") Executor statisticsExecutor,
            MediaItemViewHelper mediaItemViewHelper) {
        this.entityManagerFactory = entityManagerFactory;
        this.profilesRepository = profilesRepository;
        this.statisticsExecutor = statisticsExecutor;
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
                query("overallStats", em -> getOverallStats(em, profileId, from, to));
        CompletableFuture<List<RatingDistributionDTO>> distributionFuture =
                query("ratingDistribution", em -> getRatingDistribution(em, profileId, from, to));
        CompletableFuture<List<RatingDistributionDTO>> distributionByIntegerFuture =
                query("ratingDistributionByInteger", em -> getRatingDistributionByInteger(em, profileId, from, to));
        CompletableFuture<List<MediaTypeBreakdownDTO>> mediaTypeFuture =
                query("mediaTypeBreakdown", em -> getMediaTypeBreakdown(em, profileId, from, to));
        CompletableFuture<List<MediaTypeBreakdownDTO>> jellyfinMediaTypeFuture =
                query("jellyfinMediaTypeBreakdown", em -> getJellyfinMediaTypeBreakdown(em, profileId, from, to));
        CompletableFuture<List<GenreStatDTO>> genresByCountFuture =
                query("topGenresByCount", em -> getTopGenresByCount(em, profileId, genresLimit, from, to));
        CompletableFuture<List<GenreStatDTO>> genresByScoreFuture =
                query("topGenresByScore", em -> getTopGenresByScore(em, profileId, ratingThreshold, genresLimit, from, to));
        CompletableFuture<List<GenreStatDTO>> jellyfinGenresByCountFuture =
                query("jellyfinTopGenresByCount", em -> getJellyfinTopGenresByCount(em, profileId, genresLimit, from, to));
        CompletableFuture<List<PersonStatDTO>> directorsByCountFuture =
                query("directorsByCount", em -> getFavoriteDirectors(em, profileId, minCount, directorsLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, from, to));
        CompletableFuture<List<PersonStatDTO>> directorsByScoreFuture =
                query("directorsByScore", em -> getFavoriteDirectors(em, profileId, minCount, directorsLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, from, to));
        CompletableFuture<List<PersonStatDTO>> producersByCountFuture =
                query("producersByCount", em -> getFavoriteProducers(em, profileId, minCount, producersLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, from, to));
        CompletableFuture<List<PersonStatDTO>> producersByScoreFuture =
                query("producersByScore", em -> getFavoriteProducers(em, profileId, minCount, producersLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, from, to));
        CompletableFuture<List<PersonStatDTO>> actorsByCountFuture =
                query("actorsByCount", em -> getFavoriteActors(em, profileId, minCount, actorsLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT, from, to));
        CompletableFuture<List<PersonStatDTO>> actorsByScoreFuture =
                query("actorsByScore", em -> getFavoriteActors(em, profileId, minCount, actorsLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE, from, to));
        CompletableFuture<List<RatingActivityDTO>> monthlyFuture =
                query("monthlyActivity", em -> getMonthlyActivity(em, profileId, from, to));
        CompletableFuture<List<DayOfWeekActivityDTO>> dayOfWeekFuture =
                query("dayOfWeekActivity", em -> getDayOfWeekActivity(em, profileId, from, to));
        CompletableFuture<List<DayOfWeekActivityDTO>> jellyfinDayOfWeekFuture =
                query("jellyfinDayOfWeekActivity", em -> getJellyfinDayOfWeekActivity(em, profileId, from, to));
        CompletableFuture<List<DecadeStatDTO>> decadesFuture =
                query("decadePreferences", em -> getDecadePreferences(em, profileId, from, to));
        CompletableFuture<List<DecadeStatDTO>> jellyfinDecadesFuture =
                query("jellyfinDecadePreferences", em -> getJellyfinDecadePreferences(em, profileId, from, to));
        CompletableFuture<RuntimeStatsDTO> runtimeFuture =
                query("runtimeStats", em -> getRuntimeStats(em, profileId, from, to));
        CompletableFuture<YearRewindHighlightsDTO> highlightsFuture =
                query("highlights", em -> getHighlights(em, profileId, from, to));

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

    private <T> CompletableFuture<T> query(String label, Function<EntityManager, T> fn) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            try (EntityManager em = entityManagerFactory.createEntityManager()) {
                T result = fn.apply(em);
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                logger.debug("Rewind query '{}' completed in {} ms", label, elapsedMs);
                return result;
            } catch (RuntimeException e) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                logger.debug("Rewind query '{}' failed after {} ms", label, elapsedMs);
                throw e;
            }
        }, statisticsExecutor);
    }

    private OverallStatsDTO getOverallStats(EntityManager em, UUID profileId, Instant from, Instant to) {
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

    private List<RatingDistributionDTO> getRatingDistribution(EntityManager em, UUID profileId, Instant from, Instant to) {
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

    private List<RatingDistributionDTO> getRatingDistributionByInteger(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryRatingDistributionByInteger(em, profileId, from, to);

        Map<Integer, Long> countByBucket = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            Integer bucket = tuple.get("bucket", Integer.class);
            Long count = tuple.get("count", Long.class);
            if (bucket != null) {
                countByBucket.put(bucket, count != null ? count : 0L);
            }
        }

        long totalCount = countByBucket.values().stream().mapToLong(Long::longValue).sum();

        List<RatingDistributionDTO> distribution = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            long count = countByBucket.getOrDefault(i, 0L);
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
            distribution.add(new RatingDistributionDTO(
                    String.valueOf(i),
                    count,
                    ValueResolver.round2(percentage)));
        }
        return distribution;
    }

    private List<MediaTypeBreakdownDTO> getMediaTypeBreakdown(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryMediaTypeBreakdown(em, profileId, from, to);

        long totalRated = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();

        List<MediaTypeBreakdownDTO> breakdown = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            String mediaType = String.valueOf(tuple.get("mediaType"));
            double percentage = totalRated > 0 ? (count * 100.0 / totalRated) : 0.0;
            breakdown.add(new MediaTypeBreakdownDTO(
                    mediaType,
                    count,
                    count,
                    ValueResolver.round2(percentage),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return breakdown;
    }

    private List<MediaTypeBreakdownDTO> getJellyfinMediaTypeBreakdown(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinMediaTypeBreakdown(em, profileId, from, to);

        long totalWatched = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();

        List<MediaTypeBreakdownDTO> breakdown = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            String mediaType = String.valueOf(tuple.get("mediaType"));
            double percentage = totalWatched > 0 ? (count * 100.0 / totalWatched) : 0.0;
            breakdown.add(new MediaTypeBreakdownDTO(
                    mediaType,
                    count,
                    count,
                    ValueResolver.round2(percentage),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return breakdown;
    }

    private List<GenreStatDTO> getTopGenresByCount(EntityManager em, UUID profileId, int limit, Instant from, Instant to) {
        return mapGenreTuples(StatisticsSpecifications.queryTopGenresBy(em, profileId, 0f, limit, StatisticsSpecifications.SortBy.COUNT, from, to));
    }

    private List<GenreStatDTO> getTopGenresByScore(EntityManager em, UUID profileId, float ratingThreshold, int limit, Instant from, Instant to) {
        return mapGenreTuples(StatisticsSpecifications.queryTopGenresBy(em, profileId, ratingThreshold, limit, StatisticsSpecifications.SortBy.SCORE, from, to));
    }

    private List<GenreStatDTO> getJellyfinTopGenresByCount(EntityManager em, UUID profileId, int limit, Instant from, Instant to) {
        return mapGenreTuples(StatisticsSpecifications.queryJellyfinTopGenresBy(em, profileId, limit, StatisticsSpecifications.SortBy.COUNT, from, to));
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

    private List<PersonStatDTO> getFavoriteDirectors(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy, Instant from, Instant to) {
        return mapPersonTuples(
                StatisticsSpecifications.queryFavoriteCrewByJob(em, profileId, "Director", minCount, limit, sortBy, from, to),
                profileImageSize);
    }

    private List<PersonStatDTO> getFavoriteProducers(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy, Instant from, Instant to) {
        return mapPersonTuples(
                StatisticsSpecifications.queryFavoriteCrewByJob(em, profileId, "Producer", minCount, limit, sortBy, from, to),
                profileImageSize);
    }

    private List<PersonStatDTO> getFavoriteActors(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy, Instant from, Instant to) {
        return mapPersonTuples(
                StatisticsSpecifications.queryFavoriteActors(em, profileId, minCount, limit, sortBy, from, to),
                profileImageSize);
    }

    private List<PersonStatDTO> mapPersonTuples(List<Tuple> results, String profileImageSize) {
        List<PersonStatDTO> people = new ArrayList<>();
        for (Tuple tuple : results) {
            String rawPath = tuple.get("profilePathUrl", String.class);
            String imageUrl = rawPath != null ? mediaItemViewHelper.buildUrlFromPath(rawPath, profileImageSize) : null;
            people.add(new PersonStatDTO(
                    tuple.get("personId", UUID.class),
                    tuple.get("name", String.class),
                    imageUrl,
                    ValueResolver.round2(tuple.get("averageRating", Double.class)),
                    tuple.get("itemCount", Long.class)));
        }
        return people;
    }

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

    private List<DayOfWeekActivityDTO> getDayOfWeekActivity(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryDayOfWeekActivity(em, profileId, from, to);
        return mapDayOfWeekTuples(results);
    }

    private List<DayOfWeekActivityDTO> getJellyfinDayOfWeekActivity(EntityManager em, UUID profileId, Instant from, Instant to) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinDayOfWeekActivity(em, profileId, from, to);
        return mapDayOfWeekTuples(results);
    }

    private List<DayOfWeekActivityDTO> mapDayOfWeekTuples(List<Tuple> results) {
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        List<DayOfWeekActivityDTO> activity = new ArrayList<>();
        for (Tuple tuple : results) {
            String dayNum = tuple.get("dayOfWeek", String.class);
            if (dayNum != null) {
                activity.add(new DayOfWeekActivityDTO(dayNames[Integer.parseInt(dayNum)],
                        tuple.get("count", Long.class)));
            }
        }
        return activity;
    }

    private List<DecadeStatDTO> getDecadePreferences(EntityManager em, UUID profileId, Instant from, Instant to) {
        return mapDecadeTuples(StatisticsSpecifications.queryDecadePreferences(em, profileId, from, to));
    }

    private List<DecadeStatDTO> getJellyfinDecadePreferences(EntityManager em, UUID profileId, Instant from, Instant to) {
        return mapDecadeTuples(StatisticsSpecifications.queryJellyfinDecadePreferences(em, profileId, from, to));
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

    private RuntimeStatsDTO getRuntimeStats(EntityManager em, UUID profileId, Instant from, Instant to) {
        Tuple result = StatisticsSpecifications.queryRuntimeStatsForPeriod(em, profileId, from, to);

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
            busiestDay = busiestDayResult.get(0).get("date", String.class);
            busiestDayCount = busiestDayResult.get(0).get("count", Long.class);
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
        Tuple tuple = results.get(0);
        String rawPoster = tuple.get("posterImageUrl", String.class);
        return new YearRewindHighlightItemDTO(
                tuple.get("title", String.class),
                rawPoster != null ? mediaItemViewHelper.buildUrlFromPath(rawPoster, "w342") : null,
                tuple.get("rating", Float.class),
                tuple.get("eventAt", java.time.Instant.class),
                String.valueOf(tuple.get("mediaType")));
    }

    private YearRewindHighlightItemDTO mapWatchHighlightItem(List<Tuple> results) {
        if (results.isEmpty()) return null;
        Tuple tuple = results.get(0);
        String rawPoster = tuple.get("posterImageUrl", String.class);
        return new YearRewindHighlightItemDTO(
                tuple.get("title", String.class),
                rawPoster != null ? mediaItemViewHelper.buildUrlFromPath(rawPoster, "w342") : null,
                null,
                tuple.get("eventAt", java.time.Instant.class),
                String.valueOf(tuple.get("mediaType")));
    }

    private RewindStreakDTO computeLongestStreak(List<String> sortedDates) {
        if (sortedDates.isEmpty()) return null;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String streakStart = sortedDates.get(0);
        String streakEnd = sortedDates.get(0);
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
