package org.datavaultplatform.common.util;

import org.springframework.util.StringUtils;

/**
 * These are the property names that are used to pass w3c trace information via http headers and rabbit headers.
 * They are in "io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator" but not public.
 * @see <a href="https://www.w3.org/TR/trace-context/">w3c trace context standard that OTEL uses</a>
 */
public final class TraceUtils {

    public static final String TRACE_PARENT = "traceparent";
    public static final String TRACE_STATE = "tracestate";
    public static final String ANONYMOUS = "anonymous";
    
    private TraceUtils() {
    }

    public static String getMdcUserName(String username) {
        if (StringUtils.hasText(username)) {
            return username;
        } else {
            return ANONYMOUS;
        }
    }
}
