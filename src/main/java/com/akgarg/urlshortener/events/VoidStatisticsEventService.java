package com.akgarg.urlshortener.events;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VoidStatisticsEventService implements StatisticsEventService {

    @Override
    public void publishEvent(final StatisticsEvent statisticsEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Publishing '{}' event: {}", statisticsEvent.eventType().name(), statisticsEvent);
        }
    }

}
