package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditsResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import com.rattatarr.rattatarr.specifications.MediaItemSpecifications;
import com.rattatarr.rattatarr.utils.ParallelAPIProcessor;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

@Service
@NullMarked
public class MediaItemCreditsService {
    private static final Logger logger = LoggerFactory.getLogger(MediaItemCreditsService.class);

    private final MediaItemsRepository mediaItemsRepository;
    private final TMDbClient tmdbClient;
    private final PeopleService peopleService;
    private final Executor tmdbApiExecutor;

    public MediaItemCreditsService(
            MediaItemsRepository mediaItemsRepository,
            TMDbClient tmdbClient,
            PeopleService peopleService,
            @Qualifier("tmdbApiExecutor") Executor tmdbApiExecutor
    ) {
        this.mediaItemsRepository = mediaItemsRepository;
        this.tmdbClient = tmdbClient;
        this.peopleService = peopleService;
        this.tmdbApiExecutor = tmdbApiExecutor;
    }

    private List<MediaItem> fetchMediaItemsWithoutCredits() {
        Specification<MediaItem> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                MediaItemSpecifications.missingCastOrCrew()
        );

        return mediaItemsRepository.findAll(spec);
    }

    private TMDbFetchResult fetchCreditsFromTMDb(MediaItem mediaItem) {
        switch (mediaItem.mediaType()) {
            case MOVIE -> {
                logger.info("Fetching credits for Movie MediaItem ID: {} from TMDb...", mediaItem.id());
                var credits = tmdbClient.findMovieCreditsById(mediaItem.TMDbId());
                return new TMDbFetchResult(mediaItem, credits);
            }
            case SERIES -> {
                logger.info("Fetching credits for Series MediaItem ID: {} from TMDb...", mediaItem.id());
                var credits = tmdbClient.findTVShowCreditsById(mediaItem.TMDbId());
                return new TMDbFetchResult(mediaItem, credits);
            }
            default -> {
                logger.warn("Unsupported media type for MediaItem ID: {}. Skipping credits fetch.", mediaItem.id());
                throw new IllegalArgumentException("Unsupported media type: " + mediaItem.mediaType());
            }
        }
    }

    /**
     * Save credits to DB (must be sequential for SQLite).
     */
    private void saveCreditsToDB(TMDbFetchResult result, Boolean forceRefresh) {
        peopleService.upsertPeopleFromCredits(result.credits, result.mediaItem, forceRefresh);
    }

    /**
     * Update all media item credits with parallel TMDb calls and sequential DB writes.
     * TMDb API calls happen concurrently (I/O bound), while DB writes are sequential (SQLite single writer).
     */
    public void updateAllMediaItemCredits(Boolean forceRefresh) {
        List<MediaItem> mediaItems = fetchMediaItemsWithoutCredits();

        ParallelAPIProcessor.processInParallel(
                mediaItems,
                this::fetchCreditsFromTMDb,
                result -> saveCreditsToDB(result, forceRefresh),
                tmdbApiExecutor,
                logger,
                "media item credits"
        );
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundCreditsUpdate(Boolean forceRefresh) {
        logger.info("TMDb credits update started");
        try {
            updateAllMediaItemCredits(forceRefresh);
            logger.info("TMDb credits update completed successfully");
        } catch (Exception e) {
            logger.error("Error during TMDb credits update", e);
        }
    }

    private record TMDbFetchResult(
            MediaItem mediaItem,
            TMDbCreditsResponseDTO credits
    ) {
    }
}
