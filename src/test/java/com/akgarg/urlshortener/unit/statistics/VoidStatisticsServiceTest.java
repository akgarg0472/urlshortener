package com.akgarg.urlshortener.unit.statistics;

import com.akgarg.urlshortener.statistics.EventType;
import com.akgarg.urlshortener.statistics.VoidStatisticsService;
import com.akgarg.urlshortener.unit.faker.FakerService;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class VoidStatisticsServiceTest {

    @Test
    void publishEvent_ShouldLogEventDetails() {
        final var voidStatisticsService = new VoidStatisticsService();

        final var statisticsEvent = FakerService.fakeStatisticsEvent();
        final var eventType = EventType.values()[new Random().nextInt(EventType.values().length)];

        assertNotNull(statisticsEvent, "Statistics event is null");

        voidStatisticsService.publishEvent(statisticsEvent, eventType);
    }

}
