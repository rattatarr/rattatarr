package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import com.rattatarr.rattatarr.services.SonarrService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sonarr")
@ApiVersion("v1")
@NullMarked
public class SonarrController extends BaseController {
    private final SonarrService sonarrService;
    private final BackgroundJobService backgroundJobService;

    public SonarrController(SonarrService sonarrService, BackgroundJobService backgroundJobService) {
        this.sonarrService = sonarrService;
        this.backgroundJobService = backgroundJobService;
    }

    @GetMapping("/test")
    public GenericResponseDTO testConnection(
            @RequestParam(defaultValue = "DEFAULT") ArrInstance instance) {
        logger.info("Testing Sonarr ({}) connection", instance);
        boolean success = sonarrService.testConnection(instance);
        if (!success)
            return GenericResponseDTO.failure("Sonarr connection failed");
        return GenericResponseDTO.success("Sonarr connection successful", null);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO importSeries(
            @RequestParam(defaultValue = "DEFAULT") ArrInstance instance) {
        logger.info("Starting Sonarr ({}) series import", instance);
        var jobType = instance == ArrInstance.ANIME ? JobType.SONARR_ANIME_IMPORT : JobType.SONARR_IMPORT;
        var job = backgroundJobService.create(jobType, null);
        sonarrService.triggerBackgroundImport(job, instance);
        return BackgroundJobResponseDTO.fromEntity(job);
    }
}
