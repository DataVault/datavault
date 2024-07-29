package org.datavaultplatform.worker.rabbit;

import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class ScheduledMonitor {
    private final Logger log;
    private final AtomicLong counter = new AtomicLong();

    public ScheduledMonitor(Logger log) {
        this.log = log;
    }
    
    public void monitor(Object context) {
        log.info("Scheduled Task [{}] Invoked  count=[{}]", context.getClass().getSimpleName(), counter.incrementAndGet());
    }

    public void error(Object context, Exception ex) {
        log.error("Scheduled Task [{}] Error", context.getClass().getSimpleName(), ex);
    }
}

