package com.github.mstarodubtsev.dropwizard;

import org.apache.log4j.MDC;

/**
 * RAII class for managing MDC key/value pairs for a given context/scope.
 */
public class MDCContext implements AutoCloseable {

    /**
     * The key to remove when closing.
     */
    private final String key;

    /**
     * Constructor - sets MDC key/value, remembers key for later.
     * @param key MDC map key
     * @param value MDC map value
     */
    public MDCContext(final String key, final String value) {
        this.key = key;
        MDC.put(key, value);
    }

    /**
     * Removes MDC key.
     */
    @Override
    public final void close() throws Exception {
        MDC.remove(key);
    }

}
