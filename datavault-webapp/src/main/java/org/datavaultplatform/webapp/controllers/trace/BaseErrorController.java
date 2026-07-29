package org.datavaultplatform.webapp.controllers.trace;

import io.micrometer.tracing.TraceContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.config.trace.TraceLoggingFilter;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;

@Slf4j
public abstract class BaseErrorController {

    public static final String MESSAGE_PATTERN = "Please report the Trace Id [%s] to support.";
    
    protected String getExceptionMessage(Throwable throwable, HttpStatus httpStatus) {
        if (throwable != null) {
            StringWriter reason = new StringWriter();
            throwable.printStackTrace(new PrintWriter(reason));
            return reason.toString();
        } else {
            return httpStatus.getReasonPhrase();
        }
    }

    protected String getMessage(String requestUri, HttpStatus httpStatus, String exceptionMessage, String traceId, boolean outputTraceIdOnError) {
        if (requestUri == null) {
            requestUri = "Unknown";
        }

        String fullMessage = MessageFormat.format("Error code {0} returned for {1} with message:<br/> {2}",
                httpStatus.value(), requestUri, exceptionMessage);

        log.error(fullMessage);

        final String message;
        if (outputTraceIdOnError) {
            message = MESSAGE_PATTERN.formatted(traceId);
        } else if (showFullErrorMessageWhenNotDisplayingTraceId()) {
            message = fullMessage;
        } else {
            message = "";
        }
        return message;
    }

    protected void extraDebug(HttpServletRequest request) {
        Collections
                .list(request.getAttributeNames())
                .stream()
                .filter(Objects::nonNull)
                .filter(aName -> aName.startsWith("jakarta.servlet.error."))
                .forEach(aName -> log.error("error attr [{}] -> [{}]", aName, request.getAttribute(aName)));
    }

    protected HttpStatus getHttpStatus(Integer statusCode, HttpStatus defaultHttpStatus) {
        if (statusCode == null) {
            return defaultHttpStatus;
        } else {
            return HttpStatus.valueOf(statusCode);
        }
    }

    protected abstract boolean showFullErrorMessageWhenNotDisplayingTraceId();

    protected String withinTraceContext(ServletRequest request, BiFunction<String, String, String> consumer) {
        TraceContext traceContext = TraceLoggingFilter.getTraceContext(request);
        String traceId = traceContext.traceId();
        String spanId = traceContext.spanId();

        try (var ignored1 = MDC.putCloseable("traceId", traceId);
             var ignored2 = MDC.putCloseable("spanId", spanId)) {

            return consumer.apply(traceId, spanId);

        }
    }
}
