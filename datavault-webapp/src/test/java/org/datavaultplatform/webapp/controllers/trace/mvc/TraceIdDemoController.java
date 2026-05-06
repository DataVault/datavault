package org.datavaultplatform.webapp.controllers.trace.mvc;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.datavaultplatform.common.util.TraceUtils;
import org.datavaultplatform.webapp.controllers.SimpleRestService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Profile("trace")
@Controller
public class TraceIdDemoController {

    private static final Logger log = LoggerFactory.getLogger(TraceIdDemoController.class);

    private final Tracer tracer;
    private final SimpleRestService restService;

    public TraceIdDemoController(Tracer tracer, SimpleRestService restService) {
        this.tracer = tracer;
        this.restService = restService;
    }

    @GetMapping("/page1")
    String getPageOne(HttpServletRequest request, HttpServletResponse response, ModelMap model){

        model.addAttribute("time", Instant.now());
        model.addAttribute("message", "the is page 1");

        String incomingTraceparent = request.getHeader(TraceUtils.TRACE_PARENT);
        log.info("Incoming traceparent header: {}", incomingTraceparent);

        String traceId = tracer.currentSpan().context().traceId();

        log.info("Trace ID from tracer.currentSpan(): {}", traceId);
        model.addAttribute("traceId", traceId);
        return "page1";
    }

    @GetMapping("/oops")
    String getErrorPage(HttpServletRequest request, HttpServletResponse response, ModelMap model){
        // Add current trace ID to the model
        String traceId = Optional.ofNullable(tracer.currentSpan())
                .map(span -> span.context().traceId())
                .orElse("no-trace-id");

        log.info("Trace ID from tracer.currentSpan(): [{}]", traceId);
        log.info("ThreadName IN CONTROLLER IS [{}]", Thread.currentThread().getName());

        throw new RuntimeException("oops");
    }

    @GetMapping("/call-downstream")
    @ResponseBody
    public ResponseEntity<String> callDownstream(HttpServletRequest request) {
        String traceId = tracer.currentSpan().context().traceId();
        log.info("/call-downstream: Starting request with traceId=[{}]", traceId);

        // Build the full URL for the downstream service
        String downstreamUrl = request.getRequestURL().toString().replace("/call-downstream", "/downstream");

        String response = restService.get(downstreamUrl);

        log.info("/call-downstream: Received response: {}", response);
        return ResponseEntity.ok("Called downstream service. Check logs for trace propagation.");
    }

    @GetMapping("/downstream")
    @ResponseBody
    public ResponseEntity<String> downstreamEndpoint(HttpServletRequest request) {
        String traceparentHeader = request.getHeader(TraceUtils.TRACE_PARENT);
        String traceId = tracer.currentSpan().context().traceId();

        log.info("/downstream: Received traceparent header: [{}]", traceparentHeader);
        log.info("/downstream: Current traceId from tracer: [{}]", traceId);

        return ResponseEntity.ok("Hello from downstream! traceId=" + traceId);
    }
}
