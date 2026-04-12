package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.dtos.requests.SeriesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ShowResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import com.rattatarr.rattatarr.specifications.MediaItemSpecifications;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Hibernate;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NullMarked
public class SeriesService extends BaseService<MediaItem, MediaItemsRepository> {
    private final MediaItemViewHelper mediaItemViewHelper;
    private final MediaItemRefreshService mediaItemRefreshService;
    private final SettingsService settingsService;
    private final MediaItemRatingsService mediaItemRatingsService;

    public SeriesService(
            MediaItemsRepository repository,
            MediaItemViewHelper mediaItemViewHelper,
            MediaItemRefreshService mediaItemRefreshService,
            SettingsService settingsService,
            MediaItemRatingsService mediaItemRatingsService
    ) {
        super(repository);
        this.mediaItemViewHelper = mediaItemViewHelper;
        this.mediaItemRefreshService = mediaItemRefreshService;
        this.settingsService = settingsService;
        this.mediaItemRatingsService = mediaItemRatingsService;
    }

    @Override
    protected void updateEntity(MediaItem existing, MediaItem updated) {

    }

    @Transactional(readOnly = true)
    public Page<ShowResponseDTO> filterSeries(SeriesFiltersDTO filters, Pageable pageable) {
        boolean loadSeasons = BooleanUtils.toBoolean(filters.withSeasons()) || BooleanUtils.toBoolean(filters.withEpisodes());
        boolean loadEpisodes = BooleanUtils.toBoolean(filters.withEpisodes());
        boolean loadCredits = ObjectUtils.isNotEmpty(filters.id());

        // Check what is going on with pageable inside of this
        // Short: returns normal if not rating, if rating then returns an unsorted pageable to keep a custom order by for rating table.
        var ratingSort = MediaItemSpecifications.resolveRatingSort(pageable, filters.profileId());

        Specification<MediaItem> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                MediaItemSpecifications.hasId(filters.id()),
                MediaItemSpecifications.isSeries(),
                MediaItemSpecifications.genres(filters.genres(), true),
                MediaItemSpecifications.releasedAfter(filters.releasedAfter()),
                MediaItemSpecifications.releasedBefore(filters.releasedBefore()),
                MediaItemSpecifications.titleLike(filters.title()),
                MediaItemSpecifications.withMetadata(),
                MediaItemSpecifications.ratingInRange(filters.profileId(), filters.ratingMin(), filters.ratingMax()),
                MediaItemSpecifications.hasCastOrCrewMember(filters.personId()),
                MediaItemSpecifications.unrated(filters.profileId(), filters.unrated()),
                ratingSort.spec()
        );

        Page<MediaItem> page = repository.findAll(spec, ratingSort.pageable());

        // Auto-refresh stale series if enabled and fetching a single series.
        // Works for both TMDb-imported and Jellyfin-sourced series.
        boolean autoRefreshEnabled = settingsService.getBooleanSetting(
                SettingsService.SYNC_AUTO_REFRESH_ON_READ,
                false
        );

        if (autoRefreshEnabled && filters.id() != null && !page.isEmpty()) {
            logger.info("Auto-refresh is enabled. Checking if series ID {} is stale...", filters.id());
            MediaItem series = page.getContent().getFirst();

            // Refresh if stale (routes to TMDb or Jellyfin based on source)
            MediaItem refreshedSeries = mediaItemRefreshService.refreshIfStale(series);

            if (!refreshedSeries.equals(series)) {
                // Series was refreshed, update the page content
                List<MediaItem> updatedContent = Collections.singletonList(refreshedSeries);
                page = new PageImpl<>(updatedContent, ratingSort.pageable(), page.getTotalElements());
            }
        }

        page.forEach(mediaItem -> {
            if (loadSeasons) {
                Hibernate.initialize(mediaItem.seasons());
                mediaItem.seasons().forEach(season -> {
                    Hibernate.initialize(season.metadata());
                    Hibernate.initialize(season.ratings());
                    if (loadEpisodes) {
                        Hibernate.initialize(season.episodes());
                    }
                });
            }

            mediaItemViewHelper.initializeCredits(mediaItem, loadCredits);
            mediaItemViewHelper.applyImageUrls(mediaItem, filters.posterSize(), filters.backdropSize(), filters.profileSize(), loadSeasons, loadCredits);
        });

        List<UUID> mediaItemIds = page.getContent().stream()
                .map(MediaItem::id)
                .toList();
        Map<UUID, MediaItemRating> ratingsMap = mediaItemRatingsService.batchFetchRatingsMap(mediaItemIds, filters.profileId());

        List<ShowResponseDTO> series = page.getContent().stream()
                .map(item -> ShowResponseDTO.fromEntity(item, ratingsMap.get(item.id()), loadCredits))
                .toList();

        return new PageImpl<>(series, ratingSort.pageable(), page.getTotalElements());
    }
}
