package com.akgarg.urlshortener.configs;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientBuilderConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder statisticsServiceRestClientBuilder() {
        return RestClient.builder()
                .baseUrl("http://urlshortener-statistics-service");
    }

}
