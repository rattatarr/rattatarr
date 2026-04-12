package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.ResolveBrokenMediaItemRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.BrokenMediaItemResponseDTO;
import com.rattatarr.rattatarr.services.BrokenMediaItemsService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/library/broken-media-items")
@ApiVersion("v1")
@NullMarked
public class BrokenMediaItemsController extends BaseController {

    private final BrokenMediaItemsService brokenMediaItemsService;

    public BrokenMediaItemsController(BrokenMediaItemsService brokenMediaItemsService) {
        this.brokenMediaItemsService = brokenMediaItemsService;
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<BrokenMediaItemResponseDTO> resolveItem(
            @PathVariable UUID id,
            @RequestBody @Valid ResolveBrokenMediaItemRequestDTO request
    ) {
        logger.info("Resolving broken media item with id {} using MediaItem id {}", id, request.mediaItemId());
        var resolved = brokenMediaItemsService.resolveById(id, request.mediaItemId());
        return ResponseEntity.ok(BrokenMediaItemResponseDTO.fromEntity(resolved));
    }

    @PostMapping("/seed")
    public ResponseEntity<List<BrokenMediaItemResponseDTO>> seed(
            @RequestParam(defaultValue = "5") int count
    ) {
        logger.info("Seeding {} test broken media items", count);
        var items = brokenMediaItemsService.seedTestData(count);
        return ResponseEntity.ok(items.stream().map(BrokenMediaItemResponseDTO::fromEntity).toList());
    }
}
