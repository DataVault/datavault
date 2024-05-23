package org.datavaultplatform.worker.rabbit;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public class RabbitMessageSelector implements ApplicationContextAware {

    private final RabbitMessageProcessor processor;
    private final RabbitQueuePoller hiPriorityPoller;
    private final RabbitQueuePoller loPriorityPoller;
    private ApplicationContext ctx;

    public RabbitMessageSelector(RabbitQueuePoller hiPriorityPoller, RabbitQueuePoller loPriorityPoller, RabbitMessageProcessor processor) {
        this.processor = processor;
        this.hiPriorityPoller = hiPriorityPoller;
        this.loPriorityPoller = loPriorityPoller;
    }
    
    public static <T> Optional<T> getFirst(Supplier<Optional<T>> hi, Supplier<Optional<T>> lo) {
        return Stream.of(hi, lo).map(Supplier::get).flatMap(Optional::stream).findFirst();
    }

    public synchronized void selectAndProcessNextMessage() {

        // it only looks on loPriorityQueue is no messages on hiPriorityQueue
        Optional<RabbitMessageInfo> selected = getFirst(hiPriorityPoller::poll, loPriorityPoller::poll);

        selected.ifPresent(processor::onMessage);
    }
    
    @PostConstruct
    public void init() {
        if (ctx == null) {
            log.warn("No Application Context Set!");
            return;
        }
        String appName = ctx.getEnvironment().getProperty("spring.application.name","spring.application.name not set!");
        log.info("Worker [{}] Restart Queue [{}]", appName, this.hiPriorityPoller.getQueueName());
        log.info("Worker [{}] Worker  Queue [{}]", appName, this.loPriorityPoller.getQueueName());
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
