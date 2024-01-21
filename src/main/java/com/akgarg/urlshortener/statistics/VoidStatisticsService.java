package com.akgarg.urlshortener.statistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VoidStatisticsService implements StatisticsService {

    private static final Logger LOGGER = LogManager.getLogger(VoidStatisticsService.class);

    @Override
    public void publishEvent(final StatisticsEvent statisticsEvent) {
        LOGGER.info("Publishing '{}' event: {}", statisticsEvent.eventType().name().toLowerCase(), statisticsEvent);
    }
    
}
