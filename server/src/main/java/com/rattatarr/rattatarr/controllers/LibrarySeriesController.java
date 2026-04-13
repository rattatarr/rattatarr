package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.BrokenMediaItemsFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.requests.SeriesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.BrokenMediaItemResponseWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SeriesResponseWrapper;
import com.rattatarr.rattatarr.services.BrokenMediaItemsService;
import com.rattatarr.rattatarr.services.MediaItemRefreshService;
import com.rattatarr.rattatarr.services.MediaItemsService;
import com.rattatarr.rattatarr.services.SeriesService;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/library/series")
@ApiVersion("v1")
@NullMarked
public class LibrarySeriesController extends BaseController {
    private final SeriesService seriesService;
    private final BrokenMediaItemsService brokenMediaItemsService;
    private final MediaItemsService mediaItemsService;
    private final MediaItemRefreshService mediaItemRefreshService;

    public LibrarySeriesController(SeriesService seriesService,
                                   BrokenMediaItemsService brokenMediaItemsService,
                                   MediaItemsService mediaItemsService,
                                   MediaItemRefreshService mediaItemRefreshService
    ) {
        this.seriesService = seriesService;
        this.brokenMediaItemsService = brokenMediaItemsService;
        this.mediaItemsService = mediaItemsService;
        this.mediaItemRefreshService = mediaItemRefreshService;
    }

    @GetMapping
    public ResponseEntity<SeriesResponseWrapper> getSeries(
            @PageableDefault(size = 20, sort = "productionYear") Pageable pageable,
            @ModelAttribute SeriesFiltersDTO filters
    ) {
        logger.info("Fetching series with filters: {}", pageable);

        return ResponseEntity.ok(SeriesResponseWrapper.fromPage(
                seriesService.filterSeries(filters, pageable)
        ));
    }

    @GetMapping("/broken")
    public ResponseEntity<BrokenMediaItemResponseWrapper> getBrokenSeries(
            @PageableDefault(size = 20, sort = "productionYear") Pageable pageable
    ) {
        logger.info("Fetching broken series");

        return ResponseEntity.ok(BrokenMediaItemResponseWrapper.fromPage(
                brokenMediaItemsService.filterBrokenMediaItems(
                        new BrokenMediaItemsFiltersDTO(
                                MediaType.SERIES
                        ),
                        pageable
                )
        ));
    }

    @GetMapping("/watched/unrated")
    public ResponseEntity<SeriesResponseWrapper> getRecentlyWatchedUnratedSeries(
            @PageableDefault(size = 20) Pageable pageable,
            @ModelAttribute SeriesFiltersDTO filters
    ) {
        logger.info("Fetching recently watched unrated series for profile {}", filters.profileId());

        return ResponseEntity.ok(SeriesResponseWrapper.fromPage(
                seriesService.findRecentlyWatchedUnratedSeries(filters, pageable)
        ));
    }

    /**
     * Manual refresh of a specific series
     * Decides automatically if the refresh is done via TMDb or Jellyfin
     */
    @PutMapping("/{id}/refresh")
    public GenericResponseDTO refreshMediaItem(@PathVariable UUID id) {
        logger.info("Manual refresh triggered for media item {}", id);

        try {
            var mediaItem = mediaItemsService.findById(id)
                    .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Media item not found: " + id));
            var refreshed = mediaItemRefreshService.refresh(mediaItem);
            return GenericResponseDTO.success(
                    "Media item '" + refreshed.title() + "' refreshed successfully", null);
        } catch (Exception e) {
            logger.error("Failed to refresh media item {}", id, e);
            return GenericResponseDTO.failure("Refresh failed: " + e.getMessage());
        }
    }

    /**
     * Batch refresh of all stale series
     * Decides automatically if the refresh is done via TMDb or Jellyfin
     */
    @PutMapping("/refresh")
    public GenericResponseDTO refreshAllStaleItems() {
        logger.info("Manual refresh of all stale series triggered via API");

        mediaItemRefreshService.refreshAllStaleSeriesAsync();
        return GenericResponseDTO.accepted(
                "Stale series refresh started in background", null);
    }
}
