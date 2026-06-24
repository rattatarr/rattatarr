package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.MediaItemExceptions;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.models.entities.WatchedUnratedDismissal;
import com.rattatarr.rattatarr.repositories.WatchedUnratedDismissalsRepository;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@NullMarked
public class WatchedUnratedDismissalService {
    private static final Logger logger = LoggerFactory.getLogger(WatchedUnratedDismissalService.class);

    private final WatchedUnratedDismissalsRepository repository;
    private final ProfilesService profilesService;
    private final MediaItemsService mediaItemsService;

    public WatchedUnratedDismissalService(WatchedUnratedDismissalsRepository repository,
                                          ProfilesService profilesService,
                                          MediaItemsService mediaItemsService) {
        this.repository = repository;
        this.profilesService = profilesService;
        this.mediaItemsService = mediaItemsService;
    }

    @Transactional
    public void dismiss(UUID profileId, UUID mediaItemId) {
        Profile profile = profilesService.findByIdOrThrow(
                profileId, ProfilesExceptions.ProfileNotFoundExceptions::new);
        MediaItem mediaItem = mediaItemsService.findByIdOrThrow(
                mediaItemId, MediaItemExceptions.MediaItemNotFoundExceptions::new);

        Instant now = Instant.now();
        WatchedUnratedDismissal dismissal = repository
                .findByProfileIdAndMediaItemId(profileId, mediaItemId)
                .orElseGet(() -> new WatchedUnratedDismissal(profile, mediaItem, now));
        dismissal.setDismissedAt(now);
        repository.save(dismissal);

        logger.info("Dismissed media item '{}' ({}) from watched-unrated for profile '{}' ({})",
                mediaItem.title(), mediaItem.id(), profile.name(), profile.id());
    }

    @Transactional
    public void restore(UUID profileId, UUID mediaItemId) {
        Profile profile = profilesService.findByIdOrThrow(
                profileId, ProfilesExceptions.ProfileNotFoundExceptions::new);
        MediaItem mediaItem = mediaItemsService.findByIdOrThrow(
                mediaItemId, MediaItemExceptions.MediaItemNotFoundExceptions::new);

        repository.findByProfileIdAndMediaItemId(profileId, mediaItemId)
                .ifPresentOrElse(
                        dismissal -> {
                            repository.delete(dismissal);
                            logger.info("Restored media item '{}' ({}) to watched-unrated for profile '{}' ({})",
                                    mediaItem.title(), mediaItem.id(), profile.name(), profile.id());
                        },
                        () -> logger.debug("No dismissal to restore for media item '{}' ({}) profile '{}' ({})",
                                mediaItem.title(), mediaItem.id(), profile.name(), profile.id()));
    }
}
