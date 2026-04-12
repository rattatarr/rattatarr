package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ProfileResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfilesWrapper;
import com.rattatarr.rattatarr.services.JellyfinService;
import com.rattatarr.rattatarr.services.JellyfinTraversalService;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;

@RestController
@RequestMapping("/jellyfin")
@ApiVersion("v1")
@NullMarked
public class JellyfinController extends BaseController {
    private final JellyfinService jellyfinService;
    private final JellyfinTraversalService jellyfinTraversalService;

    public JellyfinController(
            JellyfinService jellyfinService,
            JellyfinTraversalService jellyfinTraversalService
    ) {
        this.jellyfinService = jellyfinService;
        this.jellyfinTraversalService = jellyfinTraversalService;
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
    public GenericResponseDTO syncMedia() {
        logger.info("Starting Jellyfin media synchronization");

        jellyfinTraversalService.syncMediaAsync();
        return GenericResponseDTO.accepted(
                "Jellyfin media synchronization started in background",
                null
        );
    }
}
