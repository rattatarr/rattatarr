package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.WatchActivityFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.WatchActivityWrapper;
import com.rattatarr.rattatarr.services.WatchActivityService;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/watch-activity")
@ApiVersion("v1")
@NullMarked
public class WatchActivityController extends BaseController {

    private final WatchActivityService watchActivityService;

    public WatchActivityController(WatchActivityService watchActivityService) {
        this.watchActivityService = watchActivityService;
    }

    @GetMapping
    public ResponseEntity<WatchActivityWrapper> getActivity(
            @ModelAttribute WatchActivityFiltersDTO filters,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        logger.info("Fetching watch activity with filters: {}", filters);
        return ResponseEntity.ok(WatchActivityWrapper.fromPage(
                watchActivityService.filterActivity(filters, pageable)
        ));
    }
}
