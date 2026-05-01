package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.YearRewindRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.YearRewindResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.YearRewindAvailableYearsWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.YearRewindWrapper;
import com.rattatarr.rattatarr.services.YearRewindService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles/statistics/rewind")
@ApiVersion("v1")
@NullMarked
public class YearRewindController extends BaseController {

    private final YearRewindService yearRewindService;

    public YearRewindController(YearRewindService yearRewindService) {
        this.yearRewindService = yearRewindService;
    }

    @GetMapping
    public ResponseEntity<YearRewindWrapper> getYearRewind(
            @Valid @ModelAttribute YearRewindRequestDTO requestDTO) {
        logger.debug("Getting year rewind for profile: {}, year: {}", requestDTO.profileId(), requestDTO.year());

        YearRewindResponseDTO rewind = yearRewindService.getRewind(
                requestDTO.profileId(),
                requestDTO.year(),
                requestDTO.ratingThreshold(),
                requestDTO.minCount(),
                requestDTO.genresLimit(),
                requestDTO.actorsLimit(),
                requestDTO.directorsLimit(),
                requestDTO.producersLimit(),
                requestDTO.profileImageSize());

        return ResponseEntity.ok(YearRewindWrapper.fromDTO(rewind));
    }

    @GetMapping("/years")
    public ResponseEntity<YearRewindAvailableYearsWrapper> getAvailableYears(
            @NotNull @RequestParam UUID profileId) {
        logger.debug("Getting available rewind years for profile: {}", profileId);

        List<Integer> years = yearRewindService.getAvailableYears(profileId);

        return ResponseEntity.ok(YearRewindAvailableYearsWrapper.fromList(years));
    }
}
