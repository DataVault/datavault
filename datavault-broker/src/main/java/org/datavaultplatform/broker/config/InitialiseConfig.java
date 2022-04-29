package org.datavaultplatform.broker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${broker.initialise.enabled:true}")
@ComponentScan("org.datavaultplatform.broker.initialise")
public class InitialiseConfig {

}
