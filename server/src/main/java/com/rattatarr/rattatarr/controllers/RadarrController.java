package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieLookupResponseDTO;
import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.ArrInstance;
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
    public GenericResponseDTO testConnection(
            @RequestParam(defaultValue = "DEFAULT") ArrInstance instance) {
        logger.info("Testing Radarr ({}) connection", instance);
        boolean success = radarrService.testConnection(instance);
        if (!success)
            return GenericResponseDTO.failure("Radarr connection failed");
        return GenericResponseDTO.success("Radarr connection successful", null);
    }

    @GetMapping("/movies")
    public ResponseEntity<RadarrMovieLookupResponseDTO> lookupMovieByTmdbId(
            @RequestParam int tmdbId,
            @RequestParam(defaultValue = "DEFAULT") ArrInstance instance) {
        logger.info("Looking up Radarr ({}) movie by TMDb ID: {}", instance, tmdbId);
        return ResponseEntity.ok(radarrService.lookupByTmdbId(tmdbId, instance));
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO importMovies(
            @RequestParam(defaultValue = "DEFAULT") ArrInstance instance) {
        logger.info("Starting Radarr ({}) movie import", instance);
        var jobType = instance == ArrInstance.ANIME ? JobType.RADARR_ANIME_IMPORT : JobType.RADARR_IMPORT;
        var job = backgroundJobService.create(jobType, null);
        radarrService.triggerBackgroundImport(job, instance);
        return BackgroundJobResponseDTO.fromEntity(job);
    }

    @PostMapping("/refresh-ratings")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO refreshRatings(
            @RequestParam(defaultValue = "DEFAULT") ArrInstance instance) {
        logger.info("Starting Radarr ({}) ratings refresh", instance);
        var jobType = instance == ArrInstance.ANIME ? JobType.RADARR_ANIME_RATINGS_REFRESH : JobType.RADARR_RATINGS_REFRESH;
        var job = backgroundJobService.create(jobType, null);
        radarrService.triggerBackgroundRatingsRefresh(job, instance);
        return BackgroundJobResponseDTO.fromEntity(job);
    }
}
