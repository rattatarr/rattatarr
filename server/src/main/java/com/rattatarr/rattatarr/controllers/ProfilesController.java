package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.CreateProfileRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.ProfilesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ProfileResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfileWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfilesWrapper;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.services.ProfilesService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@ApiVersion("v1")
public class ProfilesController extends BaseController {

    private final ProfilesService profilesService;

    public ProfilesController(ProfilesService profilesService) {
        this.profilesService = profilesService;
    }

    @PostMapping
    public ResponseEntity<ProfileWrapper> createProfile(
            @Valid @RequestBody CreateProfileRequestDTO requestDTO,
            @ModelAttribute("zoneId") ZoneId zoneId
    ) {
        logger.debug("Creating profile: {}", requestDTO.toString());

        Profile profile = new Profile(
                requestDTO.name(),
                requestDTO.jellyfinId()
        );
        profile = profilesService.save(profile);

        return ResponseEntity.ok(
                ProfileWrapper.fromDTO(ProfileResponseDTO.fromEntity(profile, zoneId))
        );
    }

    @GetMapping
    public ResponseEntity<ProfilesWrapper> getAllProfiles(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @ModelAttribute("zoneId") ZoneId zoneId,
            @ModelAttribute ProfilesFiltersDTO filters
    ) {
        logger.debug("Fetching all profiles");

        ProfilesWrapper profilesWrapper = ProfilesWrapper.fromPage(
                profilesService.filterProfiles(filters, zoneId, pageable).map(
                        profile -> ProfileResponseDTO.fromEntity(profile, zoneId)
                )
        );

        return ResponseEntity.ok(profilesWrapper);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProfileWrapper> getProfileById(
            @PathVariable("id") UUID id,
            @ModelAttribute("zoneId") ZoneId zoneId
    ) {
        logger.debug("Fetching profile with id: {}", id);

        Optional<Profile> profile = profilesService.findById(id);
        if (profile.isEmpty()) {
            throw new ProfilesExceptions.ProfileNotFoundExceptions(id);
        }

        return ResponseEntity.ok(
                ProfileWrapper.fromDTO(ProfileResponseDTO.fromEntity(profile.get(), zoneId))
        );
    }
}
