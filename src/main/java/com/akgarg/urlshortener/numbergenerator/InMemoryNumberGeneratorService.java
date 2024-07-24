package com.akgarg.urlshortener.numbergenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("dev")
@Service
public class InMemoryNumberGeneratorService implements NumberGeneratorService {

    private static final Logger LOGGER = LogManager.getLogger(InMemoryNumberGeneratorService.class);

    @Override
    public long generateNextNumber() {
        LOGGER.debug("Generating globally unique number");

        final long number = System.currentTimeMillis();

        LOGGER.trace("Globally unique number generated: {}", number);

        return number;
    }

}
