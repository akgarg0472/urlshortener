package com.akgarg.urlshortener.numbergenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemoryNumberGeneratorService implements NumberGeneratorService {

    @Override
    public long generateNextNumber() {
        log.debug("Generating globally unique number");
        final var number = System.currentTimeMillis();
        log.trace("Globally unique number generated: {}", number);
        return number;
    }

}
