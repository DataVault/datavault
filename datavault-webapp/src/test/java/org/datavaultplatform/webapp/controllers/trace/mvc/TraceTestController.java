package org.datavaultplatform.webapp.controllers.trace.mvc;

import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("trace")
class TraceTestController {

    private final Tracer tracer;

    TraceTestController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping("/trace-test")
    public String test() {
        return tracer.currentSpan().context().traceId();
    }
}
