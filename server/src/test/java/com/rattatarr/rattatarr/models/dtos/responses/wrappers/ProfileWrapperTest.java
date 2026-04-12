package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.ProfileResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProfileWrapperTest {
    @Test
    void testFromDTO() {
        ProfileResponseDTO profileDTO = new ProfileResponseDTO(UUID.randomUUID(), ZonedDateTime.now(), ZonedDateTime.now(), "Test", UUID.randomUUID().toString());
        ProfileWrapper wrapper = ProfileWrapper.fromDTO(profileDTO);
        assertNotNull(wrapper);
        assertEquals(profileDTO, wrapper.profile());
    }
}
