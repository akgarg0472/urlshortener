package com.akgarg.urlshortener.numbergenerator;

import com.akgarg.urlshortener.utils.UrlLogger;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class LocalInMemoryNumberGeneratorService implements NumberGeneratorService {

    private static final UrlLogger LOGGER = UrlLogger.getLogger(LocalInMemoryNumberGeneratorService.class);
    private final AtomicLong counter = new AtomicLong(1_00_00_00_000L);

    @Override
    public long generateNumber() {
        LOGGER.debug("Generating globally unique number");

        final long number = counter.getAndIncrement();

        LOGGER.trace("Globally unique number generated is: {}", number);

        return number;
    }

}
