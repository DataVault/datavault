package org.datavaultplatform.webapp.controllers.trace;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.datavaultplatform.webapp.controllers.trace.mvc.TraceIdDemoControllerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TraceIdDemoControllerConfig.class)
@ActiveProfiles("trace")
public abstract class BaseTraceIdDemoControllerTest {

    @Autowired
    Tracer tracer;

    @Autowired
    Propagator propagator;
    
    @Autowired
    TestRestTemplate restTemplate;

    @BeforeEach
    void setup() {
        assertThat(tracer).isNotNull();
        assertThat(propagator).isNotNull();
    }

}
