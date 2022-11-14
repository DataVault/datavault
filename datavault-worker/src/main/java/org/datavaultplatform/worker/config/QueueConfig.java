package org.datavaultplatform.worker.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.config.BaseQueueConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${worker.rabbit.enabled:true}")
@Slf4j
public class QueueConfig extends BaseQueueConfig {
}
