package com.akgarg.urlshortener.statistics;

import com.akgarg.urlshortener.utils.UrlLogger;
import org.springframework.stereotype.Service;

@Service
public class VoidStatisticsService implements StatisticsService {

    private static final UrlLogger LOGGER = UrlLogger.getLogger(VoidStatisticsService.class);

    @Override
    public void publishEvent(final StatisticsEvent statisticsEvent, final EventType eventType) {
        LOGGER.info("[{}] Publishing statistics event to void: {}", eventType, statisticsEvent);
    }

}
