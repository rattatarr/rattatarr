package com.rattatarr.rattatarr.clients.tmdb;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbConfigurationResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbFindItemResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbFindResponseDTO;
import com.rattatarr.rattatarr.configs.RestClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TMDbClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClientProperties properties;

    @Mock
    private TMDbConfig config;

    private RestClient.RequestHeadersUriSpec getSpec;
    private RestClient.RequestHeadersSpec headersSpec;
    private RestClient.ResponseSpec responseSpec;

    private TMDbClient tmDbClient;

    @BeforeEach
    void setUp() {
        getSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        headersSpec = Mockito.mock(RestClient.RequestHeadersSpec.class);
        responseSpec = Mockito.mock(RestClient.ResponseSpec.class);

        tmDbClient = new TMDbClient(restClient, properties, config);

        // Setup RestClient mock chain
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // Setup config mocks
        when(config.getAuthHeader()).thenReturn("Bearer test-token");
        when(config.buildUrl(anyString())).thenAnswer(invocation ->
                "https://api.themoviedb.org" + invocation.getArgument(0)
        );
        when(config.getImageUrl(anyString(), any())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            String size = invocation.getArgument(1);
            return "https://image.tmdb.org/t/p/" + (size != null ? size : "w500") + path;
        });

        // Default: no retries so errors propagate immediately
        when(properties.getMaxRetries()).thenReturn(0);
    }

    @Test
    void findByIMDbId_withMovieResults_shouldReturnFindResponse() {
        // Given
        String imdbId = "tt1234567";
        TMDbFindItemResponseDTO movieResult = new TMDbFindItemResponseDTO(
                12345,
                "The Test Movie",
                "A test movie overview",
                "/poster.jpg",
                "/backdrop.jpg",
                "2023-01-15"
        );
        TMDbFindResponseDTO expectedResponse = new TMDbFindResponseDTO(
                List.of(movieResult),
                Collections.emptyList()
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(expectedResponse);

        // When
        TMDbFindResponseDTO result = tmDbClient.findByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.movieResults().size());
        assertEquals(0, result.tvResults().size());
        assertEquals("The Test Movie", result.movieResults().get(0).title());
        assertEquals(12345, result.movieResults().get(0).id());

        verify(restClient).get();
        verify(getSpec).uri(contains("/3/find/" + imdbId));
        verify(getSpec).uri(contains("external_source=imdb_id"));
    }

    @Test
    void findByIMDbId_withTVResults_shouldReturnFindResponse() {
        // Given
        String imdbId = "tt7654321";
        TMDbFindItemResponseDTO tvResult = new TMDbFindItemResponseDTO(
                67890,
                "The Test Series",
                "A test TV series overview",
                "/tv-poster.jpg",
                "/tv-backdrop.jpg",
                "2022-05-20"
        );
        TMDbFindResponseDTO expectedResponse = new TMDbFindResponseDTO(
                Collections.emptyList(),
                List.of(tvResult)
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(expectedResponse);

        // When
        TMDbFindResponseDTO result = tmDbClient.findByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.movieResults().size());
        assertEquals(1, result.tvResults().size());
        assertEquals("The Test Series", result.tvResults().getFirst().title());
        assertEquals(67890, result.tvResults().getFirst().id());
    }

    @Test
    void findByIMDbId_withInvalidId_shouldReturnEmptyResults() {
        // Given
        String imdbId = "tt9999999";
        TMDbFindResponseDTO expectedResponse = new TMDbFindResponseDTO(
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(expectedResponse);

        // When
        TMDbFindResponseDTO result = tmDbClient.findByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.movieResults().size());
        assertEquals(0, result.tvResults().size());
    }

    @Test
    void findMovieByIMDbId_withValidId_shouldReturnMovieDetails() {
        // Given
        String imdbId = "tt1234567";
        TMDbFindItemResponseDTO movieResult = new TMDbFindItemResponseDTO(
                12345,
                "The Test Movie",
                "A test movie overview",
                "/poster.jpg",
                "/backdrop.jpg",
                "2023-01-15"
        );
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                List.of(movieResult),
                Collections.emptyList()
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findMovieByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(12345, result.id());
        assertEquals("The Test Movie", result.title());
        assertEquals("A test movie overview", result.description());
        assertEquals("/poster.jpg", result.posterPath());
        assertEquals("/backdrop.jpg", result.backdropPath());
        assertEquals("2023-01-15", result.releaseDate());

        verify(restClient).get();
    }

    @Test
    void findMovieByIMDbId_withInvalidId_shouldReturnEmpty() {
        // Given
        String imdbId = "tt9999999";
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findMovieByIMDbId(imdbId);

        // Then
        assertNull(result);
        verify(restClient).get();
    }

    @Test
    void findMovieByIMDbId_withTVShowId_shouldReturnEmpty() {
        // Given
        String imdbId = "tt7654321";
        TMDbFindItemResponseDTO tvResult = new TMDbFindItemResponseDTO(
                67890,
                "The Test Series",
                "A test TV series overview",
                "/tv-poster.jpg",
                "/tv-backdrop.jpg",
                "2022-05-20"
        );
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                Collections.emptyList(),
                List.of(tvResult)
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findMovieByIMDbId(imdbId);

        // Then
        assertNull(result);
    }

    @Test
    void findTVShowByIMDbId_withValidId_shouldReturnTVShowDetails() {
        // Given
        String imdbId = "tt7654321";
        TMDbFindItemResponseDTO tvResult = new TMDbFindItemResponseDTO(
                67890,
                "The Test Series",
                "A test TV series overview",
                "/tv-poster.jpg",
                "/tv-backdrop.jpg",
                "2022-05-20"
        );
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                Collections.emptyList(),
                List.of(tvResult)
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findTVShowByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(67890, result.id());
        assertEquals("The Test Series", result.title());
        assertEquals("A test TV series overview", result.description());
        assertEquals("/tv-poster.jpg", result.posterPath());
        assertEquals("/tv-backdrop.jpg", result.backdropPath());
        assertEquals("2022-05-20", result.releaseDate());

        verify(restClient).get();
    }

    @Test
    void findTVShowByIMDbId_withInvalidId_shouldReturnEmpty() {
        // Given
        String imdbId = "tt9999999";
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findTVShowByIMDbId(imdbId);

        // Then
        assertNull(result);
        verify(restClient).get();
    }

    @Test
    void findTVShowByIMDbId_withMovieId_shouldReturnEmpty() {
        // Given
        String imdbId = "tt1234567";
        TMDbFindItemResponseDTO movieResult = new TMDbFindItemResponseDTO(
                12345,
                "The Test Movie",
                "A test movie overview",
                "/poster.jpg",
                "/backdrop.jpg",
                "2023-01-15"
        );
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                List.of(movieResult),
                Collections.emptyList()
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findTVShowByIMDbId(imdbId);

        // Then
        assertNull(result);
    }

    @Test
    void findMovieByIMDbId_withMultipleResults_shouldReturnFirstResult() {
        // Given
        String imdbId = "tt1234567";
        TMDbFindItemResponseDTO firstMovie = new TMDbFindItemResponseDTO(
                12345,
                "First Movie",
                "First overview",
                "/poster1.jpg",
                "/backdrop1.jpg",
                "2023-01-15"
        );
        TMDbFindItemResponseDTO secondMovie = new TMDbFindItemResponseDTO(
                67890,
                "Second Movie",
                "Second overview",
                "/poster2.jpg",
                "/backdrop2.jpg",
                "2023-02-20"
        );
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                List.of(firstMovie, secondMovie),
                Collections.emptyList()
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findMovieByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(12345, result.id());
        assertEquals("First Movie", result.title());
    }

    @Test
    void findTVShowByIMDbId_withMultipleResults_shouldReturnFirstResult() {
        // Given
        String imdbId = "tt7654321";
        TMDbFindItemResponseDTO firstShow = new TMDbFindItemResponseDTO(
                11111,
                "First Show",
                "First show overview",
                "/tv1.jpg",
                "/backdrop1.jpg",
                "2022-01-01"
        );
        TMDbFindItemResponseDTO secondShow = new TMDbFindItemResponseDTO(
                22222,
                "Second Show",
                "Second show overview",
                "/tv2.jpg",
                "/backdrop2.jpg",
                "2022-02-02"
        );
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                Collections.emptyList(),
                List.of(firstShow, secondShow)
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findTVShowByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(11111, result.id());
        assertEquals("First Show", result.title());
    }

    @Test
    void findMovieByIMDbId_withNullFields_shouldHandleGracefully() {
        // Given
        String imdbId = "tt1234567";
        TMDbFindItemResponseDTO movieResult = new TMDbFindItemResponseDTO(
                12345,
                null,
                null,
                null,
                null,
                null
        );
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                List.of(movieResult),
                Collections.emptyList()
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findMovieByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(12345, result.id());
        assertNull(result.title());
        assertNull(result.description());
        assertNull(result.posterPath());
        assertNull(result.backdropPath());
        assertNull(result.releaseDate());
    }

    @Test
    void findTVShowByIMDbId_withNullFields_shouldHandleGracefully() {
        // Given
        String imdbId = "tt7654321";
        TMDbFindItemResponseDTO tvResult = new TMDbFindItemResponseDTO(
                67890,
                null,
                null,
                null,
                null,
                null
        );
        TMDbFindResponseDTO findResponse = new TMDbFindResponseDTO(
                Collections.emptyList(),
                List.of(tvResult)
        );

        when(responseSpec.body(TMDbFindResponseDTO.class))
                .thenReturn(findResponse);

        // When
        TMDbFindItemResponseDTO result = tmDbClient.findTVShowByIMDbId(imdbId);

        // Then
        assertNotNull(result);
        assertEquals(67890, result.id());
        assertNull(result.title());
        assertNull(result.description());
        assertNull(result.posterPath());
        assertNull(result.backdropPath());
        assertNull(result.releaseDate());
    }

    // ==================== getImageUrl() Tests ====================

    @Test
    void getImageUrl_withPath_shouldReturnImageUrl() {
        // Given
        String imagePath = "/abc123.jpg";

        // When
        String result = tmDbClient.getImageUrl(imagePath);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(imagePath));
        assertTrue(result.contains("w500")); // Default size
        verify(config).getImageUrl(imagePath, null);
    }

    @Test
    void getImageUrl_withPathAndSize_shouldReturnImageUrl() {
        // Given
        String imagePath = "/xyz789.jpg";
        String size = "w780";

        // When
        String result = tmDbClient.getImageUrl(imagePath, size);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(imagePath));
        assertTrue(result.contains(size));
        verify(config).getImageUrl(imagePath, size);
    }

    @Test
    void getImageUrl_withOriginalSize_shouldReturnOriginalImageUrl() {
        // Given
        String imagePath = "/poster.jpg";
        String size = "original";

        // When
        String result = tmDbClient.getImageUrl(imagePath, size);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(imagePath));
        assertTrue(result.contains("original"));
        verify(config).getImageUrl(imagePath, size);
    }

    // ==================== testConnection() Tests ====================

    @Test
    void testConnection_withValidConfig_shouldReturnTrue() {
        // Given
        TMDbConfigurationResponseDTO.Images images = new TMDbConfigurationResponseDTO.Images("http://image.tmdb.org/t/p/");
        TMDbConfigurationResponseDTO config = new TMDbConfigurationResponseDTO(images);
        when(responseSpec.body(TMDbConfigurationResponseDTO.class)).thenReturn(config);

        // When
        boolean result = tmDbClient.testConnection();

        // Then
        assertTrue(result);
        verify(restClient).get();
        verify(getSpec).uri(contains("/3/configuration"));
        verify(headersSpec).headers(any());
    }

    @Test
    void testConnection_withNullImages_shouldReturnFalse() {
        // Given
        TMDbConfigurationResponseDTO configResponse = new TMDbConfigurationResponseDTO(null);
        when(responseSpec.body(TMDbConfigurationResponseDTO.class)).thenReturn(configResponse);

        // When
        boolean result = tmDbClient.testConnection();

        // Then
        assertFalse(result);
        verify(restClient).get();
        verify(getSpec).uri(contains("/3/configuration"));
    }

    @Test
    void testConnection_withNullBaseUrl_shouldReturnFalse() {
        // Given
        TMDbConfigurationResponseDTO.Images images = new TMDbConfigurationResponseDTO.Images(null);
        TMDbConfigurationResponseDTO configResponse = new TMDbConfigurationResponseDTO(images);
        when(responseSpec.body(TMDbConfigurationResponseDTO.class)).thenReturn(configResponse);

        // When
        boolean result = tmDbClient.testConnection();

        // Then
        assertFalse(result);
        verify(restClient).get();
        verify(getSpec).uri(contains("/3/configuration"));
    }

    @Test
    void testConnection_whenTMDbDown_shouldReturnFalse() {
        // Given
        RestClientResponseException exception = new RestClientResponseException(
                "Service Unavailable",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable",
                null,
                "{\"error\":\"TMDb server is down\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(responseSpec.body(TMDbConfigurationResponseDTO.class)).thenThrow(exception);

        // When
        boolean result = tmDbClient.testConnection();

        // Then
        assertFalse(result);
    }

    @Test
    void testConnection_withHtmlResponse_shouldReturnFalse() {
        // Given
        org.springframework.web.client.RestClientException exception =
                new org.springframework.web.client.RestClientException(
                        "Could not extract response: no suitable HttpMessageConverter found"
                );

        when(responseSpec.body(TMDbConfigurationResponseDTO.class)).thenThrow(exception);

        // When
        boolean result = tmDbClient.testConnection();

        // Then
        assertFalse(result);
    }
}
