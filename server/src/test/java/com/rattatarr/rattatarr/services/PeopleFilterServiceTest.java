package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.PeopleFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PersonResponseDTO;
import com.rattatarr.rattatarr.models.entities.Person;
import com.rattatarr.rattatarr.repositories.PeopleRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleFilterServiceTest {

    @Mock
    private PeopleRepository repository;

    @Mock
    private MediaItemCastService mediaItemCastService;

    @Mock
    private MediaItemCrewService mediaItemCrewService;

    @Mock
    private MediaItemViewHelper mediaItemViewHelper;

    @InjectMocks
    private PeopleService service;

    @Test
    void filterPeople_withNullNameFilter_returnsAllPeople() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        PeopleFiltersDTO filters = new PeopleFiltersDTO(null, null);
        Person person = new Person("Tom Hanks", "31", "/tom.jpg");
        Page<Person> page = new PageImpl<>(List.of(person), pageable, 1);

        when(repository.findAll(ArgumentMatchers.<Specification<Person>>any(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<PersonResponseDTO> result = service.filterPeople(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Tom Hanks", result.getContent().get(0).name());
    }

    @Test
    void filterPeople_withNameFilter_returnsMatchingPeople() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        PeopleFiltersDTO filters = new PeopleFiltersDTO("Nolan", null);
        Person person = new Person("Christopher Nolan", "525", "/nolan.jpg");
        Page<Person> page = new PageImpl<>(List.of(person), pageable, 1);

        when(repository.findAll(ArgumentMatchers.<Specification<Person>>any(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<PersonResponseDTO> result = service.filterPeople(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Christopher Nolan", result.getContent().get(0).name());
        verify(repository).findAll(ArgumentMatchers.<Specification<Person>>any(), eq(pageable));
    }

    @Test
    void filterPeople_withEmptyResult_returnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        PeopleFiltersDTO filters = new PeopleFiltersDTO("nobody", null);
        Page<Person> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(ArgumentMatchers.<Specification<Person>>any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        Page<PersonResponseDTO> result = service.filterPeople(filters, pageable);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void filterPeople_withProfileSize_appliesImageUrlToProfilePathUrl() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        PeopleFiltersDTO filters = new PeopleFiltersDTO(null, "w185");
        Person person = new Person("Tom Hanks", "31", "/tom.jpg");
        Page<Person> page = new PageImpl<>(List.of(person), pageable, 1);

        when(repository.findAll(ArgumentMatchers.<Specification<Person>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemViewHelper.buildUrlFromPath("/tom.jpg", "w185"))
                .thenReturn("https://image.tmdb.org/t/p/w185/tom.jpg");

        // When
        Page<PersonResponseDTO> result = service.filterPeople(filters, pageable);

        // Then
        assertEquals("https://image.tmdb.org/t/p/w185/tom.jpg", result.getContent().get(0).profilePathUrl());
        verify(mediaItemViewHelper).buildUrlFromPath("/tom.jpg", "w185");
    }

    @Test
    void filterPeople_withNullProfileSize_defaultsToW185AndAppliesImageUrl() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        PeopleFiltersDTO filters = new PeopleFiltersDTO(null, null); // null defaults to "w185"
        Person person = new Person("Tom Hanks", "31", "/tom.jpg");
        Page<Person> page = new PageImpl<>(List.of(person), pageable, 1);

        when(repository.findAll(ArgumentMatchers.<Specification<Person>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemViewHelper.buildUrlFromPath("/tom.jpg", "w185"))
                .thenReturn("https://image.tmdb.org/t/p/w185/tom.jpg");

        // When
        Page<PersonResponseDTO> result = service.filterPeople(filters, pageable);

        // Then
        assertEquals("https://image.tmdb.org/t/p/w185/tom.jpg", result.getContent().get(0).profilePathUrl());
        verify(mediaItemViewHelper).buildUrlFromPath("/tom.jpg", "w185");
    }

    @Test
    void getPersonById_withTMDbId_returnsPersonWithResolvedUrl() {
        // Given
        String tmdbId = "1606164";
        String profileSize = "w185";
        Person person = new Person("Tom Hanks", tmdbId, "/tom.jpg");
        when(repository.findByTMDbId(tmdbId)).thenReturn(Optional.of(person));
        when(mediaItemViewHelper.buildUrlFromPath("/tom.jpg", profileSize))
                .thenReturn("https://image.tmdb.org/t/p/w185/tom.jpg");

        // When
        PersonResponseDTO result = service.getPersonById(tmdbId, profileSize);

        // Then
        assertNotNull(result);
        assertEquals("Tom Hanks", result.name());
        assertEquals("https://image.tmdb.org/t/p/w185/tom.jpg", result.profilePathUrl());
        verify(repository).findByTMDbId(tmdbId);
        verify(mediaItemViewHelper).buildUrlFromPath("/tom.jpg", profileSize);
    }

    @Test
    void getPersonById_withTMDbId_throwsWhenNotFound() {
        // Given
        String tmdbId = "9999999";
        when(repository.findByTMDbId(tmdbId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(CommonExceptions.ResourceNotFoundExceptions.class,
                () -> service.getPersonById(tmdbId, "w185"));
        verify(repository).findByTMDbId(tmdbId);
    }

    @Test
    void getPersonById_withUUID_returnsPersonWithResolvedUrl() {
        // Given
        UUID personId = UUID.randomUUID();
        String profileSize = "w185";
        Person person = new Person("Tom Hanks", "31", "/tom.jpg");
        when(repository.findById(personId)).thenReturn(Optional.of(person));
        when(mediaItemViewHelper.buildUrlFromPath("/tom.jpg", profileSize))
                .thenReturn("https://image.tmdb.org/t/p/w185/tom.jpg");

        // When
        PersonResponseDTO result = service.getPersonById(personId.toString(), profileSize);

        // Then
        assertNotNull(result);
        assertEquals("Tom Hanks", result.name());
        assertEquals("https://image.tmdb.org/t/p/w185/tom.jpg", result.profilePathUrl());
        verify(repository).findById(personId);
    }
}
