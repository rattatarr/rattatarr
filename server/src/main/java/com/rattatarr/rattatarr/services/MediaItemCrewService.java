package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditCrewMemberResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemCrew;
import com.rattatarr.rattatarr.models.entities.Person;
import com.rattatarr.rattatarr.repositories.MediaItemCrewRepository;
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
public class MediaItemCrewService {

    private final MediaItemCrewRepository repository;

    public MediaItemCrewService(MediaItemCrewRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MediaItemCrew upsert(
            MediaItem mediaItem,
            TMDbCreditCrewMemberResponseDTO crewMember,
            Person person,
            Boolean forceRefresh
    ) {
        return repository.findByMediaItemAndPersonAndJobAndDepartment(
                        mediaItem,
                        person,
                        crewMember.job(),
                        crewMember.department()
                )
                .map(existingCrew -> {
                    if (!forceRefresh) return existingCrew;

                    // Update any fields if necessary in the future

                    return repository.save(existingCrew);
                })
                .orElseGet(() -> {
                    MediaItemCrew newCrew = new MediaItemCrew(
                            mediaItem,
                            person,
                            crewMember.job(),
                            crewMember.department()
                    );
                    return repository.save(newCrew);
                });
    }

    @Transactional
    public void upsertBatchFromTMDb(
            List<TMDbCreditCrewMemberResponseDTO> crewMembers,
            MediaItem mediaItem,
            Map<String, Person> peopleMap
    ) {
        if (crewMembers.isEmpty()) {
            return;
        }

        List<MediaItemCrew> existingCrewList = repository.findByMediaItemId(mediaItem.id());

        Map<String, MediaItemCrew> existingCrewMap = existingCrewList.stream()
                .collect(Collectors.toMap(
                        crew -> crew.person().id() + ":" + crew.job() + ":" + crew.department(),
                        Function.identity()
                ));

        List<MediaItemCrew> toSave = new ArrayList<>();

        crewMembers.forEach(crewMember -> {
            Person person = peopleMap.get(crewMember.personTMDbId());
            if (person != null) {
                String key = person.id() + ":" + crewMember.job() + ":" + crewMember.department();
                MediaItemCrew existingCrew = existingCrewMap.get(key);

                if (existingCrew == null) {
                    MediaItemCrew newCrew = new MediaItemCrew(
                            mediaItem,
                            person,
                            crewMember.job(),
                            crewMember.department()
                    );
                    toSave.add(newCrew);
                }
            }
        });

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
        }
    }
}
