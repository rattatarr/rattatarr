package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.DeleteRateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.RateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.services.IMDbImportService;
import com.rattatarr.rattatarr.services.ProfilesService;
import com.rattatarr.rattatarr.services.RatingService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/ratings")
@ApiVersion("v1")
@NullMarked
public class RatingsController extends BaseController {
    private final RatingService ratingService;
    private final IMDbImportService imDbImportService;
    private final ProfilesService profilesService;

    public RatingsController(RatingService ratingService, IMDbImportService imDbImportService, ProfilesService profilesService) {
        this.ratingService = ratingService;
        this.imDbImportService = imDbImportService;
        this.profilesService = profilesService;
    }

    @PutMapping
    public ResponseEntity<GenericResponseDTO> getSeries(
            @RequestBody RateRequestDTO requestDTO
    ) {
        logger.info("Submitting rating: {}", requestDTO);

        ratingService.rate(requestDTO);

        return ResponseEntity.ok(
                GenericResponseDTO.success("Rating submitted successfully", null)
        );
    }

    @DeleteMapping
    public ResponseEntity<GenericResponseDTO> deleteRating(
            @RequestBody DeleteRateRequestDTO requestDTO
    ) {
        logger.info("Deleting rating: {}", requestDTO);

        ratingService.deleteRating(requestDTO);

        return ResponseEntity.ok(
                GenericResponseDTO.success("Rating deleted successfully", null)
        );
    }

    @PostMapping("/import/imdb")
    public GenericResponseDTO importImdbCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("profileId") UUID profileId
    ) {
        logger.info("Received IMDb CSV import request for profile: {}, file: {}", profileId, file.getOriginalFilename());

        if (file.isEmpty()) {
            return GenericResponseDTO.failure("CSV file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return GenericResponseDTO.failure("File must be a CSV");
        }

        // Parse CSV synchronously (before file is cleaned up)
        var rows = imDbImportService.parseCSV(file);

        // Trigger async processing with parsed data
        imDbImportService.triggerBackgroundImportRatings(rows, profileId);

        return GenericResponseDTO.success("IMDb ratings import started", null);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportRatingsCsv(
            @RequestParam("profileId") UUID profileId
    ) {
        logger.info("Exporting ratings CSV for profile: {}", profileId);

        Profile profile = profilesService.findByIdOrThrow(
                profileId,
                ProfilesExceptions.ProfileNotFoundExceptions::new
        );

        byte[] csvData = ratingService.exportProfileMediaRatingsCsv(profile);

        String filename = "profile-" + profile.name() + "-ratings.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
