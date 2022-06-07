package org.datavaultplatform.broker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnExpression("${broker.database.enabled:true}")
@EnableJpaRepositories("org.datavaultplatform.common.model.dao")
@EntityScan({
    "org.datavaultplatform.common.event",
    "org.datavaultplatform.common.model"
})
public class DatabaseConfig {
}
