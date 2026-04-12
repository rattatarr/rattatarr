package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileStatisticsServiceTest {

    // Run tasks synchronously on the calling thread — suitable for unit tests.
    private final Executor executor = Runnable::run;
    @Mock
    private EntityManagerFactory entityManagerFactory;
    @Mock
    private ProfilesRepository profilesRepository;
    @Mock
    private TMDbClient tmdbClient;
    private ProfileStatisticsService service;

    @BeforeEach
    void setUp() {
        service = new ProfileStatisticsService(entityManagerFactory, profilesRepository, executor, tmdbClient);
    }

    @Test
    void getStatistics_WhenProfileNotFound_ShouldThrowException() {
        UUID profileId = UUID.randomUUID();
        when(profilesRepository.findById(profileId)).thenReturn(Optional.empty());

        assertThrows(
                ProfilesExceptions.ProfileNotFoundExceptions.class,
                () -> service.getStatistics(profileId, 8.0f, 3, 10, 10, 10, 10, 5, "w185"));

        verify(profilesRepository).findById(profileId);
    }
}
