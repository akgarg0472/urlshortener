package com.akgarg.urlshortener.v1.statistics;

import com.akgarg.urlshortener.exception.StatisticsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static com.akgarg.urlshortener.utils.UrlShortenerUtil.REQUEST_ID_HEADER;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final RestClient.Builder statisticsServiceRestClientBuilder;
    private final Environment environment;

    public int getCurrentCustomAliasUsageForUser(final String requestId, final String userId, final long startTime, final long endTime) {
        log.info("Getting current custom alias usage for user {}", userId);
        return query(requestId, userId, startTime, endTime, "customAlias");
    }

    public int getCurrentShortUrlUsageForUser(final String requestId, final String userId, final long startTime, final long endTime) {
        log.info("Getting current short url usage for user {}", userId);
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
                    )
                    .header(REQUEST_ID_HEADER, requestId)
                    .retrieve()
                    .toEntity(StatisticsResponse.class)
                    .getBody();

            if (log.isDebugEnabled()) {
                log.debug("Statistics response: {}", statisticsResponse);
            }

            if (statisticsResponse == null || statisticsResponse.statusCode() != 200) {
                log.error("Statistics API query failed with status code {}", statisticsResponse != null ? statisticsResponse.statusCode() : null);
                return 0;
            }

            return statisticsResponse.value();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException iae && iae.getMessage().startsWith("Service Instance cannot be null")) {
                log.warn("Statistics service is unreachable");
                throw new StatisticsException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Failed to query usage data. Please try again later!");
            }

            if (e instanceof HttpServerErrorException httpServerErrorException) {
                log.warn("Statistics service responded with code: {} and body: {}",
                        httpServerErrorException.getStatusCode().value(),
                        httpServerErrorException.getResponseBodyAsString());
                throw new StatisticsException(httpServerErrorException.getStatusCode().value(), "Failed to query usage data. Please try again later!");
            }

            log.error("Statistics query failed", e);

            throw e;
        }
    }

}
