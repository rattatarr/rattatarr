package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import com.rattatarr.rattatarr.services.ProfileStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Periodically warms the profile-statistics cache so that users never hit a
 * cold-cache on the first request of each 10-minute window.
 * Calling through the injected {@link ProfileStatisticsService} bean ensures
 * the Spring proxy intercepts the call and populates the Caffeine cache via
 * {@code @Cacheable}.
 */
@Service
public class StatisticsCacheWarmerScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsCacheWarmerScheduler.class);

    private static final float DEFAULT_RATING_THRESHOLD = 7.0f;
    private static final int DEFAULT_MIN_COUNT = 1;
    private static final int DEFAULT_GENRES_LIMIT = 10;
    private static final int DEFAULT_ACTORS_LIMIT = 10;
    private static final int DEFAULT_DIRECTORS_LIMIT = 10;
    private static final int DEFAULT_PRODUCERS_LIMIT = 10;
    private static final int DEFAULT_GENRE_OVER_TIME_LIMIT = 5;
    private static final String DEFAULT_PROFILE_IMAGE_SIZE = "w185";

    private final ProfileStatisticsService profileStatisticsService;
    private final ProfilesRepository profilesRepository;

    public StatisticsCacheWarmerScheduler(
            ProfileStatisticsService profileStatisticsService,
            ProfilesRepository profilesRepository) {
        this.profileStatisticsService = profileStatisticsService;
        this.profilesRepository = profilesRepository;
    }

    @Scheduled(
            fixedRateString = "${rattatarr.cache.statistics-warm-interval:PT10M}",
            initialDelayString = "${rattatarr.cache.statistics-warm-initial-delay:PT1M}"
    )
    public void warmStatisticsCache() {
        List<Profile> profiles = profilesRepository.findAll();

        if (profiles.isEmpty()) {
            logger.debug("Statistics cache warm: no profiles found, skipping");
            return;
        }

        Instant startTime = Instant.now();
        logger.info("Statistics cache warm started for {} profile(s)", profiles.size());

        int succeeded = 0;
        int failed = 0;

        for (Profile profile : profiles) {
            try {
                profileStatisticsService.getStatistics(
                        profile.id(),
                        DEFAULT_RATING_THRESHOLD,
                        DEFAULT_MIN_COUNT,
                        DEFAULT_GENRES_LIMIT,
                        DEFAULT_ACTORS_LIMIT,
                        DEFAULT_DIRECTORS_LIMIT,
                        DEFAULT_PRODUCERS_LIMIT,
                        DEFAULT_GENRE_OVER_TIME_LIMIT,
                        DEFAULT_PROFILE_IMAGE_SIZE);
                succeeded++;
            } catch (Exception e) {
                logger.warn("Statistics cache warm failed for profile {}: {}", profile.id(), e.getMessage());
                failed++;
            }
        }

        Duration duration = Duration.between(startTime, Instant.now());
        logger.info(
                "Statistics cache warm completed in {} ms — {} succeeded, {} failed",
                duration.toMillis(), succeeded, failed);
    }
}
