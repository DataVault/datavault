package org.datavaultplatform.worker.config;

import org.datavaultplatform.common.config.BasePropertiesConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@ConditionalOnExpression("${worker.properties.enabled:true}")
public class PropertiesConfig extends BasePropertiesConfig {
}
