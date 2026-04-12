package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.ProfileStatisticsRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ProfileStatisticsResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.ProfileStatisticsWrapper;
import com.rattatarr.rattatarr.services.ProfileStatisticsService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles/statistics")
@ApiVersion("v1")
@NullMarked
public class ProfileStatisticsController extends BaseController {

    private final ProfileStatisticsService profileStatisticsService;

    public ProfileStatisticsController(ProfileStatisticsService profileStatisticsService) {
        this.profileStatisticsService = profileStatisticsService;
    }

    @GetMapping
    public ResponseEntity<ProfileStatisticsWrapper> getProfileStatistics(
            @Valid @ModelAttribute ProfileStatisticsRequestDTO requestDTO) {
        logger.debug("Getting statistics for profile: {}", requestDTO.profileId());

        ProfileStatisticsResponseDTO statistics = profileStatisticsService.getStatistics(
                requestDTO.profileId(),
                requestDTO.ratingThreshold(),
                requestDTO.minCount(),
                requestDTO.genresLimit(),
                requestDTO.actorsLimit(),
                requestDTO.directorsLimit(),
                requestDTO.producersLimit(),
                requestDTO.genreOverTimeLimit(),
                requestDTO.profileImageSize());

        return ResponseEntity.ok(ProfileStatisticsWrapper.fromDTO(statistics));
    }
}
