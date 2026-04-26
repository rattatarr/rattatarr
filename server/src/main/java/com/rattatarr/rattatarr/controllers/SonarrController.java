package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
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
    public GenericResponseDTO testConnection() {
        logger.info("Testing Sonarr connection");
        boolean success = sonarrService.testConnection();
        if (!success)
            return GenericResponseDTO.failure("Sonarr connection failed");
        return GenericResponseDTO.success("Sonarr connection successful", null);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO importSeries() {
        logger.info("Starting Sonarr series import");
        var job = backgroundJobService.create(JobType.SONARR_IMPORT, null);
        sonarrService.triggerBackgroundImport(job);
        return BackgroundJobResponseDTO.fromEntity(job);
    }
}
