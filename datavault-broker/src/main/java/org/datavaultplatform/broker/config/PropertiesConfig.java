package org.datavaultplatform.broker.config;

import org.datavaultplatform.common.config.BasePropertiesConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@ConditionalOnExpression("${broker.properties.enabled:true}")
public class PropertiesConfig extends BasePropertiesConfig {
}
