package com.akgarg.urlshortener.unit.statistics;

import com.akgarg.urlshortener.statistics.VoidStatisticsEventService;
import com.akgarg.urlshortener.unit.faker.FakerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class VoidStatisticsEventServiceTest {

    @Test
    void publishEvent_ShouldLogEventDetails() {
        final var voidStatisticsService = new VoidStatisticsEventService();

        final var statisticsEvent = FakerService.fakeStatisticsEvent();

        assertNotNull(statisticsEvent, "Statistics event is null");

        voidStatisticsService.publishEvent(statisticsEvent);
    }

}
