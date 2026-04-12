package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.exceptions.BrokenMediaItemsExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.ResolveBrokenMediaItemRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.BrokenMediaItemResponseDTO;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import com.rattatarr.rattatarr.services.BrokenMediaItemsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrokenMediaItemsControllerTest {

    @Mock
    private BrokenMediaItemsService brokenMediaItemsService;

    @InjectMocks
    private BrokenMediaItemsController controller;

    @Test
    void resolveItem_shouldReturn200WithResolvedDTO() {
        // Given
        UUID brokenId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        ResolveBrokenMediaItemRequestDTO request = new ResolveBrokenMediaItemRequestDTO(mediaItemId);

        BrokenMediaItem resolved = new BrokenMediaItem(
                MediaType.MOVIE, "Test Movie", "jf-123",
                "tmdb-1", null, 2020, "TMDbId", true
        );
        when(brokenMediaItemsService.resolveById(brokenId, mediaItemId)).thenReturn(resolved);

        // When
        ResponseEntity<BrokenMediaItemResponseDTO> response = controller.resolveItem(brokenId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().resolved());
        verify(brokenMediaItemsService).resolveById(brokenId, mediaItemId);
    }

    @Test
    void resolveItem_shouldPropagateNotFoundFromService() {
        // Given
        UUID brokenId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        ResolveBrokenMediaItemRequestDTO request = new ResolveBrokenMediaItemRequestDTO(mediaItemId);

        when(brokenMediaItemsService.resolveById(brokenId, mediaItemId))
                .thenThrow(new BrokenMediaItemsExceptions.BrokenMediaItemNotFoundExceptions(brokenId));

        // When / Then
        assertThrows(
                BrokenMediaItemsExceptions.BrokenMediaItemNotFoundExceptions.class,
                () -> controller.resolveItem(brokenId, request)
        );
        verify(brokenMediaItemsService).resolveById(brokenId, mediaItemId);
    }
}
