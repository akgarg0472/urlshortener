package com.akgarg.urlshortener.unit.faker;

import com.akgarg.urlshortener.statistics.StatisticsEvent;
import com.github.javafaker.Faker;

import java.util.concurrent.TimeUnit;

public class FakerService {

    private static final Faker faker = new Faker();

    public static StatisticsEvent getFakeStatisticsEvent() {
        final var requestId = faker.internet().uuid().replace("-", "");
        final var shortUrl = faker.lorem().characters(7, true);
        final var originalUrl = faker.internet().url();
        final var userId = faker.internet().uuid().replace("-", "");
        final var ipAddress = faker.internet().ipV4Address();
        final var userAgent = faker.internet().userAgentAny();
        final var createdAt = faker.date().past(10_000, TimeUnit.MILLISECONDS).getTime();
        final var eventDuration = System.currentTimeMillis() - createdAt;

        return new StatisticsEvent(
                requestId,
                shortUrl,
                originalUrl,
                userId,
                ipAddress,
                userAgent,
                createdAt,
                eventDuration
        );
    }

}
