package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditCastMemberResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemCast;
import com.rattatarr.rattatarr.models.entities.Person;
import com.rattatarr.rattatarr.repositories.MediaItemCastRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NullMarked
public class MediaItemCastService {

    private final MediaItemCastRepository repository;

    public MediaItemCastService(MediaItemCastRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MediaItemCast upsert(
            MediaItem mediaItem,
            TMDbCreditCastMemberResponseDTO castMember,
            Person person,
            Boolean forceRefresh
    ) {
        return repository.findByMediaItemAndPersonAndCharacter(mediaItem, person, castMember.character())
                .map(existingCast -> {
                    if (!forceRefresh) return existingCast;

                    existingCast.setOrder(castMember.order());

                    return repository.save(existingCast);
                })
                .orElseGet(() -> {
                    MediaItemCast newCast = new MediaItemCast(
                            mediaItem,
                            person,
                            castMember.character(),
                            castMember.order()
                    );
                    return repository.save(newCast);
                });
    }

    @Transactional
    public void upsertBatchFromTMDb(
            List<TMDbCreditCastMemberResponseDTO> castMembers,
            MediaItem mediaItem,
            Map<String, Person> peopleMap
    ) {
        if (castMembers.isEmpty()) {
            return;
        }

        List<MediaItemCast> existingCastList = repository.findByMediaItemId(mediaItem.id());

        Map<String, MediaItemCast> existingCastMap = existingCastList.stream()
                .collect(Collectors.toMap(
                        cast -> cast.person().id() + ":" + cast.character(),
                        Function.identity()
                ));

        List<MediaItemCast> toSave = new ArrayList<>();

        castMembers.forEach(castMember -> {
            Person person = peopleMap.get(castMember.personTMDbId());
            if (person != null) {
                String key = person.id() + ":" + castMember.character();
                MediaItemCast existingCast = existingCastMap.get(key);

                if (existingCast != null) {
                    if (!castMember.order().equals(existingCast.order())) {
                        existingCast.setOrder(castMember.order());
                        toSave.add(existingCast);
                    }
                } else {
                    MediaItemCast newCast = new MediaItemCast(
                            mediaItem,
                            person,
                            castMember.character(),
                            castMember.order()
                    );
                    toSave.add(newCast);
                }
            }
        });

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
        }
    }
}
