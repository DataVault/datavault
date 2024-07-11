package org.datavaultplatform.worker.rabbit;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.task.TaskInterrupter;
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

    private TaskInterrupter.Checker checker;

    
    private final ScheduledMonitor monitor;
    
    public RabbitMessageSelectorScheduler(
            @Value(SPEL_DELAY_MS) long selectorMessageDelayMs,
            RabbitMessageSelector selector,
            ScheduledMonitor monitor){
        this.selector = selector;
        this.selectorMessageDelayMs = selectorMessageDelayMs;
        this.monitor = monitor;
    }
    
    @SuppressWarnings("DefaultAnnotationParam")
    @Scheduled(fixedDelayString = SPEL_DELAY_MS, timeUnit = TimeUnit.MILLISECONDS)
    @SneakyThrows
    public synchronized void selectAndProcessNextMessage() {
        log.info("SelectorMessageDelayMs [{}]", selectorMessageDelayMs);
        monitor.monitor(this);
        if (!ready.get()) {
            return;
        }
        try {
            TaskInterrupter.setInterrupterCheck(checker);
            selector.selectAndProcessNextMessage();
        } catch (Exception ex) {
            monitor.error(this, ex);
        } finally {
            TaskInterrupter.setInterrupterCheck(null);
        }
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
    
    public synchronized void setChecker(TaskInterrupter.Checker checker) {
        this.checker = checker;
    }
}
