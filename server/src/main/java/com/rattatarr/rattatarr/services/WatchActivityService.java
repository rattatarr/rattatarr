package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.dtos.requests.WatchActivityFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.WatchEventResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import com.rattatarr.rattatarr.models.entities.WatchEvent;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.repositories.WatchEventsRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import com.rattatarr.rattatarr.specifications.MediaItemSpecifications;
import com.rattatarr.rattatarr.specifications.WatchEventSpecifications;
import com.rattatarr.rattatarr.utils.TimeConverter;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NullMarked
public class WatchActivityService extends BaseService<WatchEvent, WatchEventsRepository> {

    private final MediaItemsRepository mediaItemsRepository;
    private final MediaItemRatingsService mediaItemRatingsService;
    private final MediaItemViewHelper mediaItemViewHelper;

    public WatchActivityService(WatchEventsRepository repository,
                                MediaItemsRepository mediaItemsRepository,
                                MediaItemRatingsService mediaItemRatingsService,
                                MediaItemViewHelper mediaItemViewHelper) {
        super(repository);
        this.mediaItemsRepository = mediaItemsRepository;
        this.mediaItemRatingsService = mediaItemRatingsService;
        this.mediaItemViewHelper = mediaItemViewHelper;
    }

    @Transactional(readOnly = true)
    public Page<WatchEventResponseDTO> filterActivity(WatchActivityFiltersDTO filters, Pageable pageable) {
        Specification<WatchEvent> spec = Specification.allOf(
                WatchEventSpecifications.forProfile(filters.profileId()),
                WatchEventSpecifications.excludeEventType(WatchEventType.START),
                WatchEventSpecifications.deduplicated(),
                WatchEventSpecifications.afterOrAt(TimeConverter.parseToInstant(filters.startDate())),
                WatchEventSpecifications.before(TimeConverter.parseToInstantEndOfDay(filters.endDate())),
                WatchEventSpecifications.forMediaType(filters.mediaType())
        );

        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "watchedAt"));
        Page<WatchEvent> events = repository.findAll(spec, sorted);

        List<UUID> mediaItemIds = events.getContent().stream()
                .map(e -> e.mediaItem().id())
                .distinct()
                .toList();

        List<MediaItem> mediaItems = mediaItemsRepository.findAll(
                Specification.allOf(
                        GenericSpecifications.notDeleted(),
                        MediaItemSpecifications.withMetadata(),
                        MediaItemSpecifications.hasAnyId(mediaItemIds)
                )
        );
        mediaItems.forEach(item -> mediaItemViewHelper.applyImageUrls(item, filters.posterSize(), filters.backdropSize(), null, false, false));
        Map<UUID, MediaItem> mediaItemsMap = mediaItems.stream().collect(Collectors.toMap(MediaItem::id, Function.identity()));

        Map<UUID, MediaItemRating> ratingsMap = mediaItemRatingsService.batchFetchRatingsMap(mediaItemIds, filters.profileId());

        List<WatchEventResponseDTO> dtos = events.getContent().stream()
                .map(event -> {
                    MediaItem mediaItem = mediaItemsMap.getOrDefault(event.mediaItem().id(), event.mediaItem());
                    return WatchEventResponseDTO.from(event, mediaItem, ratingsMap.get(mediaItem.id()));
                })
                .toList();

        return new PageImpl<>(dtos, pageable, events.getTotalElements());
    }

    @Override
    protected void updateEntity(WatchEvent entity, WatchEvent update) {
        throw new UnsupportedOperationException();
    }
}
