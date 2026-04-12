package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BrokenMediaItemResponseWrapperTest {

    @Test
    void fromPage_shouldCreateWrapperFromPage() {
        // Given
        BrokenMediaItem brokenMovie = new BrokenMediaItem(
                MediaType.MOVIE, "Broken Movie", "jf-123", null, null, 2023, "Missing TMDb ID"
        );
        Page<BrokenMediaItem> page = new PageImpl<>(List.of(brokenMovie), PageRequest.of(0, 20), 1);

        // When
        BrokenMediaItemResponseWrapper wrapper = BrokenMediaItemResponseWrapper.fromPage(page);

        // Then
        assertNotNull(wrapper);
        assertEquals(1, wrapper.movies().size());
        assertEquals("Broken Movie", wrapper.movies().getFirst().title());
        assertNotNull(wrapper.pagination());
        assertEquals(0, wrapper.pagination().currentPage());
        assertEquals(20, wrapper.pagination().pageSize());
        assertEquals(1, wrapper.pagination().totalElements());
    }

    @Test
    void fromList_shouldCreateWrapperFromList() {
        // Given
        BrokenMediaItem brokenSeries = new BrokenMediaItem(
                MediaType.SERIES, "Broken Series", "jf-456", null, null, 2023, "Missing TMDb ID"
        );
        List<BrokenMediaItem> brokenItems = List.of(brokenSeries);

        // When
        BrokenMediaItemResponseWrapper wrapper = BrokenMediaItemResponseWrapper.fromList(brokenItems);

        // Then
        assertNotNull(wrapper);
        assertEquals(1, wrapper.movies().size());
        assertEquals("Broken Series", wrapper.movies().getFirst().title());
        assertNull(wrapper.pagination());
    }
}
