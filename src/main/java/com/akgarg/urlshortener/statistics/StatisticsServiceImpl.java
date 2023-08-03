package com.akgarg.urlshortener.statistics;

import com.akgarg.urlshortener.logger.UrlLogger;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final UrlLogger LOGGER = UrlLogger.getLogger(StatisticsServiceImpl.class);

    @Override
    public void publishEvent(final StatisticsEvent statisticsEvent, final EventType eventType) {
        LOGGER.info("publishing event to statistics service for event type={}: {}", eventType, statisticsEvent);
    }

}
