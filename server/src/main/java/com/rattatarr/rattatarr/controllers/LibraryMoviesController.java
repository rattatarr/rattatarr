package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.BrokenMediaItemsFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.requests.MoviesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.BrokenMediaItemResponseWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.MoviesResponseWrapper;
import com.rattatarr.rattatarr.services.BrokenMediaItemsService;
import com.rattatarr.rattatarr.services.MoviesService;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/library/movies")
@ApiVersion("v1")
@NullMarked
public class LibraryMoviesController extends BaseController {
    private final MoviesService moviesService;
    private final BrokenMediaItemsService brokenMediaItemsService;

    public LibraryMoviesController(MoviesService moviesService,
                                   BrokenMediaItemsService brokenMediaItemsService
    ) {
        this.moviesService = moviesService;
        this.brokenMediaItemsService = brokenMediaItemsService;
    }

    @GetMapping
    public ResponseEntity<MoviesResponseWrapper> getMovies(
            @PageableDefault(size = 20, sort = "productionYear") Pageable pageable,
            @ModelAttribute MoviesFiltersDTO filters
    ) {
        logger.info("Fetching movies with filters: {}", filters);

        return ResponseEntity.ok(MoviesResponseWrapper.fromPage(
                moviesService.filterMovies(filters, pageable)
        ));
    }

    @GetMapping("/broken")
    public ResponseEntity<BrokenMediaItemResponseWrapper> getBrokenMovies(
            @PageableDefault(size = 20, sort = "productionYear") Pageable pageable
    ) {
        logger.info("Fetching broken movies");

        return ResponseEntity.ok(BrokenMediaItemResponseWrapper.fromPage(
                brokenMediaItemsService.filterBrokenMediaItems(
                        new BrokenMediaItemsFiltersDTO(
                                MediaType.MOVIE
                        ),
                        pageable
                )
        ));
    }
}
