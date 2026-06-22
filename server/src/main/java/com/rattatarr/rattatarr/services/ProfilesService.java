package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.ProfilesFiltersDTO;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import com.rattatarr.rattatarr.specifications.ProfileSpecifications;
import com.rattatarr.rattatarr.utils.TimeConverter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@NullMarked
public class ProfilesService extends BaseService<Profile, ProfilesRepository> {
    protected ProfilesService(ProfilesRepository repository) {
        super(repository);
    }

    @Override
    protected void validateBeforeSave(Profile entity) {
        Specification<Profile> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                ProfileSpecifications.nameLike(entity.name())
        );

        repository.findOne(spec).ifPresent(profile -> {
            throw new ProfilesExceptions.DuplicateProfileNameExceptions(entity.name());
        });
    }

    @Override
    protected void validateBeforeUpdate(Profile existing, Profile updated) {
        if (!existing.name().equalsIgnoreCase(updated.name())) {
            Specification<Profile> spec = Specification.allOf(
                    GenericSpecifications.notDeleted(),
                    ProfileSpecifications.nameLike(updated.name())
            );

            repository.findOne(spec).ifPresent(profile -> {
                throw new ProfilesExceptions.DuplicateProfileNameExceptions(updated.name());
            });
        }
    }

    @Override
    protected void updateEntity(Profile existing, Profile updated) {
        existing.setName(updated.name());
        existing.setJellyfinId(updated.jellyfinId());
    }

    @Transactional(readOnly = true)
    public Page<Profile> filterProfiles(ProfilesFiltersDTO filters, ZoneId zoneId, Pageable pageable) {
        logger.debug("Filtering profiles with specification: {} and zoneId: {}", filters, zoneId);

        Instant createdAtAfter, createdAtBefore,
                updatedAtAfter, updatedAtBefore;

        createdAtAfter = TimeConverter.toUTCInstant(filters.createdAtAfter(), zoneId);
        createdAtBefore = TimeConverter.toUTCInstant(filters.createdAtBefore(), zoneId);
        updatedAtAfter = TimeConverter.toUTCInstant(filters.updatedAtAfter(), zoneId);
        updatedAtBefore = TimeConverter.toUTCInstant(filters.updatedAtBefore(), zoneId);

        Specification<Profile> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                ProfileSpecifications.nameLike(filters.name()),
                GenericSpecifications.createdAtAfter(createdAtAfter),
                GenericSpecifications.createdAtBefore(createdAtBefore),
                GenericSpecifications.updatedAtAfter(updatedAtAfter),
                GenericSpecifications.updatedAtBefore(updatedAtBefore)
        );

        return repository.findAll(spec, pageable);
    }

    @Transactional
    public List<Profile> syncProfilesWithJellyfin(List<Profile> profiles) {
        logger.info("Syncing {} profiles with Jellyfin", profiles.size());

        List<Profile> syncedProfiles = new ArrayList<>(profiles.size());

        profiles.forEach(profile -> {
            Profile existing = repository.findByJellyfinId(profile.jellyfinId()).orElse(null);

            syncedProfiles.add(upsert(existing, profile));
        });

        return syncedProfiles;
    }

    @Transactional(readOnly = true)
    public Optional<Profile> findByJellyfinId(String jellyfinId) {
        return repository.findByJellyfinId(jellyfinId);
    }

    /**
     * Cheap, log-friendly label for a profile id: {@code 'Alex' (uuid)}.
     * Resolves the name via a single primary-key lookup; never throws.
     * Returns {@code 'unknown' (uuid)} when the id is null or not found,
     * so log statements stay safe and uniform.
     */
    @Transactional(readOnly = true)
    public String describe(@Nullable UUID profileId) {
        if (profileId == null) {
            return "'unknown' (null)";
        }
        return repository
                .findById(profileId)
                .map(profile -> "'%s' (%s)".formatted(profile.name(), profileId))
                .orElseGet(() -> "'unknown' (%s)".formatted(profileId));
    }
}
