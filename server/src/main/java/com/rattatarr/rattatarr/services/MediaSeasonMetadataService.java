package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbSeasonWithEpisodesResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbShowResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.models.entities.MediaSeasonMetadata;
import com.rattatarr.rattatarr.repositories.MediaSeasonMetadataRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@NullMarked
public class MediaSeasonMetadataService extends BaseService<MediaSeasonMetadata, MediaSeasonMetadataRepository> {

    private final MediaSeasonsService mediaSeasonsService;

    public MediaSeasonMetadataService(
            MediaSeasonMetadataRepository repository,
            MediaSeasonsService mediaSeasonsService
    ) {
        super(repository);
        this.mediaSeasonsService = mediaSeasonsService;
    }

    @Override
    protected void updateEntity(MediaSeasonMetadata existing, MediaSeasonMetadata updated) {
        existing.setEpisodeCount(updated.episodeCount());
        existing.setPosterImageUrl(updated.posterImageUrl());
        existing.setAirDate(updated.airDate());
    }

    @Transactional(readOnly = true)
    public Optional<MediaSeasonMetadata> findByMediaSeasonId(UUID seasonId) {
        return repository.findByMediaSeasonId(seasonId);
    }

    @Transactional
    public MediaSeasonMetadata upsertWithRefresh(MediaSeasonMetadata seasonMetadata, Boolean forceRefresh) {
        Optional<MediaSeasonMetadata> existing = findByMediaSeasonId(seasonMetadata.mediaSeason().id());

        if (existing.isPresent() && !forceRefresh) {
            logger.debug("Skipping update for existing season metadata (forceRefresh=false)");
            return existing.get();
        }

        return upsert(existing.orElse(null), seasonMetadata);
    }

    @Transactional
    public void upsertBatchFromTMDbShow(
            MediaItem mediaItem, TMDbShowResponseDTO tmDbShow, Boolean forceRefresh
    ) {
        if (ObjectUtils.isEmpty(tmDbShow.seasons())) {
            return;
        }
        logger.debug("Enriching season metadata for series '{}' ({}, {} seasons) from TMDb...",
                mediaItem.title(), mediaItem.id(), tmDbShow.seasons().size());

        for (var season : tmDbShow.seasons()) {
            if (season.seasonNumber() == null) continue;

            Optional<MediaSeason> optionalMediaSeason = mediaSeasonsService.findByMediaItemAndSeason(mediaItem, season.seasonNumber());
            if (optionalMediaSeason.isEmpty()) {
                logger.warn("No MediaSeason found for series '{}' ({}) season {}. Skipping season metadata creation.",
                        mediaItem.title(), mediaItem.id(), season.seasonNumber());
                continue;
            }

            MediaSeasonMetadata seasonMetadata = new MediaSeasonMetadata(
                    optionalMediaSeason.get(),
                    season.episodeCount(),
                    season.posterPath(),
                    season.airDate()
            );

            upsertWithRefresh(seasonMetadata, forceRefresh);
        }
    }

    @Transactional
    public void upsertBatchFromSeasonDetails(
            Map<Integer, TMDbSeasonWithEpisodesResponseDTO> seasonDetails,
            MediaItem mediaItem,
            Boolean forceRefresh
    ) {
        if (seasonDetails.isEmpty()) {
            logger.debug("No season details to save metadata for");
            return;
        }

        logger.debug("Batch upserting season metadata for {} seasons of '{}' ({})",
                seasonDetails.size(), mediaItem.title(), mediaItem.id());

        for (Map.Entry<Integer, TMDbSeasonWithEpisodesResponseDTO> entry : seasonDetails.entrySet()) {
            int seasonNumber = entry.getKey();
            TMDbSeasonWithEpisodesResponseDTO seasonData = entry.getValue();

            if (seasonData == null) {
                logger.debug("No season data for season {}, skipping metadata", seasonNumber);
                continue;
            }

            Optional<MediaSeason> seasonOpt = mediaSeasonsService.findByMediaItemAndSeason(mediaItem, seasonNumber);
            if (seasonOpt.isEmpty()) {
                logger.warn("Season {} not found for series '{}' ({}), skipping metadata",
                        seasonNumber, mediaItem.title(), mediaItem.id());
                continue;
            }

            MediaSeason mediaSeason = seasonOpt.get();
            int episodeCount = seasonData.episodes() != null ? seasonData.episodes().size() : 0;

            MediaSeasonMetadata seasonMetadata = new MediaSeasonMetadata(
                    mediaSeason,
                    episodeCount,
                    seasonData.posterPath(),
                    seasonData.airDate()
            );

            upsertWithRefresh(seasonMetadata, forceRefresh);
        }

        logger.debug("Saved metadata for {} seasons of '{}' ({})",
                seasonDetails.size(), mediaItem.title(), mediaItem.id());
    }
}
