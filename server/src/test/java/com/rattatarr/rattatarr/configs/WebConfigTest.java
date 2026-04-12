package com.rattatarr.rattatarr.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WebConfigTest {

    private WebConfig webConfig;
    private PathMatchConfigurer configurer;
    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() {
        corsProperties = mock(CorsProperties.class);
        when(corsProperties.getAllowedOrigins()).thenReturn(List.of("http://localhost:3000"));
        when(corsProperties.getAllowedMethods())
                .thenReturn(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        when(corsProperties.getAllowedHeaders())
                .thenReturn(List.of("Content-Type", "Authorization"));
        when(corsProperties.isAllowCredentials()).thenReturn(true);
        when(corsProperties.getMaxAge()).thenReturn(3600L);

        webConfig = new WebConfig(corsProperties);
        configurer = mock(PathMatchConfigurer.class);
    }

    @Test
    void testConfigurePathMatch() {
        // When
        webConfig.configurePathMatch(configurer);

        // Then
        verify(configurer, times(2)).addPathPrefix(anyString(), any());
    }

    @Test
    void testConfigurePathMatchWithV1Prefix() {
        // When
        webConfig.configurePathMatch(configurer);

        // Then
        verify(configurer).addPathPrefix(eq("/api/v1"), any());
    }

    @Test
    void testConfigurePathMatchWithV2Prefix() {
        // When
        webConfig.configurePathMatch(configurer);

        // Then
        verify(configurer).addPathPrefix(eq("/api/v2"), any());
    }

    @Test
    void testApiVersionAnnotationMatchingV1() {
        // Given
        @ApiVersion("v1")
        class TestController {
        }

        // Then
        assertTrue(TestController.class.isAnnotationPresent(ApiVersion.class));
        assertEquals("v1", TestController.class.getAnnotation(ApiVersion.class).value());
    }

    @Test
    void testApiVersionAnnotationMatchingV2() {
        // Given
        @ApiVersion("v2")
        class TestController {
        }

        // Then
        assertTrue(TestController.class.isAnnotationPresent(ApiVersion.class));
        assertEquals("v2", TestController.class.getAnnotation(ApiVersion.class).value());
    }

    @Test
    void testAddCorsMappings() {
        // Given
        CorsRegistry corsRegistry = mock(CorsRegistry.class);
        CorsRegistration corsRegistration = mock(CorsRegistration.class);

        when(corsRegistry.addMapping("/**")).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(anyBoolean())).thenReturn(corsRegistration);
        when(corsRegistration.maxAge(anyLong())).thenReturn(corsRegistration);

        // When
        webConfig.addCorsMappings(corsRegistry);

        // Then
        verify(corsRegistry).addMapping("/**");
        verify(corsRegistration).allowedOrigins(any(String[].class));
        verify(corsRegistration).allowedMethods(any(String[].class));
        verify(corsRegistration).allowedHeaders(any(String[].class));
        verify(corsRegistration).allowCredentials(true);
        verify(corsRegistration).maxAge(3600L);
    }
}
