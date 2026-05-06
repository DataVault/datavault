package org.datavaultplatform.worker.rabbit;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelPropagator;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.micrometer.tracing.propagation.Propagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.TraceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("GrazieInspectionRunner")
@Slf4j
class RabbitTraceTest {

    static final String TEST_TRACE_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    static final String TEST_TRACEPARENT = "00-%s-1f0bd736979fd383-01".formatted(TEST_TRACE_ID);

    Propagator propagator;
    Tracer tracer;

    @BeforeEach
    void beforeEach() {
        W3CTraceContextPropagator w3c = io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator.getInstance();
        W3CTraceContextPropagator.getInstance();
        ContextPropagators otelPropagators =
                ContextPropagators.create(w3c);

        // Real OTel tracer
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().build();
        io.opentelemetry.api.trace.Tracer otelTracer =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(tracerProvider)
                        .build()
                        .getTracer("test");


        // Micrometer wrapper
        this.propagator = new OtelPropagator(otelPropagators, otelTracer);

        // Micrometer tracer wrapper
        this.tracer =
                new OtelTracer(otelTracer, new OtelCurrentTraceContext(), null);
    }

    private Span getTestSpan(boolean setFixedTraceId) {
        String body = "sample-message";
        MessageProperties props = new MessageProperties();
        props.setMessageId("1234");
        if (setFixedTraceId) {
            props.setHeader(TraceUtils.TRACE_PARENT, TEST_TRACEPARENT);
        }
        Message message = new Message(body.getBytes(StandardCharsets.UTF_8), props);
        return RabbitMessageSelector.getSpanWithTraceIdFromMessage(propagator, message, "test-span");
    }

    @Test
    void testExtractSpanAndTrace() {
        Span testSpan = getTestSpan(true);
        try(Tracer.SpanInScope ws = tracer.withSpan(testSpan)) {
            String traceId1 = testSpan.context().traceId();
            log.info("ACTUAL TRACEID {}", traceId1);
            String traceId2 = tracer.currentSpan().context().traceId();
            assertThat(traceId1).isEqualTo(traceId2);
            assertThat(traceId1).isEqualTo(TEST_TRACE_ID);
        }
        testSpan.end();
    }

    @Test
    void testNotExtractSpanAndTrace() {
        Span testSpan = getTestSpan(false);
        try(Tracer.SpanInScope ws = tracer.withSpan(testSpan)){
            String traceId1 = testSpan.context().traceId();
            log.info("ACTUAL TRACEID {}", traceId1);
            String traceId2 = tracer.currentSpan().context().traceId();
            assertThat(traceId1).isEqualTo(traceId2);
            assertThat(traceId1).isNotEqualTo(TEST_TRACE_ID);
        }
        testSpan.end();
    }
}
