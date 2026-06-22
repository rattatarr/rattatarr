package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.GenresFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.GenresWrapper;
import com.rattatarr.rattatarr.services.GenresService;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/library/genres")
@ApiVersion("v1")
@NullMarked
public class LibraryGenresController extends BaseController {
    private final GenresService genresService;

    public LibraryGenresController(GenresService genresService) {
        this.genresService = genresService;
    }

    @GetMapping
    public ResponseEntity<GenresWrapper> getGenres(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @ModelAttribute GenresFiltersDTO filters
    ) {
        logger.info("Fetching genres with filters {} ({})", filters, pageable);

        return ResponseEntity.ok(GenresWrapper.fromPage(
                genresService.filterGenres(filters, pageable)
        ));
    }

}
