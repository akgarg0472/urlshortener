package com.akgarg.urlshortener.statistics;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("dev")
@Service
@Slf4j
public class VoidStatisticsService implements StatisticsService {

    @Override
    public void publishEvent(final StatisticsEvent statisticsEvent) {
        log.info("Publishing '{}' event: {}", statisticsEvent.eventType().name(), statisticsEvent);
    }

}
