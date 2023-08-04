package com.akgarg.urlshortener.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.ParameterizedMessage;

public final class UrlLogger {

    private final Class<?> clazz;

    private UrlLogger(final Class<?> clazz) {
        this.clazz = clazz;
    }

    public static UrlLogger getLogger(final Class<?> clazz) {
        return new UrlLogger(clazz);
    }

    public void info(final String message, final Object... params) {
        log(Level.INFO, message, params);
    }

    public void error(final String message, final Object... params) {
        log(Level.ERROR, message, params);
    }

    public void debug(final String message, final Object... params) {
        log(Level.DEBUG, message, params);
    }

    public void trace(final String message, final Object... params) {
        log(Level.TRACE, message, params);
    }

    private void log(
            final Level level,
            final String message,
            final Object... params
    ) {
        final var parameterizedMessage = new ParameterizedMessage(message, params);
        LogManager.getLogger(clazz).log(level, parameterizedMessage);
    }

}
