package com.rattatarr.rattatarr.configs;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    private static final List<String> SUPPORTED_VERSIONS = List.of("v1", "v2");

    private final CorsProperties corsProperties;

    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        SUPPORTED_VERSIONS.forEach(version -> configurer.addPathPrefix(
                "/api/" + version,
                c -> c.isAnnotationPresent(ApiVersion.class)
                        && c.getAnnotation(ApiVersion.class).value().equals(version)));
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods(corsProperties.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(corsProperties.getAllowedHeaders().toArray(String[]::new))
                .allowCredentials(corsProperties.isAllowCredentials())
                .maxAge(corsProperties.getMaxAge());

        logger.info(
                "CORS configured: origins={}, methods={}, credentials={}, maxAge={}s",
                corsProperties.getAllowedOrigins(),
                corsProperties.getAllowedMethods(),
                corsProperties.isAllowCredentials(),
                corsProperties.getMaxAge());
    }
}
