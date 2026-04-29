package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ProfileResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfilesWrapper;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import com.rattatarr.rattatarr.services.JellyfinService;
import com.rattatarr.rattatarr.services.JellyfinTraversalService;
import com.rattatarr.rattatarr.services.schedulers.JellyfinActivityPollingService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;

@RestController
@RequestMapping("/jellyfin")
@ApiVersion("v1")
@NullMarked
public class JellyfinController extends BaseController {
    private final JellyfinService jellyfinService;
    private final JellyfinTraversalService jellyfinTraversalService;
    private final JellyfinActivityPollingService jellyfinActivityPollingService;
    private final BackgroundJobService backgroundJobService;

    public JellyfinController(
            JellyfinService jellyfinService,
            JellyfinTraversalService jellyfinTraversalService,
            JellyfinActivityPollingService jellyfinActivityPollingService,
            BackgroundJobService backgroundJobService
    ) {
        this.jellyfinService = jellyfinService;
        this.jellyfinTraversalService = jellyfinTraversalService;
        this.jellyfinActivityPollingService = jellyfinActivityPollingService;
        this.backgroundJobService = backgroundJobService;
    }

    @GetMapping("/test")
    public GenericResponseDTO testConnection() {
        logger.info("Testing Jellyfin connection");

        boolean success = jellyfinService.testConnection();
        if (!success)
            return GenericResponseDTO.failure("Jellyfin connection failed");

        return GenericResponseDTO.success("Jellyfin connection successful", null);
    }

    @PutMapping("/sync-profiles")
    public ProfilesWrapper syncProfiles(
            @ModelAttribute("zoneId") ZoneId zoneId
    ) {
        logger.info("Syncing Jellyfin users with Rattatarr profiles");
        var profileList = jellyfinService.syncJellyfinUsersWithRattatarrProfiles();
        return ProfilesWrapper.fromList(
                profileList.stream()
                        .map(profile -> ProfileResponseDTO.fromEntity(profile, zoneId))
                        .toList()
        );
    }

    @PutMapping("/sync-media")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO syncMedia() {
        logger.info("Starting Jellyfin media synchronization");
        var job = backgroundJobService.create(JobType.JELLYFIN_SYNC, null);
        jellyfinTraversalService.syncMediaAsync(job);
        return BackgroundJobResponseDTO.fromEntity(job);
    }

    @PostMapping("/poll-activity")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO pollActivity() {
        logger.info("Manually triggering Jellyfin activity poll");
        var job = backgroundJobService.create(JobType.JELLYFIN_ACTIVITY_POLL, null);
        jellyfinActivityPollingService.triggerPollAsync(job);
        return BackgroundJobResponseDTO.fromEntity(job);
    }
}
