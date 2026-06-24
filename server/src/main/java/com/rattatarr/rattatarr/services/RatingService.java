package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.exceptions.MediaItemExceptions;
import com.rattatarr.rattatarr.exceptions.MediaSeasonExceptions;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.DeleteRateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.RateRequestDTO;
import com.rattatarr.rattatarr.models.entities.*;
import com.rattatarr.rattatarr.repositories.MediaItemCastRepository;
import com.rattatarr.rattatarr.repositories.MediaItemCrewRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@NullMarked
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    private final ProfilesService profilesService;
    private final MediaItemRatingsService mediaItemRatingsService;
    private final MediaSeasonRatingsService mediaSeasonRatingsService;
    private final MediaItemsService mediaItemsService;
    private final MediaSeasonsService mediaSeasonsService;
    private final MediaItemCastRepository mediaItemCastRepository;
    private final MediaItemCrewRepository mediaItemCrewRepository;

    public RatingService(
            ProfilesService profilesService,
            MediaItemRatingsService mediaItemRatingsService,
            MediaSeasonRatingsService mediaSeasonRatingsService,
            MediaItemsService mediaItemsService,
            MediaSeasonsService mediaSeasonsService,
            MediaItemCastRepository mediaItemCastRepository,
            MediaItemCrewRepository mediaItemCrewRepository
    ) {
        this.profilesService = profilesService;
        this.mediaItemRatingsService = mediaItemRatingsService;
        this.mediaSeasonRatingsService = mediaSeasonRatingsService;
        this.mediaItemsService = mediaItemsService;
        this.mediaSeasonsService = mediaSeasonsService;
        this.mediaItemCastRepository = mediaItemCastRepository;
        this.mediaItemCrewRepository = mediaItemCrewRepository;
    }

    private void validateRating(
            Float rating
    ) {
        if (rating < 1.0f || rating > 10.0f) {
            throw new CommonExceptions.InvalidRequestExceptions("Rating must be between 1.0 and 10.0");
        }
        float decimalPart = rating % 1;
        if (decimalPart != 0.0f && decimalPart != 0.5f) {
            throw new CommonExceptions.InvalidRequestExceptions("Rating must have only .0 or .5 decimal places");
        }
    }

    public void rate(RateRequestDTO request) {
        logger.debug("Rating request received: {}", request);

        if (request.ratingMediaType() == null) {
            throw new CommonExceptions.InvalidRequestExceptions("Rating media type is required");
        }

        Float rating = request.rating();

        validateRating(rating);

        Profile profile = profilesService.findByIdOrThrow(
                request.profileId(),
                ProfilesExceptions.ProfileNotFoundExceptions::new
        );

        switch (request.ratingMediaType()) {
            case MEDIA_ITEM -> {
                MediaItem mediaItem = mediaItemsService.findByIdOrThrow(
                        request.entityId(),
                        MediaItemExceptions.MediaItemNotFoundExceptions::new
                );

                mediaItemRatingsService.upsert(profile, mediaItem, rating);
                logger.info("Rated media item '{}' ({}) {} for profile '{}' ({})",
                        mediaItem.title(), mediaItem.id(), rating, profile.name(), profile.id());
            }
            case MEDIA_SEASON -> {
                MediaSeason mediaSeason = mediaSeasonsService.findByIdOrThrow(
                        request.entityId(),
                        MediaSeasonExceptions.MediaSeasonNotFoundExceptions::new
                );

                mediaSeasonRatingsService.upsert(profile, mediaSeason, rating);
                logger.info("Rated season {} ({}) {} for profile '{}' ({})",
                        mediaSeason.season(), mediaSeason.id(), rating, profile.name(), profile.id());
            }
            default ->
                    throw new CommonExceptions.InvalidRequestExceptions("Unsupported rating media type: " + request.ratingMediaType());
        }
    }

    public void deleteRating(
            DeleteRateRequestDTO request
    ) {
        logger.debug("Delete rating request received: {}", request);

        if (request.ratingMediaType() == null) {
            throw new CommonExceptions.InvalidRequestExceptions("Rating media type is required");
        }

        Profile profile = profilesService.findByIdOrThrow(
                request.profileId(),
                ProfilesExceptions.ProfileNotFoundExceptions::new
        );

        switch (request.ratingMediaType()) {
            case MEDIA_ITEM -> {
                MediaItem mediaItem = mediaItemsService.findByIdOrThrow(
                        request.entityId(),
                        MediaItemExceptions.MediaItemNotFoundExceptions::new
                );

                MediaItemRating rating = mediaItemRatingsService.findByProfileAndMediaItem(profile, mediaItem)
                        .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Rating not found for media item ID: " + request.entityId()));

                mediaItemRatingsService.delete(rating.id());
                logger.info("Deleted rating for media item '{}' ({}) of profile '{}' ({})",
                        mediaItem.title(), mediaItem.id(), profile.name(), profile.id());
            }
            case MEDIA_SEASON -> {
                MediaSeason mediaSeason = mediaSeasonsService.findByIdOrThrow(
                        request.entityId(),
                        MediaSeasonExceptions.MediaSeasonNotFoundExceptions::new
                );

                MediaSeasonRating rating = mediaSeasonRatingsService.findByProfileAndMediaSeason(profile, mediaSeason)
                        .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Rating not found for media season ID: " + request.entityId()));

                mediaSeasonRatingsService.delete(rating.id());
                logger.info("Deleted rating for season {} ({}) of profile '{}' ({})",
                        mediaSeason.season(), mediaSeason.id(), profile.name(), profile.id());
            }
            default ->
                    throw new CommonExceptions.InvalidRequestExceptions("Unsupported rating media type: " + request.ratingMediaType());
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportProfileMediaRatingsCsv(Profile profile) {
        List<MediaItemRating> ratings = mediaItemRatingsService.findAllByProfile(profile);
        List<UUID> mediaItemIds = ratings.stream()
                .map(MediaItemRating::mediaItem)
                .map(MediaItem::id)
                .distinct()
                .toList();

        Map<UUID, List<MediaItemCast>> castByMediaItemId = mediaItemCastRepository.findByMediaItemIdIn(mediaItemIds).stream()
                .collect(Collectors.groupingBy(cast -> cast.mediaItem().id()));

        Map<UUID, List<MediaItemCrew>> crewByMediaItemId = mediaItemCrewRepository.findByMediaItemIdIn(mediaItemIds).stream()
                .collect(Collectors.groupingBy(crew -> crew.mediaItem().id()));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(
                             "title",
                             "media type",
                             "my rating",
                             "imdb id",
                             "tmdb id",
                             "top actors 1",
                             "top actors 2",
                             "top actors 3",
                             "top actors 4",
                             "top actors 5",
                             "director 1",
                             "director 2",
                             "director 3",
                             "producer 1",
                             "producer 2",
                             "producer 3"
                     )
                     .build())) {

            for (MediaItemRating rating : ratings) {
                MediaItem mediaItem = rating.mediaItem();

                List<String> topActors = resolveTopActors(castByMediaItemId.get(mediaItem.id()));
                List<String> topDirectors = resolveTopCrewByJob(crewByMediaItemId.get(mediaItem.id()), "director", 3);
                List<String> topProducers = resolveTopCrewByJob(crewByMediaItemId.get(mediaItem.id()), "producer", 3);

                csvPrinter.printRecord(
                        mediaItem.title(),
                        mediaItem.mediaType(),
                        rating.rating(),
                        mediaItem.IMDbId(),
                        mediaItem.TMDbId(),
                        topActors.get(0),
                        topActors.get(1),
                        topActors.get(2),
                        topActors.get(3),
                        topActors.get(4),
                        topDirectors.get(0),
                        topDirectors.get(1),
                        topDirectors.get(2),
                        topProducers.get(0),
                        topProducers.get(1),
                        topProducers.get(2)
                );
            }

            csvPrinter.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new CommonExceptions.InvalidRequestExceptions("Failed to export ratings CSV", e);
        }
    }

    private List<String> resolveTopActors(List<MediaItemCast> castList) {
        if (castList == null || castList.isEmpty()) {
            return fillToSize(List.of(), 5);
        }

        List<String> topActors = castList.stream()
                .sorted(Comparator.comparing(MediaItemCast::order, Comparator.nullsLast(Integer::compareTo)))
                .map(MediaItemCast::person)
                .map(person -> person != null ? person.name() : null)
                .filter(name -> name != null && !name.isBlank())
                .limit(5)
                .toList();

        return fillToSize(topActors, 5);
    }

    private List<String> resolveTopCrewByJob(List<MediaItemCrew> crewList, String job, int limit) {
        if (crewList == null || crewList.isEmpty()) {
            return fillToSize(List.of(), limit);
        }

        List<String> crew = crewList.stream()
                .filter(item -> item.job() != null && item.job().equalsIgnoreCase(job))
                .map(MediaItemCrew::person)
                .map(person -> person != null ? person.name() : null)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .limit(limit)
                .toList();

        return fillToSize(crew, limit);
    }

    private List<String> fillToSize(List<String> values, int size) {
        List<String> result = new ArrayList<>(values);
        while (result.size() < size) {
            result.add("");
        }
        return result;
    }
}
