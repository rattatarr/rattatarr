package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbShowResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemMetadata;
import com.rattatarr.rattatarr.repositories.MediaItemMetadataRepository;
import com.rattatarr.rattatarr.utils.ParallelAPIProcessor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
@NullMarked
public class MediaItemMetadataService extends BaseService<MediaItemMetadata, MediaItemMetadataRepository> {

    private final TMDbClient tmDbClient;
    private final MediaSeasonMetadataService mediaSeasonMetadataService;
    private final MediaItemsService mediaItemsService;
    private final Executor tmdbApiExecutor;

    public MediaItemMetadataService(
            MediaItemMetadataRepository repository,
            TMDbClient tmDbClient,
            MediaSeasonMetadataService mediaSeasonMetadataService,
            MediaItemsService mediaItemsService,
            @Qualifier("tmdbApiExecutor") Executor tmdbApiExecutor
    ) {
        super(repository);
        this.tmDbClient = tmDbClient;
        this.mediaSeasonMetadataService = mediaSeasonMetadataService;
        this.mediaItemsService = mediaItemsService;
        this.tmdbApiExecutor = tmdbApiExecutor;
    }

    @Override
    protected void updateEntity(MediaItemMetadata existing, MediaItemMetadata updated) {

    }

    public MediaItemMetadata upsert(MediaItemMetadata metadata, Boolean forceRefresh) {
        repository.findByMediaItemId(metadata.mediaItem().id())
                .map(existingMetadata -> {
                    if (!forceRefresh) return existingMetadata;

                    existingMetadata.setDescription(metadata.description());
                    existingMetadata.setPosterImageUrl(metadata.posterImageUrl());
                    existingMetadata.setBackdropImageUrl(metadata.backdropImageUrl());
                    // Preserve Radarr-sourced ratings — not overwritten by TMDb refresh
                    return repository.save(existingMetadata);
                })
                .orElseGet(() -> repository.save(metadata));

        return metadata;
    }

    @Transactional(readOnly = true)
    public boolean isRatingFresh(UUID mediaItemId) {
        return repository.findByMediaItemId(mediaItemId)
                .map(metadata -> {
                    boolean hasRating = metadata.imdbRating() != null || metadata.rottenTomatoesRating() != null;
                    if (!hasRating) return false;
                    return metadata.updatedAt().isAfter(Instant.now().minus(7, ChronoUnit.DAYS));
                })
                .orElse(false);
    }

    @Transactional
    public void updateExternalRatings(MediaItem mediaItem, @Nullable Float imdbRating, @Nullable Integer rottenTomatoesRating) {
        repository.findByMediaItemId(mediaItem.id()).ifPresent(metadata -> {
            metadata.setImdbRating(imdbRating);
            metadata.setRottenTomatoesRating(rottenTomatoesRating);
            repository.save(metadata);
        });
    }

    private TMDbFetchResult fetchMetadataFromTMDb(MediaItem mediaItem) {
        switch (mediaItem.mediaType()) {
            case MOVIE -> {
                logger.info("Fetching metadata for Movie MediaItem ID: {} from TMDb...", mediaItem.id());
                var tmDbMovie = tmDbClient.findMovieById(mediaItem.TMDbId());
                MediaItemMetadata metadata = new MediaItemMetadata(
                        mediaItem,
                        tmDbMovie.overview(),
                        tmDbMovie.posterPath(),
                        tmDbMovie.backdropPath()
                );
                return new TMDbFetchResult(mediaItem, metadata, null);
            }
            case SERIES -> {
                logger.info("Fetching metadata for TV Show MediaItem ID: {} from TMDb...", mediaItem.id());
                var tmDbShow = tmDbClient.findTVShowById(mediaItem.TMDbId());
                MediaItemMetadata metadata = new MediaItemMetadata(
                        mediaItem,
                        tmDbShow.overview(),
                        tmDbShow.posterPath(),
                        tmDbShow.backdropPath()
                );
                return new TMDbFetchResult(mediaItem, metadata, tmDbShow);
            }
            default -> {
                logger.warn("Unsupported media type for TMDb enrichment: {} for MediaItem ID: {}",
                        mediaItem.mediaType(), mediaItem.id());
                throw new IllegalArgumentException("Unsupported media type: " + mediaItem.mediaType());
            }
        }
    }

    /**
     * Save metadata to DB (must be sequential for SQLite single writer).
     * For series, also saves season metadata.
     */
    private void saveMetadataToDB(TMDbFetchResult result, Boolean forceRefresh) {
        upsert(result.metadata, forceRefresh);

        if (result.mediaItem.mediaType() == MediaType.SERIES && result.showResponse != null) {
            mediaSeasonMetadataService.upsertBatchFromTMDbShow(
                    result.mediaItem,
                    result.showResponse,
                    forceRefresh
            );
        }
    }

    @Nullable
    public MediaItemMetadata enrichFromTMDb(MediaItem mediaItem, Boolean forceRefresh) {
        try {
            TMDbFetchResult result = fetchMetadataFromTMDb(mediaItem);
            saveMetadataToDB(result, forceRefresh);
            return result.metadata;
        } catch (Exception e) {
            logger.error("Error enriching metadata for MediaItem ID: {}", mediaItem.id(), e);
            return null;
        }
    }

    /**
     * Refresh all metadata with parallel TMDb calls and sequential DB writes.
     * TMDb API calls happen concurrently (I/O bound), while DB writes are sequential (SQLite single writer).
     */
    public void refreshAllMetadata(Boolean forceRefresh) {
        List<MediaItem> mediaItems = mediaItemsService.findAll().stream()
                .filter(item -> !ObjectUtils.isEmpty(item.TMDbId()))
                .toList();

        ParallelAPIProcessor.processInParallel(
                mediaItems,
                this::fetchMetadataFromTMDb,
                result -> saveMetadataToDB(result, forceRefresh),
                tmdbApiExecutor,
                logger,
                "media items"
        );
    }

    private record TMDbFetchResult(
            MediaItem mediaItem,
            MediaItemMetadata metadata,
            @Nullable TMDbShowResponseDTO showResponse  // Only populated for series
    ) {
    }
}
