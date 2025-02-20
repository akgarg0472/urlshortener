package com.akgarg.urlshortener.numbergenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemoryNumberGeneratorService implements NumberGeneratorService {

    @Override
    public long generateNextNumber() {
        final var number = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Globally unique number generated: {}", number);
        }
        return number;
    }

}
