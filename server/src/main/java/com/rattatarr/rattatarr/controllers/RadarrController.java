package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.RadarrMoviesWrapper;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import com.rattatarr.rattatarr.services.RadarrService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/radarr")
@ApiVersion("v1")
@NullMarked
public class RadarrController extends BaseController {
    private final RadarrService radarrService;
    private final BackgroundJobService backgroundJobService;

    public RadarrController(RadarrService radarrService, BackgroundJobService backgroundJobService) {
        this.radarrService = radarrService;
        this.backgroundJobService = backgroundJobService;
    }

    @GetMapping("/movies")
    public RadarrMoviesWrapper getMovies(@RequestParam(required = false) @Nullable Integer tmdbId) {
        logger.info("Fetching movies tracked by Radarr, tmdbId={}", tmdbId);
        return RadarrMoviesWrapper.fromList(radarrService.getTrackedMovies(tmdbId));
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO importMovies() {
        logger.info("Starting Radarr movie import");
        var job = backgroundJobService.create(JobType.RADARR_IMPORT, null);
        radarrService.triggerBackgroundImport(job);
        return BackgroundJobResponseDTO.fromEntity(job);
    }
}
