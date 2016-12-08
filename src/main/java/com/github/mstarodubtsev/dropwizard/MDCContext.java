package com.github.mstarodubtsev.dropwizard;

import org.apache.log4j.MDC;

public class MDCContext implements AutoCloseable {

    private final String key;

    public MDCContext(String key, String value) {
        this.key = key;
        MDC.put(key, value);
    }

    @Override
    public void close() throws Exception {
        MDC.remove(key);
    }

}
