package com.rattatarr.rattatarr.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClientProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "Rattatarr/1.0")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
