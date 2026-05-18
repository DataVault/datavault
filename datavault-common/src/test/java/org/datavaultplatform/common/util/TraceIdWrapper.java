package org.datavaultplatform.common.util;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.trace.TraceId;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class TraceIdWrapper {
    private final String traceId;
    private final Tracer tracer;
    private Span testSpan;
    private Tracer.SpanInScope testScope;

    public TraceIdWrapper(String traceId, Tracer tracer) {
        this.traceId = traceId;
        this.tracer = tracer;
    }

    public void runWithinWrapper(Runnable runnable) {
        setupTestTraceId(traceId);
        try (Tracer.SpanInScope ignored = tracer.withSpan(testSpan)) {
            runnable.run();
        } finally {
            tearDownTestSpan();
        }
    }
    public <T> T runWithinWrapper(Callable<T> callable) throws Exception {
        setupTestTraceId(traceId);
        try (Tracer.SpanInScope ignored = tracer.withSpan(testSpan)) {
            return callable.call();
        } finally {
            tearDownTestSpan();
        }
    }

    final void tearDownTestSpan() {
        if (this.testScope != null) {
            this.testScope.close();
        }
        if (this.testSpan != null) {
            this.testSpan.end();
        }
    }

    private void setupTestTraceId(String testTraceId) {
        if (testTraceId == null) {
            return;
        }
        String spanId = "00f067aa0ba902b7";

        assertThat(TraceId.isValid(testTraceId)).isTrue();
        TraceContext context = tracer.traceContextBuilder()
                .traceId(testTraceId)
                .spanId(spanId)
                .sampled(true)
                .build();
        testSpan = tracer.spanBuilder().setParent(context).start();
        testScope = tracer.withSpan(testSpan);
    }

}
