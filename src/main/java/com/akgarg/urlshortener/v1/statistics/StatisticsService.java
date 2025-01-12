package com.akgarg.urlshortener.v1.statistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {


    private final RestClient.Builder statisticsServiceRestClientBuilder;
    private final Environment environment;

    public int getCurrentCustomAliasUsageForUser(final String requestId, final String userId, final long startTime, final long endTime) {
        log.info("[{}] Getting current custom alias usage for user {}", requestId, userId);
        final var shortUrlQueryParam = environment.getProperty("statistics.service.usage.query-param.short-url", "shortUrl");
        return query(requestId, userId, startTime, endTime, shortUrlQueryParam);
    }

    public int getCurrentShortUrlUsageForUser(final String requestId, final String userId, final long startTime, final long endTime) {
        log.info("[{}] Getting current short url usage for user {}", requestId, userId);
        final var customAliasQueryParam = environment.getProperty("statistics.service.usage.query-param.custom_alias", "customAlias");
        return query(requestId, userId, startTime, endTime, customAliasQueryParam);
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
            log.error("[{}] statistics query failed", requestId, e);
            return 0;
        }
    }

}
