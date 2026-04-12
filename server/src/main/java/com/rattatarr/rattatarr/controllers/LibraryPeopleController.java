package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.PeopleFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PersonResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.PeopleWrapper;
import com.rattatarr.rattatarr.services.PeopleService;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/library/people")
@ApiVersion("v1")
@NullMarked
public class LibraryPeopleController extends BaseController {
    private final PeopleService peopleService;

    public LibraryPeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping
    public ResponseEntity<PeopleWrapper> getPeople(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @ModelAttribute PeopleFiltersDTO filters
    ) {
        logger.info("Fetching people with filters: {}", pageable);

        return ResponseEntity.ok(PeopleWrapper.fromPage(
                peopleService.filterPeople(filters, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> getPersonById(
            @PathVariable String id,
            @RequestParam(defaultValue = "w185") String profileSize
    ) {
        logger.info("Fetching person with id: {}", id);

        return ResponseEntity.ok(peopleService.getPersonById(id, profileSize));
    }
}
