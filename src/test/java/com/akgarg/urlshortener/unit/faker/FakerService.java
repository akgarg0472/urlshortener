package com.akgarg.urlshortener.unit.faker;

import com.akgarg.urlshortener.statistics.EventType;
import com.akgarg.urlshortener.statistics.StatisticsEvent;
import com.akgarg.urlshortener.url.v1.UrlMetadata;
import com.github.javafaker.Faker;

import java.util.concurrent.TimeUnit;

public class FakerService {

    private static final Faker faker = new Faker();

    public static StatisticsEvent fakeStatisticsEvent() {
        final var requestId = faker.internet().uuid().replace("-", "");
        final var shortUrl = faker.lorem().characters(7, true);
        final var originalUrl = faker.internet().url();
        final var userId = faker.internet().uuid().replace("-", "");
        final var ipAddress = faker.internet().ipV4Address();
        final var userAgent = faker.internet().userAgentAny();
        final var createdAt = faker.date().past(10_000, TimeUnit.MILLISECONDS).getTime();
        final var eventDuration = System.currentTimeMillis() - createdAt;
        final var eventType = faker.options().option(EventType.class);

        return new StatisticsEvent(
                requestId,
                eventType,
                shortUrl,
                originalUrl,
                userId,
                ipAddress,
                userAgent,
                createdAt,
                eventDuration
        );
    }

    public static UrlMetadata fakeUrlMetadata() {
        return new UrlMetadata(
                "O9Oz9L1",
                "https://www.google.com",
                "b34ed1400fd06ef21f",
                System.currentTimeMillis()
        );
    }

}
