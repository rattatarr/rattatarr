package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.BrokenMediaItemsExceptions;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.exceptions.MediaItemExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.BrokenMediaItemsFiltersDTO;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.repositories.BrokenMediaItemsRepository;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.specifications.BrokenMediaItemSpecifications;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@NullMarked
public class BrokenMediaItemsService extends BaseService<BrokenMediaItem, BrokenMediaItemsRepository> {
    private final MediaItemsRepository mediaItemsRepository;

    public BrokenMediaItemsService(BrokenMediaItemsRepository repository, MediaItemsRepository mediaItemsRepository) {
        super(repository);
        this.mediaItemsRepository = mediaItemsRepository;
    }

    @Override
    protected void updateEntity(BrokenMediaItem existing, BrokenMediaItem updated) {

    }

    @Transactional(readOnly = true)
    public Optional<BrokenMediaItem> findByJellyfinId(String jellyfinId) {
        return repository.findByJellyfinId(jellyfinId);
    }

    @Transactional
    public void saveBrokenMediaItem(BrokenMediaItem brokenMediaItem) {
        logger.info("Saving BrokenMediaItem with Jellyfin ID {} to the database...", brokenMediaItem.jellyfinId());
        repository.save(brokenMediaItem);
    }

    @Transactional
    public BrokenMediaItem resolveById(UUID id, UUID mediaItemId) {
        if (mediaItemId == null) {
            throw new CommonExceptions.InvalidRequestExceptions("mediaItemId must not be null");
        }
        BrokenMediaItem item = findByIdOrThrow(id, BrokenMediaItemsExceptions.BrokenMediaItemNotFoundExceptions::new);
        MediaItem mediaItem = mediaItemsRepository
                .findById(mediaItemId)
                .orElseThrow(() -> new MediaItemExceptions.MediaItemNotFoundExceptions(mediaItemId));
        logger.info("Resolving BrokenMediaItem {} by linking to MediaItem {}", id, mediaItemId);
        item.setResolvedMediaItem(mediaItem);
        item.setResolved(true);
        return repository.save(item);
    }

    @Transactional(readOnly = true)
    public Page<BrokenMediaItem> filterBrokenMediaItems(BrokenMediaItemsFiltersDTO filters, Pageable pageable) {
        Specification<BrokenMediaItem> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                BrokenMediaItemSpecifications.notResolved(),
                (filters.mediaType().equals(MediaType.MOVIE) ?
                        BrokenMediaItemSpecifications.isMovie() :
                        BrokenMediaItemSpecifications.isSeries())
        );

        return repository.findAll(spec, pageable);
    }

    @Transactional
    public List<BrokenMediaItem> seedTestData(int count) {
        int clamped = Math.min(Math.max(count, 1), 20);
        Random random = new Random();
        String[] titles = {
                "The Lost Signal", "Dark Horizon", "Crimson Tide Rising", "Beyond the Veil",
                "Shattered Orbit", "The Iron Mask", "Last Light", "Fracture Point",
                "Empty Vessel", "The Forgotten Coast", "Silent Breach", "Under the Wire",
                "Pale Morning", "Edge of Reason", "The Narrow Road", "Storm Protocol",
                "Cold Archive", "The Sunken City", "Broken Compass", "Last Transmission"
        };
        String[] missingFieldOptions = {"TMDbId", "IMDbId", "TMDbId,IMDbId", "productionYear"};
        MediaType[] types = MediaType.values();

        List<BrokenMediaItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < clamped; i++) {
            String title = titles[random.nextInt(titles.length)] + " " + (random.nextInt(900) + 100);
            MediaType mediaType = types[random.nextInt(types.length)];
            String jellyfinId = "jf-seed-" + UUID.randomUUID().toString().substring(0, 8);
            int productionYear = 1990 + random.nextInt(36);
            String missingFields = missingFieldOptions[random.nextInt(missingFieldOptions.length)];

            BrokenMediaItem item = new BrokenMediaItem(mediaType, title, jellyfinId, null, null, productionYear, missingFields);
            items.add(repository.save(item));
        }
        logger.info("Seeded {} test BrokenMediaItem rows", clamped);
        return items;
    }
}
