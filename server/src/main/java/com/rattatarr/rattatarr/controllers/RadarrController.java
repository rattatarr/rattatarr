package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieLookupResponseDTO;
import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import com.rattatarr.rattatarr.services.RadarrService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/test")
    public GenericResponseDTO testConnection() {
        logger.info("Testing Radarr connection");
        boolean success = radarrService.testConnection();
        if (!success)
            return GenericResponseDTO.failure("Radarr connection failed");
        return GenericResponseDTO.success("Radarr connection successful", null);
    }

    @GetMapping("/movies")
    public ResponseEntity<RadarrMovieLookupResponseDTO> lookupMovieByTmdbId(@RequestParam int tmdbId) {
        logger.info("Looking up Radarr movie by TMDb ID: {}", tmdbId);
        return ResponseEntity.ok(radarrService.lookupByTmdbId(tmdbId));
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO importMovies() {
        logger.info("Starting Radarr movie import");
        var job = backgroundJobService.create(JobType.RADARR_IMPORT, null);
        radarrService.triggerBackgroundImport(job);
        return BackgroundJobResponseDTO.fromEntity(job);
    }

    @PostMapping("/refresh-ratings")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO refreshRatings() {
        logger.info("Starting Radarr ratings refresh");
        var job = backgroundJobService.create(JobType.RADARR_RATINGS_REFRESH, null);
        radarrService.triggerBackgroundRatingsRefresh(job);
        return BackgroundJobResponseDTO.fromEntity(job);
    }
}
