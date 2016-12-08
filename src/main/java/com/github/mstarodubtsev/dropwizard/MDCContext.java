package com.github.mstarodubtsev.dropwizard;

import org.apache.log4j.MDC;

public class MDCContext implements AutoCloseable {

    String key;
    
    public MDCContext(String key, String value) {
        MDC.put(key, value);
    }
    
    @Override
    public void close() throws Exception {
        MDC.remove(key);
    }

}
