package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbFindItemResponseDTO;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.IMDbWatchlistColumn;
import com.rattatarr.rattatarr.models.IMDbWatchlistRow;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.utils.CSVProcessor;
import com.rattatarr.rattatarr.utils.ParallelAPIProcessor;
import org.apache.commons.csv.CSVRecord;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
@NullMarked
public class IMDbImportService {
    private static final Logger logger = LoggerFactory.getLogger(IMDbImportService.class);

    private final TMDbClient tmdbClient;
    private final TMDbService tmDbService;
    private final ProfilesService profilesService;
    private final MediaItemRatingsService mediaItemRatingsService;
    private final Executor tmdbApiExecutor;

    public IMDbImportService(
            TMDbClient tmdbClient,
            TMDbService tmDbService,
            ProfilesService profilesService,
            MediaItemRatingsService mediaItemRatingsService,
            @Qualifier("tmdbApiExecutor") Executor tmdbApiExecutor) {
        this.tmdbClient = tmdbClient;
        this.tmDbService = tmDbService;
        this.profilesService = profilesService;
        this.mediaItemRatingsService = mediaItemRatingsService;
        this.tmdbApiExecutor = tmdbApiExecutor;
    }

    private Optional<IMDbWatchlistRow> mapIMDbCSVRow(CSVRecord record) {
        String IMDbId = record.get(IMDbWatchlistColumn.CONST.toString());
        String itemType = record.get(IMDbWatchlistColumn.TITLE_TYPE.toString());
        String ratingStr = record.get(IMDbWatchlistColumn.YOUR_RATING.toString());
        String dateRatedStr = record.get(IMDbWatchlistColumn.DATE_RATED.toString());

        Float rating = null;
        if (StringUtils.hasText(ratingStr)) {
            try {
                rating = Float.parseFloat(ratingStr);
            } catch (NumberFormatException e) {
                logger.warn("Invalid rating format for IMDb ID {}: {}", IMDbId, ratingStr);
                return Optional.empty();
            }
        }
        Instant dateRated = null;
        if (StringUtils.hasText(dateRatedStr)) {
            try {
                LocalDate localDate = LocalDate.parse(dateRatedStr);
                dateRated = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (DateTimeParseException e) {
                logger.warn("Invalid date format for IMDb ID {}: {}", IMDbId, dateRatedStr);
            }
        }

        MediaType mediaType;
        if (itemType.equalsIgnoreCase("movie")) {
            mediaType = MediaType.MOVIE;
        } else if (itemType.toLowerCase().contains("series")) {
            mediaType = MediaType.SERIES;
        } else {
            logger.warn("Unsupported media type for IMDb ID {}: {}", IMDbId, itemType);
            return Optional.empty();
        }

        IMDbWatchlistRow row = new IMDbWatchlistRow(IMDbId, rating, itemType, dateRated, mediaType);
        return row.isImportable() ? Optional.of(row) : Optional.empty();
    }

    @Nullable
    private TMDbFetchResult fetchTMDbId(IMDbWatchlistRow row) {
        TMDbFindItemResponseDTO tmDbItem = switch (row.mediaType()) {
            case MOVIE -> tmdbClient.findMovieByIMDbId(row.IMDbId());
            case SERIES -> tmdbClient.findTVShowByIMDbId(row.IMDbId());
        };

        if (tmDbItem == null) {
            logger.warn("No TMDb item found for IMDb ID: {}", row.IMDbId());
            return null;
        }

        return new TMDbFetchResult(row, tmDbItem.id().toString());
    }

    /**
     * Import media item and save rating to DB (must be sequential for SQLite).
     */
    private void importAndSaveRating(TMDbFetchResult result, Profile profile) {
        var mediaItem = tmDbService.importMediaItem(result.TMDbId, result.row.mediaType());

        if (result.row.rating() != null) {
            mediaItemRatingsService.upsert(profile, mediaItem, result.row.rating(), result.row.dateRated());
        }
    }

    /**
     * Import ratings with parallel TMDb lookups and sequential DB writes.
     * TMDb API calls happen concurrently (I/O bound), while DB writes are sequential (SQLite single writer).
     */
    public void importRatings(List<IMDbWatchlistRow> rows, Profile profile) {
        ParallelAPIProcessor.processInParallel(
                rows,
                this::fetchTMDbId,
                result -> importAndSaveRating(result, profile),
                tmdbApiExecutor,
                logger,
                "IMDb ratings"
        );
    }

    public List<IMDbWatchlistRow> parseCSV(MultipartFile file) {
        return CSVProcessor.parseCSV(file, this::mapIMDbCSVRow);
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundImportRatings(List<IMDbWatchlistRow> rows, UUID profileId) {
        Profile profile = profilesService.findByIdOrThrow(
                profileId,
                ProfilesExceptions.ProfileNotFoundExceptions::new
        );

        logger.info("IMDb ratings import started for profile {}", profileId);
        try {
            importRatings(rows, profile);
            logger.info("IMDb ratings import completed successfully for profile {}", profileId);
        } catch (Exception error) {
            logger.error("Error during IMDb ratings import for profile {}", profileId, error);
        }
    }

    private record TMDbFetchResult(
            IMDbWatchlistRow row,
            String TMDbId
    ) {
    }
}
