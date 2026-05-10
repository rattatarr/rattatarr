package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.dtos.requests.MoviesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.MovieResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import com.rattatarr.rattatarr.specifications.MediaItemSpecifications;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NullMarked
public class MoviesService extends BaseService<MediaItem, MediaItemsRepository> {
    private final MediaItemViewHelper mediaItemViewHelper;
    private final MediaItemRatingsService mediaItemRatingsService;

    public MoviesService(
            MediaItemsRepository repository,
            MediaItemViewHelper mediaItemViewHelper,
            MediaItemRatingsService mediaItemRatingsService
    ) {
        super(repository);
        this.mediaItemViewHelper = mediaItemViewHelper;
        this.mediaItemRatingsService = mediaItemRatingsService;
    }

    @Override
    protected void updateEntity(MediaItem existing, MediaItem updated) {

    }

    @Transactional(readOnly = true)
    public Page<MovieResponseDTO> filterMovies(MoviesFiltersDTO filters, Pageable pageable) {
        boolean loadCredits = ObjectUtils.isNotEmpty(filters.id());

        var ratingSort = MediaItemSpecifications.resolveRatingSort(pageable, filters.profileId());
        var lastWatchedSort = MediaItemSpecifications.resolveLastWatchedSort(ratingSort.pageable(), filters.profileId());

        Specification<MediaItem> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                MediaItemSpecifications.hasId(filters.id()),
                MediaItemSpecifications.isMovie(),
                MediaItemSpecifications.genres(filters.genres(), true),
                MediaItemSpecifications.releasedAfter(filters.releasedAfter()),
                MediaItemSpecifications.releasedBefore(filters.releasedBefore()),
                MediaItemSpecifications.titleLike(filters.title()),
                MediaItemSpecifications.withMetadata(),
                MediaItemSpecifications.ratingInRange(filters.profileId(), filters.ratingMin(), filters.ratingMax()),
                MediaItemSpecifications.hasCastOrCrewMember(filters.personId()),
                MediaItemSpecifications.unrated(filters.profileId(), filters.unrated()),
                ratingSort.spec(),
                lastWatchedSort.spec()
        );

        Page<MediaItem> page = repository.findAll(spec, lastWatchedSort.pageable());

        page.forEach(mediaItem -> {
            mediaItemViewHelper.initializeCredits(mediaItem, loadCredits);
            mediaItemViewHelper.applyImageUrls(mediaItem, filters.posterSize(), filters.backdropSize(), filters.profileSize(), false, loadCredits);
        });

        List<UUID> mediaItemIds = page.getContent().stream()
                .map(MediaItem::id)
                .toList();
        Map<UUID, MediaItemRating> ratingsMap = mediaItemRatingsService.batchFetchRatingsMap(mediaItemIds, filters.profileId());

        List<MovieResponseDTO> movies = page.getContent().stream()
                .map(item -> MovieResponseDTO.fromEntity(item, ratingsMap.get(item.id()), loadCredits))
                .toList();

        return new PageImpl<>(movies, lastWatchedSort.pageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<MovieResponseDTO> findRecentlyWatchedUnratedMovies(MoviesFiltersDTO filters, Pageable pageable) {
        UUID profileId = filters.profileId();
        Specification<MediaItem> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                MediaItemSpecifications.isMovie(),
                MediaItemSpecifications.withMetadata(),
                MediaItemSpecifications.recentlyWatchedUnrated(profileId)
        );

        Page<MediaItem> page = repository.findAll(spec, pageable);

        page.forEach(mediaItem -> mediaItemViewHelper.applyImageUrls(
                mediaItem,
                filters.posterSize(),
                filters.backdropSize(),
                filters.profileSize(),
                false,
                false
        ));

        List<MovieResponseDTO> movies = page.getContent().stream()
                .map(item -> MovieResponseDTO.fromEntity(item, false))
                .toList();

        return new PageImpl<>(movies, pageable, page.getTotalElements());
    }
}
