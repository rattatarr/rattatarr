package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.specifications.MediaItemSpecifications;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@NullMarked
public class MediaItemsService extends BaseService<MediaItem, MediaItemsRepository> {

    public MediaItemsService(MediaItemsRepository repository) {
        super(repository);
    }

    @Override
    protected void updateEntity(MediaItem existing, MediaItem updated) {
        existing.setTitle(updated.title());
        if (updated.jellyfinId() != null) {
            existing.setJellyfinId(updated.jellyfinId());
        }
        if (updated.TMDbId() != null) {
            existing.setTMDbId(updated.TMDbId());
        }
        if (updated.IMDbId() != null) {
            existing.setIMDbId(updated.IMDbId());
        }
    }

    @Transactional(readOnly = true)
    public Optional<MediaItem> findByJellyfinId(String jellyfinId) {
        return repository.findByJellyfinId(jellyfinId);
    }

    @Transactional(readOnly = true)
    public Optional<MediaItem> findByTMDbId(String tmdbId) {
        return repository.findByTMDbId(tmdbId);
    }

    @Transactional(readOnly = true)
    public List<MediaItem> findAllWithGenres() {
        return repository.findAllWithGenres();
    }

    @Transactional(readOnly = true)
    public boolean isStale(MediaItem mediaItem, Duration threshold) {
        if (mediaItem.updatedAt() == null) {
            return true;
        }
        Instant staleThreshold = Instant.now().minus(threshold);
        return mediaItem.updatedAt().isBefore(staleThreshold);
    }

    @Transactional(readOnly = true)
    public List<MediaItem> findStaleSeries(Duration threshold) {
        Instant staleThreshold = Instant.now().minus(threshold);
        return repository.findAll(MediaItemSpecifications.staleSeries(staleThreshold));
    }
}
