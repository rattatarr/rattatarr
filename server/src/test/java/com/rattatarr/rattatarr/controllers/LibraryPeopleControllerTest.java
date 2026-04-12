package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.PeopleFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PersonResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.PeopleWrapper;
import com.rattatarr.rattatarr.services.PeopleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryPeopleControllerTest {

    @Mock
    private PeopleService peopleService;

    @InjectMocks
    private LibraryPeopleController controller;

    @Test
    void getPeople_shouldReturnPeople() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        PeopleFiltersDTO filters = new PeopleFiltersDTO(null, null);
        PersonResponseDTO person = new PersonResponseDTO(UUID.randomUUID(), "Tom Hanks", "31", "/tom.jpg");
        Page<PersonResponseDTO> peoplePage = new PageImpl<>(List.of(person), pageable, 1);

        when(peopleService.filterPeople(any(PeopleFiltersDTO.class), any(Pageable.class)))
                .thenReturn(peoplePage);

        // When
        ResponseEntity<PeopleWrapper> response = controller.getPeople(pageable, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().people().size());
        verify(peopleService).filterPeople(eq(filters), eq(pageable));
    }

    @Test
    void getPeople_shouldHandleEmptyResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        PeopleFiltersDTO filters = new PeopleFiltersDTO(null, null);
        Page<PersonResponseDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(peopleService.filterPeople(any(PeopleFiltersDTO.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        ResponseEntity<PeopleWrapper> response = controller.getPeople(pageable, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().people().size());
    }

    @Test
    void getPersonById_shouldReturnPersonWithResolvedUrl() {
        // Given
        String personId = UUID.randomUUID().toString();
        String profileSize = "w185";
        PersonResponseDTO personDTO = new PersonResponseDTO(UUID.randomUUID(), "Tom Hanks", "31", "https://image.tmdb.org/t/p/w185/tom.jpg");

        when(peopleService.getPersonById(eq(personId), eq(profileSize))).thenReturn(personDTO);

        // When
        ResponseEntity<PersonResponseDTO> response = controller.getPersonById(personId, profileSize);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tom Hanks", response.getBody().name());
        assertEquals("https://image.tmdb.org/t/p/w185/tom.jpg", response.getBody().profilePathUrl());
        verify(peopleService).getPersonById(eq(personId), eq(profileSize));
    }

    @Test
    void getPersonById_withTMDbId_shouldReturnPerson() {
        // Given
        String tmdbId = "1606164";
        String profileSize = "w185";
        PersonResponseDTO personDTO = new PersonResponseDTO(UUID.randomUUID(), "Tom Hanks", tmdbId, "https://image.tmdb.org/t/p/w185/tom.jpg");

        when(peopleService.getPersonById(eq(tmdbId), eq(profileSize))).thenReturn(personDTO);

        // When
        ResponseEntity<PersonResponseDTO> response = controller.getPersonById(tmdbId, profileSize);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tom Hanks", response.getBody().name());
        assertEquals(tmdbId, response.getBody().TMDbId());
        verify(peopleService).getPersonById(eq(tmdbId), eq(profileSize));
    }

    @Test
    void getPersonById_shouldReturn404WhenPersonNotFound() {
        // Given
        String personId = UUID.randomUUID().toString();
        String profileSize = "w185";

        when(peopleService.getPersonById(eq(personId), eq(profileSize)))
                .thenThrow(new CommonExceptions.ResourceNotFoundExceptions("Person not found: " + personId));

        // When / Then
        assertThrows(CommonExceptions.ResourceNotFoundExceptions.class,
                () -> controller.getPersonById(personId, profileSize));
        verify(peopleService).getPersonById(eq(personId), eq(profileSize));
    }
}
