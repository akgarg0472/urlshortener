package com.akgarg.urlshortener.numbergenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Profile("dev")
@Service
public class LocalInMemoryNumberGenerator implements NumberGeneratorService {

    private static final Logger LOGGER = LogManager.getLogger(LocalInMemoryNumberGenerator.class);
    private final AtomicLong counter = new AtomicLong(1_00_00_00_000L);

    @Override
    public long generateNextNumber() {
        LOGGER.debug("Generating globally unique number");

        final long number = counter.getAndIncrement();

        LOGGER.trace("Globally unique number generated: {}", number);

        return number;
    }

}
