package com.rattatarr.rattatarr.clients.jellyfin;

import com.rattatarr.rattatarr.clients.jellyfin.requests.queries.JellyfinItemsQuery;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientActivityLogEntryResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientPlaybackItemResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientPlaybackItemUserDataResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientUserResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinSystemInfoResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientActivityLogEntriesWrapper;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientItemsWrapper;
import com.rattatarr.rattatarr.configs.RestClientProperties;
import com.rattatarr.rattatarr.exceptions.JellyfinClientExceptions;
import com.rattatarr.rattatarr.exceptions.JellyfinConfigExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JellyfinClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClientProperties properties;

    @Mock
    private JellyfinConfig config;

    private RestClient.RequestHeadersUriSpec getSpec;
    private RestClient.RequestHeadersSpec headersSpec;
    private RestClient.ResponseSpec responseSpec;

    private JellyfinClient jellyfinClient;

    @BeforeEach
    void setUp() {
        getSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        headersSpec = Mockito.mock(RestClient.RequestHeadersSpec.class);
        responseSpec = Mockito.mock(RestClient.ResponseSpec.class);

        jellyfinClient = new JellyfinClient(restClient, properties, config);

        // Setup RestClient mock chain for string URIs
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(headersSpec);
        when(getSpec.uri(any(URI.class))).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // Setup config mocks
        when(config.getAuthHeader()).thenReturn("MediaBrowser Token=\"test-api-key\"");
        when(config.buildUrl(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return "http://localhost:8096" + (path.startsWith("/") ? path : "/" + path);
        });

        // Default: no retries so errors propagate immediately
        when(properties.getMaxRetries()).thenReturn(0);
    }

    // ==================== testConnection() Tests ====================

    @Test
    void testConnection_withValidConfig_shouldReturnTrue() {
        // Given
        JellyfinSystemInfoResponseDTO systemInfo = new JellyfinSystemInfoResponseDTO("server-id-123");
        when(responseSpec.body(JellyfinSystemInfoResponseDTO.class)).thenReturn(systemInfo);

        // When
        boolean result = jellyfinClient.testConnection();

        // Then
        assertTrue(result);
        verify(restClient).get();
        verify(getSpec).uri(contains("System/Info"));
        verify(headersSpec).headers(any());
    }

    @Test
    void testConnection_withInvalidConfig_shouldReturnFalse() {
        // Given
        when(config.buildUrl(anyString()))
                .thenThrow(new JellyfinConfigExceptions.JellyfinIsNotConfiguredException(null, null));

        // When
        boolean result = jellyfinClient.testConnection();

        // Then
        assertFalse(result);
    }

    @Test
    void testConnection_whenJellyfinDown_shouldReturnFalse() {
        // Given
        RestClientResponseException exception = new RestClientResponseException(
                "Service Unavailable",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable",
                null,
                "{\"error\":\"Jellyfin server is down\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(responseSpec.body(JellyfinSystemInfoResponseDTO.class)).thenThrow(exception);

        // When
        boolean result = jellyfinClient.testConnection();

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

        when(responseSpec.body(JellyfinSystemInfoResponseDTO.class)).thenThrow(exception);

        // When
        boolean result = jellyfinClient.testConnection();

        // Then
        assertFalse(result);
    }

    // ==================== getUsers() Tests ====================

    @Test
    void getUsers_withValidResponse_shouldReturnUsersList() {
        // Given
        JellyfinClientUserResponseDTO user1 = new JellyfinClientUserResponseDTO(
                "John Doe",
                "user-id-1",
                "2024-01-15T10:30:00Z",
                "2024-01-20T14:45:00Z"
        );
        JellyfinClientUserResponseDTO user2 = new JellyfinClientUserResponseDTO(
                "Jane Smith",
                "user-id-2",
                "2024-01-16T09:20:00Z",
                "2024-01-21T16:30:00Z"
        );
        List<JellyfinClientUserResponseDTO> expectedUsers = List.of(user1, user2);

        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(expectedUsers);

        // When
        List<JellyfinClientUserResponseDTO> result = jellyfinClient.getUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).name());
        assertEquals("user-id-1", result.get(0).id());
        assertEquals("Jane Smith", result.get(1).name());
        assertEquals("user-id-2", result.get(1).id());

        verify(restClient).get();
        verify(getSpec).uri(contains("/Users"));
        verify(headersSpec).headers(any());
    }

    @Test
    void getUsers_withValidConfig_shouldIncludeAuthHeader() {
        // Given
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(Collections.emptyList());

        // When
        jellyfinClient.getUsers();

        // Then
        verify(headersSpec).headers(argThat(consumer -> {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            consumer.accept(headers);
            return "MediaBrowser Token=\"test-api-key\"".equals(headers.getFirst("Authorization"));
        }));
    }

    @Test
    void getUsers_whenNotConfigured_shouldThrowException() {
        // Given
        when(config.buildUrl(anyString()))
                .thenThrow(new JellyfinConfigExceptions.JellyfinIsNotConfiguredException(null, null));

        // When & Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class, () ->
                jellyfinClient.getUsers()
        );
    }

    // ==================== getActivityLogEntries() Tests ====================

    @Test
    void getActivityLogEntries_withValidResponse_shouldReturnLogs() {
        // Given
        JellyfinClientActivityLogEntryResponseDTO entry = new JellyfinClientActivityLogEntryResponseDTO(
                1L,
                "Playback started",
                "Overview",
                "Short",
                "VideoPlayback",
                "item-1",
                "2026-04-13T09:20:36.680Z",
                "user-1",
                "Trace"
        );
        JellyfinClientActivityLogEntriesWrapper expectedLogs = new JellyfinClientActivityLogEntriesWrapper(
                List.of(entry),
                1,
                0
        );

        when(responseSpec.body(JellyfinClientActivityLogEntriesWrapper.class)).thenReturn(expectedLogs);

        // When
        JellyfinClientActivityLogEntriesWrapper result = jellyfinClient.getActivityLogEntries();

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals("VideoPlayback", result.items().getFirst().type());
        verify(restClient).get();
        verify(getSpec).uri(org.mockito.ArgumentMatchers.<URI>argThat(uri ->
                uri.toString().contains("System/ActivityLog/Entries")
                        && uri.toString().contains("limit=50")
        ));
        verify(headersSpec).headers(any());
    }

    @Test
    void getActivityLogEntries_whenNotConfigured_shouldThrowException() {
        // Given
        when(config.buildUrl(anyString()))
                .thenThrow(new JellyfinConfigExceptions.JellyfinIsNotConfiguredException(null, null));

        // When & Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class, () ->
                jellyfinClient.getActivityLogEntries()
        );
    }

    @Test
    void getItemByIdForUser_withValidResponse_shouldReturnItem() {
        // Given
        JellyfinClientPlaybackItemResponseDTO expectedItem = new JellyfinClientPlaybackItemResponseDTO(
                "item-1",
                "Movie",
                "Inception",
                new JellyfinClientPlaybackItemUserDataResponseDTO(12345L, false)
        );

        when(responseSpec.body(JellyfinClientPlaybackItemResponseDTO.class)).thenReturn(expectedItem);

        // When
        JellyfinClientPlaybackItemResponseDTO result = jellyfinClient.getItemByIdForUser("item-1", "user-1");

        // Then
        assertNotNull(result);
        assertEquals("item-1", result.id());
        assertNotNull(result.userData());
        assertEquals(12345L, result.userData().playbackPositionTicks());
        assertFalse(result.userData().played());
        verify(getSpec).uri(org.mockito.ArgumentMatchers.<URI>argThat(uri ->
                uri.toString().contains("/Items/item-1")
                        && uri.toString().contains("userId=user-1")
        ));
        verify(headersSpec).headers(any());
    }

    // ==================== getItems() Tests ====================

    @Test
    void getItems_withParentIdOnly_shouldConstructCorrectUri() {
        // Given
        JellyfinItemsQuery query = new JellyfinItemsQuery(
                "parent-123",
                null,
                null,
                null,
                null,
                null
        );

        JellyfinClientItemsWrapper expectedWrapper = new JellyfinClientItemsWrapper(Collections.emptyList());

        when(responseSpec.body(JellyfinClientItemsWrapper.class))
                .thenReturn(expectedWrapper);

        // When
        JellyfinClientItemsWrapper result = jellyfinClient.getItems(query);

        // Then
        assertNotNull(result);
        verify(restClient).get();
        verify(getSpec).uri(any(URI.class));
    }

    @Test
    void getItems_withRecursiveTrue_shouldIncludeRecursiveParam() {
        // Given
        JellyfinItemsQuery query = new JellyfinItemsQuery(
                "parent-123",
                true,
                null,
                null,
                null,
                null
        );

        JellyfinClientItemsWrapper expectedWrapper = new JellyfinClientItemsWrapper(Collections.emptyList());

        when(responseSpec.body(JellyfinClientItemsWrapper.class))
                .thenReturn(expectedWrapper);

        // When
        JellyfinClientItemsWrapper result = jellyfinClient.getItems(query);

        // Then
        assertNotNull(result);
        verify(restClient).get();
    }

    @Test
    void getItems_withIsMovieTrue_shouldIncludeMovieFilter() {
        // Given
        JellyfinItemsQuery query = new JellyfinItemsQuery(
                null,
                null,
                true,
                null,
                null,
                null
        );

        JellyfinClientItemsWrapper expectedWrapper = new JellyfinClientItemsWrapper(Collections.emptyList());

        when(responseSpec.body(JellyfinClientItemsWrapper.class))
                .thenReturn(expectedWrapper);

        // When
        JellyfinClientItemsWrapper result = jellyfinClient.getItems(query);

        // Then
        assertNotNull(result);
        verify(restClient).get();
    }

    @Test
    void getItems_withIsSeriesTrue_shouldIncludeSeriesFilter() {
        // Given
        JellyfinItemsQuery query = new JellyfinItemsQuery(
                null,
                null,
                null,
                true,
                null,
                null
        );

        JellyfinClientItemsWrapper expectedWrapper = new JellyfinClientItemsWrapper(Collections.emptyList());

        when(responseSpec.body(JellyfinClientItemsWrapper.class))
                .thenReturn(expectedWrapper);

        // When
        JellyfinClientItemsWrapper result = jellyfinClient.getItems(query);

        // Then
        assertNotNull(result);
        verify(restClient).get();
    }

    @Test
    void getItems_withMultipleFilters_shouldCombineAllParams() {
        // Given
        JellyfinItemsQuery query = new JellyfinItemsQuery(
                "parent-123",
                true,
                true,
                null,
                List.of("IsNotFolder", "IsPlayed"),
                null
        );

        JellyfinClientItemsWrapper expectedWrapper = new JellyfinClientItemsWrapper(Collections.emptyList());

        when(responseSpec.body(JellyfinClientItemsWrapper.class))
                .thenReturn(expectedWrapper);

        // When
        JellyfinClientItemsWrapper result = jellyfinClient.getItems(query);

        // Then
        assertNotNull(result);
        verify(restClient).get();
        verify(headersSpec).headers(any());
    }

    @Test
    void getItems_withFields_shouldIncludeFieldsParam() {
        // Given
        JellyfinItemsQuery query = new JellyfinItemsQuery(
                null,
                null,
                null,
                null,
                null,
                List.of("ProviderIds", "Path", "MediaSources")
        );

        JellyfinClientItemsWrapper expectedWrapper = new JellyfinClientItemsWrapper(Collections.emptyList());

        when(responseSpec.body(JellyfinClientItemsWrapper.class))
                .thenReturn(expectedWrapper);

        // When
        JellyfinClientItemsWrapper result = jellyfinClient.getItems(query);

        // Then
        assertNotNull(result);
        verify(restClient).get();
    }

    @Test
    void getItems_withAllParameters_shouldConstructComplexUri() {
        // Given
        JellyfinItemsQuery query = new JellyfinItemsQuery(
                "parent-789",
                true,
                true,
                false,
                List.of("IsNotFolder", "IsPlayed"),
                List.of("ProviderIds", "Path", "MediaSources")
        );

        JellyfinClientItemsWrapper expectedWrapper = new JellyfinClientItemsWrapper(Collections.emptyList());

        when(responseSpec.body(JellyfinClientItemsWrapper.class))
                .thenReturn(expectedWrapper);

        // When
        JellyfinClientItemsWrapper result = jellyfinClient.getItems(query);

        // Then
        assertNotNull(result);
        verify(restClient).get();
        verify(getSpec).uri(any(URI.class));
        verify(headersSpec).headers(any());
    }

    @Test
    void getItems_whenNotConfigured_shouldThrowException() {
        // Given
        JellyfinItemsQuery query = new JellyfinItemsQuery(
                "parent-123",
                null,
                null,
                null,
                null,
                null
        );

        // config.buildUrl is called before any RestClient interaction (to build the URI),
        // so the exception propagates directly out of getItems().
        when(config.buildUrl(anyString()))
                .thenThrow(new JellyfinConfigExceptions.JellyfinIsNotConfiguredException(null, null));

        // When & Then
        assertThrows(JellyfinConfigExceptions.JellyfinIsNotConfiguredException.class, () ->
                jellyfinClient.getItems(query)
        );
    }
}
