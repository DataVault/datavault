package org.datavaultplatform.worker.rabbit;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RabbitMessageSelectorScheduler {
    
    public static final String SPEL_DELAY_MS = "${worker.next.message.selector.delay.ms:10000}";

    private final AtomicBoolean ready = new AtomicBoolean(false);

    private final RabbitMessageSelector selector;
    private final long selectorMessageDelayMs;

    public RabbitMessageSelectorScheduler(@Value(SPEL_DELAY_MS) long selectorMessageDelayMs, RabbitMessageSelector selector){
        this.selector = selector;
        this.selectorMessageDelayMs = selectorMessageDelayMs;
    }
    
    @SuppressWarnings("DefaultAnnotationParam")
    @Scheduled(fixedDelayString = SPEL_DELAY_MS, timeUnit = TimeUnit.MILLISECONDS)
    @SneakyThrows
    public void selectAndProcessNextMessage() {
        if (!ready.get()) {
            return;
        }
        selector.selectAndProcessNextMessage();
    }

    @EventListener(ApplicationReadyEvent.class)
    protected void onReady(ApplicationReadyEvent event) {
        this.ready.set(true);
    }
    
    public void disable() {
        this.ready.set(false);
    }

    @SuppressWarnings("unused")
    public boolean isReady() {
        return this.ready.get();
    }
    
    @PostConstruct
    void init() {
        log.info("Worker Scheduler : worker.next.message.selector.delay.ms[{}]", selectorMessageDelayMs);
    }
}
