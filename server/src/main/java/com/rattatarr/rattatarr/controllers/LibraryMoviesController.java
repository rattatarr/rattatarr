package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.BrokenMediaItemsFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.requests.MoviesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.BrokenMediaItemResponseWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.MoviesResponseWrapper;
import com.rattatarr.rattatarr.services.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/library/movies")
@ApiVersion("v1")
@NullMarked
public class LibraryMoviesController extends BaseController {
    private final MoviesService moviesService;
    private final BrokenMediaItemsService brokenMediaItemsService;
    private final RadarrService radarrService;
    private final ProfilesService profilesService;
    private final WatchedUnratedDismissalService watchedUnratedDismissalService;

    public LibraryMoviesController(MoviesService moviesService,
                                   BrokenMediaItemsService brokenMediaItemsService,
                                   RadarrService radarrService,
                                   ProfilesService profilesService,
                                   WatchedUnratedDismissalService watchedUnratedDismissalService
    ) {
        this.moviesService = moviesService;
        this.brokenMediaItemsService = brokenMediaItemsService;
        this.radarrService = radarrService;
        this.profilesService = profilesService;
        this.watchedUnratedDismissalService = watchedUnratedDismissalService;
    }

    @GetMapping
    public ResponseEntity<MoviesResponseWrapper> getMovies(
            @PageableDefault(size = 20, sort = "productionYear") Pageable pageable,
            @ModelAttribute MoviesFiltersDTO filters
    ) {
        logger.info("Fetching movies with filters: {}", filters);

        if (filters.id() != null) {
            radarrService.enrichMovieFromRadarrIfStale(filters.id());
        }

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

    @GetMapping("/watched/unrated")
    public ResponseEntity<MoviesResponseWrapper> getRecentlyWatchedUnratedMovies(
            @PageableDefault(size = 20) Pageable pageable,
            @ModelAttribute MoviesFiltersDTO filters
    ) {
        String profile = profilesService.describe(filters.profileId());
        logger.info(
                "Fetching recently watched unrated movies for profile {} (page {}, size {})",
                profile,
                pageable.getPageNumber(),
                pageable.getPageSize());

        var page = moviesService.findRecentlyWatchedUnratedMovies(filters, pageable);

        logger.debug(
                "Found {} recently watched unrated movies for profile {} ({} total)",
                page.getNumberOfElements(),
                profile,
                page.getTotalElements());

        return ResponseEntity.ok(MoviesResponseWrapper.fromPage(page));
    }

    @DeleteMapping("/watched/unrated/{mediaItemId}")
    public ResponseEntity<GenericResponseDTO> dismissWatchedUnrated(
            @PathVariable UUID mediaItemId,
            @RequestParam UUID profileId
    ) {
        logger.info("Dismissing movie {} from watched-unrated for profile {}",
                mediaItemId, profilesService.describe(profileId));

        watchedUnratedDismissalService.dismiss(profileId, mediaItemId);

        return ResponseEntity.ok(
                GenericResponseDTO.success("Movie removed from watched but not rated", null));
    }

    @PostMapping("/watched/unrated/{mediaItemId}/restore")
    public ResponseEntity<GenericResponseDTO> restoreWatchedUnrated(
            @PathVariable UUID mediaItemId,
            @RequestParam UUID profileId
    ) {
        logger.info("Restoring movie {} to watched-unrated for profile {}",
                mediaItemId, profilesService.describe(profileId));

        watchedUnratedDismissalService.restore(profileId, mediaItemId);

        return ResponseEntity.ok(
                GenericResponseDTO.success("Movie restored to watched but not rated", null));
    }
}
