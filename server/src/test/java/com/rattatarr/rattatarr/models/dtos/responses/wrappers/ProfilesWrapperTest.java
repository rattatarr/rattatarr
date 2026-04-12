package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.ProfileResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProfilesWrapperTest {
    @Test
    void testFromPage() {
        ProfileResponseDTO dto = new ProfileResponseDTO(UUID.randomUUID(), ZonedDateTime.now(), ZonedDateTime.now(), "Test", UUID.randomUUID().toString());
        Page<ProfileResponseDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        ProfilesWrapper wrapper = ProfilesWrapper.fromPage(page);
        assertNotNull(wrapper);
        assertEquals(1, wrapper.profiles().size());
        assertEquals(dto, wrapper.profiles().getFirst());
        assertEquals(0, wrapper.pagination().currentPage());
        assertEquals(20, wrapper.pagination().pageSize());
        assertEquals(1, wrapper.pagination().totalElements());
        assertEquals(1, wrapper.pagination().totalPages());
        assertTrue(wrapper.pagination().isFirst());
        assertTrue(wrapper.pagination().isLast());
        assertFalse(wrapper.pagination().hasNext());
        assertFalse(wrapper.pagination().hasPrevious());
    }
}
