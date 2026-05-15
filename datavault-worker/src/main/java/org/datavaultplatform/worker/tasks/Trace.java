package org.datavaultplatform.worker.tasks;

import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.worker.tasks.deposit.*;

/**
 * A class that extends Task which is used to simply log the current traceId
 * This is used to check the propagation of the traceId from the broker.
 */
@Slf4j
public class Trace extends Task {

    @Override
    public void performAction(Context context) {
        DepositUtils.initialLogging(context);

        // the brokerTraceId is set in the message properties
        String brokerTraceId = this.getProperties().get("brokerTraceId");
        log.info("BrokerTraceId: {}", brokerTraceId);

        // This works anywhere on the same thread as the request
        Span otelSpan = Span.current();
        String traceId = otelSpan.getSpanContext().getTraceId();
        log.info("WorkerTraceId: {}", traceId);

    }
}