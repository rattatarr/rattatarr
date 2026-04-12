package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.ShowResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SeriesResponseWrapperTest {

    @Test
    void fromPage_shouldCreateWrapperFromPage() {
        // Given
        ShowResponseDTO series = new ShowResponseDTO(
                UUID.randomUUID(), "jf-456", "Test Series", "456", "tt456",
                2023, 60, null, Set.of(), null, null, null, null
        );
        Page<ShowResponseDTO> page = new PageImpl<>(List.of(series), PageRequest.of(0, 20), 1);

        // When
        SeriesResponseWrapper wrapper = SeriesResponseWrapper.fromPage(page);

        // Then
        assertNotNull(wrapper);
        assertEquals(1, wrapper.series().size());
        assertEquals("Test Series", wrapper.series().getFirst().title());
        assertNotNull(wrapper.pagination());
        assertEquals(0, wrapper.pagination().currentPage());
        assertEquals(20, wrapper.pagination().pageSize());
        assertEquals(1, wrapper.pagination().totalElements());
    }

    @Test
    void fromList_shouldCreateWrapperFromList() {
        // Given
        ShowResponseDTO series = new ShowResponseDTO(
                UUID.randomUUID(), "jf-456", "Test Series", "456", "tt456",
                2023, 60, null, Set.of(), null, null, null, null
        );
        List<ShowResponseDTO> seriesList = List.of(series);

        // When
        SeriesResponseWrapper wrapper = SeriesResponseWrapper.fromList(seriesList);

        // Then
        assertNotNull(wrapper);
        assertEquals(1, wrapper.series().size());
        assertNull(wrapper.pagination());
    }
}
