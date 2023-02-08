package org.datavaultplatform.worker.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.monitor.MemoryStats;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Slf4j
@ConditionalOnExpression("${worker.memory.enabled:true}")
public class ScheduledTaskConfig {

    @Scheduled(fixedRate = 60_000)
    public void scheduleFixedRateTask() {
        log.info("Worker:Memory[{}]", MemoryStats.getCurrent().toPretty());
    }

}