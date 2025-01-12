package com.akgarg.urlshortener.v1.statistics;

import com.akgarg.urlshortener.exception.StatisticsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {


    private final RestClient.Builder statisticsServiceRestClientBuilder;
    private final Environment environment;

    public int getCurrentCustomAliasUsageForUser(final String requestId, final String userId, final long startTime, final long endTime) {
        log.info("[{}] Getting current custom alias usage for user {}", requestId, userId);
        return query(requestId, userId, startTime, endTime, "customAlias");
    }

    public int getCurrentShortUrlUsageForUser(final String requestId, final String userId, final long startTime, final long endTime) {
        log.info("[{}] Getting current short url usage for user {}", requestId, userId);
        return query(requestId, userId, startTime, endTime, "shortUrl");
    }

    private int query(final String requestId, final String userId, final long startTime, final long endTime, final String metricName) {
        final var path = environment.getProperty("statistics.service.usage.base-path", "/api/v1/statistics/usage");

        try {
            final var statisticsResponse = statisticsServiceRestClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("metricName", metricName)
                            .queryParam("userId", userId)
                            .queryParam("startTime", startTime)
                            .queryParam("endTime", endTime)
                            .build()
                    ).retrieve()
                    .toEntity(StatisticsResponse.class)
                    .getBody();

            log.info("[{}] statistics response: {}", requestId, statisticsResponse);

            if (statisticsResponse == null || statisticsResponse.statusCode() != 200) {
                log.warn("[{}] statistics query failed", requestId);
                return 0;
            }

            return statisticsResponse.value();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException iae && iae.getMessage().startsWith("Service Instance cannot be null")) {
                log.warn("[{}] statistics service is unreachable", requestId);
                throw new StatisticsException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Failed to query usage data. Please try again later!");
            }

            if (e instanceof HttpServerErrorException httpServerErrorException) {
                log.warn("[{}] statistics service responded with code: {} and body: {}",
                        requestId,
                        httpServerErrorException.getStatusCode().value(),
                        httpServerErrorException.getResponseBodyAsString());
                throw new StatisticsException(httpServerErrorException.getStatusCode().value(), "Failed to query usage data. Please try again later!");
            }

            log.error("[{}] statistics query failed", requestId, e);
            throw e;
        }
    }

}
