package org.datavaultplatform.webapp.config.trace;

import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@Configuration
@Import(MdcRequestFilter.class)
public class TracingConfig {

    @Bean
    ContextPropagators otelContextPropagators() {
        return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }

    @Bean
    TraceLoggingFilter traceLoggingFilter(Tracer tracer) {
        return new TraceLoggingFilter(tracer);
    }

    @Bean
    MdcRequestFilter userMdcFilter() {
        return new MdcRequestFilter();
    }

    @Bean
    MdcRestorationFilter mdcRestorationFilter() {
        return new MdcRestorationFilter();
    }
    
    @Bean
    public FilterRegistrationBean<MdcRestorationFilter> mdcRestorationFilterRegistration(MdcRestorationFilter filter) {
        FilterRegistrationBean<MdcRestorationFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(filter);
        registration.addUrlPatterns("/*");

        // This is the "magic" that makes it work for Error/Forward dispatches
        registration.setDispatcherTypes(
                DispatcherType.REQUEST,
                DispatcherType.FORWARD,
                DispatcherType.ERROR,
                DispatcherType.ASYNC
        );

        // Ensure this runs BEFORE the FilterChainProxy (Spring Security)
        // Spring Security usually sits at -100
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}
