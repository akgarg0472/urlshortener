package com.akgarg.urlshortener.statistics;

import com.akgarg.urlshortener.utils.UrlLogger;

public class VoidStatisticsService implements StatisticsService {

    private static final UrlLogger LOGGER = UrlLogger.getLogger(VoidStatisticsService.class);

    @Override
    public void publishEvent(final StatisticsEvent statisticsEvent) {
        LOGGER.info("Publishing '{}' event: {}", statisticsEvent.eventType().name().toLowerCase(), statisticsEvent);
    }

}
