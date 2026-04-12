package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbMovieFullDetailsResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbShowFullDetailsResponseDTO;
import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.ImportMediaItemRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.SearchFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ImportMediaItemResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SearchGroupTMDbWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SearchTMDbWrapper;
import com.rattatarr.rattatarr.services.MediaItemCreditsService;
import com.rattatarr.rattatarr.services.TMDbService;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tmdb")
@ApiVersion("v1")
@NullMarked
public class TMDbController extends BaseController {
    private final TMDbService tmDbService;
    private final MediaItemCreditsService mediaItemCreditsService;

    public TMDbController(
            TMDbService tmDbService,
            MediaItemCreditsService mediaItemCreditsService
    ) {
        this.tmDbService = tmDbService;
        this.mediaItemCreditsService = mediaItemCreditsService;
    }

    @GetMapping("/test")
    public GenericResponseDTO testConnection() {
        logger.info("Testing TMDb connection");

        boolean success = tmDbService.testConnection();
        if (!success)
            return GenericResponseDTO.failure("TMDb connection failed");

        return GenericResponseDTO.success("TMDb connection successful", null);
    }

    @PutMapping("/build-credits")
    public GenericResponseDTO buildCredits(
            @RequestParam(defaultValue = "false") boolean forceRefresh
    ) {
        mediaItemCreditsService.triggerBackgroundCreditsUpdate(forceRefresh);

        return GenericResponseDTO.accepted(
                "TMDb credits build initiated",
                null
        );
    }

    @GetMapping("/movies/{TMDbId}")
    public TMDbMovieFullDetailsResponseDTO getMovieDetails(
            @PathVariable String TMDbId
    ) {
        return tmDbService.findMovieById(TMDbId);
    }

    @GetMapping("/series/{TMDbId}")
    public TMDbShowFullDetailsResponseDTO getShowDetails(
            @PathVariable String TMDbId
    ) {
        return tmDbService.findShowById(TMDbId);
    }

    @GetMapping("/search")
    public SearchGroupTMDbWrapper searchTMDbByName(
            @ModelAttribute SearchFiltersDTO filters
    ) {
        return tmDbService.searchByName(filters);
    }

    @GetMapping("/movies/search")
    public SearchTMDbWrapper searchMoviesByName(
            @ModelAttribute SearchFiltersDTO filters
    ) {
        return tmDbService.searchMoviesByName(filters);
    }

    @GetMapping("/series/search")
    public SearchTMDbWrapper searchTVSeriesByName(
            @ModelAttribute SearchFiltersDTO filters
    ) {
        return tmDbService.searchSeriesByName(filters);
    }

    @PutMapping("/import")
    public ImportMediaItemResponseDTO importTMDbData(
            @RequestBody ImportMediaItemRequestDTO requestDTO
    ) {
        var mediaItem = tmDbService.importMediaItem(requestDTO.id(), requestDTO.mediaType());
        return ImportMediaItemResponseDTO.fromMediaItem(mediaItem);
    }
}
