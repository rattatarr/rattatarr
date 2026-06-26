package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.dtos.requests.DeleteRateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.DeleteReviewRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.RateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.ReviewRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.services.BackgroundJobService;
import com.rattatarr.rattatarr.services.IMDbImportService;
import com.rattatarr.rattatarr.services.ProfilesService;
import com.rattatarr.rattatarr.services.RatingService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private final BackgroundJobService backgroundJobService;

    public RatingsController(RatingService ratingService, IMDbImportService imDbImportService, ProfilesService profilesService, BackgroundJobService backgroundJobService) {
        this.ratingService = ratingService;
        this.imDbImportService = imDbImportService;
        this.profilesService = profilesService;
        this.backgroundJobService = backgroundJobService;
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

    @PutMapping("/review")
    public ResponseEntity<GenericResponseDTO> setReview(
            @RequestBody ReviewRequestDTO requestDTO
    ) {
        logger.info("Submitting review: {}", requestDTO);

        ratingService.setReview(requestDTO);

        return ResponseEntity.ok(
                GenericResponseDTO.success("Review saved successfully", null)
        );
    }

    @DeleteMapping("/review")
    public ResponseEntity<GenericResponseDTO> deleteReview(
            @RequestBody DeleteReviewRequestDTO requestDTO
    ) {
        logger.info("Deleting review: {}", requestDTO);

        ratingService.deleteReview(requestDTO);

        return ResponseEntity.ok(
                GenericResponseDTO.success("Review deleted successfully", null)
        );
    }

    @PostMapping("/import/imdb")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BackgroundJobResponseDTO importImdbCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("profileId") UUID profileId
    ) {
        logger.info("Received IMDb CSV import request for profile {}, file: {}",
                profilesService.describe(profileId), file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV");
        }

        // Parse CSV synchronously (before file is cleaned up)
        var rows = imDbImportService.parseCSV(file);
        var job = backgroundJobService.create(JobType.CSV_IMPORT, profileId);
        imDbImportService.triggerBackgroundImportRatings(rows, profileId, job);
        return BackgroundJobResponseDTO.fromEntity(job);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportRatingsCsv(
            @RequestParam("profileId") UUID profileId
    ) {
        Profile profile = profilesService.findByIdOrThrow(
                profileId,
                ProfilesExceptions.ProfileNotFoundExceptions::new
        );

        logger.info("Exporting ratings CSV for profile '{}' ({})", profile.name(), profileId);

        byte[] csvData = ratingService.exportProfileMediaRatingsCsv(profile);

        String filename = "profile-" + profile.name() + "-ratings.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
