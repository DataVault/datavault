package org.datavaultplatform.broker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${broker.controllers.enabled:true}")
@ComponentScan({
    "org.datavaultplatform.broker.controllers",
    "org.datavaultplatform.broker.exceptions"})
public class ControllerConfig {
}
