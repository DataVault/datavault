package org.datavaultplatform.webapp.config.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;

public class TraceLoggingFilter implements Filter {

    public static final String TRACE_CONTEXT = "traceContext";
    private static final Logger LOGGER = LoggerFactory.getLogger(TraceLoggingFilter.class);

    private final Tracer tracer;

    public TraceLoggingFilter(Tracer tracer) {
        this.tracer = tracer;
        Assert.notNull(tracer, "Tracer must not be null");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Only process for initial REQUEST dispatches.
        // For ERROR/FORWARD dispatches, the trace context should already be in the ThreadLocal
        // from the initial REQUEST, or we don't want to interfere.
        if (httpRequest.getDispatcherType() != DispatcherType.REQUEST) {
            chain.doFilter(request, response);
            return;
        }

        // Skip static resources to keep logs cleaner, similar to the original interceptor
        if (httpRequest.getRequestURI().startsWith("/resources")) {
            chain.doFilter(request, response);
            return;
        }
        Span currentSpan = tracer.currentSpan();
        TraceContext traceContext = currentSpan == null ? TraceContext.NOOP : currentSpan.context();
        request.setAttribute(TRACE_CONTEXT, traceContext);
        LOGGER.info("XXX in filter, TraceId [{}] for URI: {}", traceContext.traceId(), httpRequest.getRequestURI());

        chain.doFilter(request, response);
    }

    public static TraceContext getTraceContext(ServletRequest request) {
        return (TraceContext) request.getAttribute(TRACE_CONTEXT);
    }
}
