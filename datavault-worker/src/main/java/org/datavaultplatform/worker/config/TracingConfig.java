package org.datavaultplatform.worker.config;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    ContextPropagators otelContextPropagators() {
        return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }
}
