package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.services.MediaItemMetadataService;
import com.rattatarr.rattatarr.services.MediaItemsService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/test")
@ApiVersion("v1")
@NullMarked
public class TestController extends BaseController {
    private final MediaItemsService mediaItemsService;
    private final MediaItemMetadataService mediaItemMetadataService;
    private final TMDbClient tmDbClient;

    public TestController(MediaItemsService mediaItemsService, MediaItemMetadataService mediaItemMetadataService, TMDbClient tmDbClient) {
        this.mediaItemsService = mediaItemsService;
        this.mediaItemMetadataService = mediaItemMetadataService;
        this.tmDbClient = tmDbClient;
    }


    @GetMapping()
    public ResponseEntity<Object> test(
            @ModelAttribute("zoneId") ZoneId zoneId
    ) {
        var items = tmDbClient.findMovieById("15080");
//        var credits = tmDbClient.findMovieCreditsById("15080");
        var search = tmDbClient.searchMoviesByName("Inc", 1);
        var imageTest = tmDbClient.getImageUrl("/miA2ALg3DXiTs8BITDskYUvXcCj.jpg");
        var item2 = tmDbClient.findTVShowById("196890");

//        Optional<MediaItem> mediaItem = mediaItemsService.findById(UUID.fromString("4145a76b-9c26-46a7-a290-dfacca9daeb1"));
//        if (mediaItem.isPresent()) {
//            mediaItemMetadataService.enrichFromTMDbAsync(mediaItem.get())
//                    .doOnNext(metadata -> System.out.println("Enriched metadata: " + metadata))
//                    .doOnError(error -> System.err.println("Error enriching metadata: " + error.getMessage()))
//                    .subscribe();
//        } else {
//            System.out.println("Media item not found.");
//        }
//        mediaItemMetadataService.refreshAllMetadataAsync(true)
//                .doOnSuccess(unused -> System.out.println("Metadata refresh completed successfully."))
//                .doOnError(error -> System.err.println("Error during metadata refresh: " + error.getMessage()))
//                .subscribe();
//        System.out.println("credits: " + credits.block());

        return ResponseEntity.ok(Map.of(
                "items", items,
                "search", search,
                "imageTest", imageTest,
                "item2", item2
        ));


    }

}
