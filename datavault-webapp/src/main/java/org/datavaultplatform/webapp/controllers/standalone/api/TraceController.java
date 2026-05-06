package org.datavaultplatform.webapp.controllers.standalone.api;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.TraceInfo;
import org.datavaultplatform.webapp.services.TraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trace")
@Profile("database") //need to get to database
@ConditionalOnBean(TraceService.class)
@Slf4j
public class TraceController {

  private final Tracer tracer;
  private final TraceService traceService;
  
  @Autowired
  public TraceController(Tracer tracer, TraceService traceService){
    this.tracer = tracer;
    this.traceService = traceService;
  }
  
  @GetMapping("/info")
  public TraceInfo getTraceInfo() {
    String traceId = tracer.currentSpan().context().traceId();
    log.info("TraceId: {}", traceId);
    return new TraceInfo(traceId);
  }

  @GetMapping("/broker/info")
  public TraceInfo getTraceBrokerInfo() {
    String traceId = tracer.currentSpan().context().traceId();
    log.info("Actual TraceId: {}", traceId);
    return traceService.getTraceInfoFromBroker();
  }

  @GetMapping("/broker/worker/info")
  public TraceInfo getTraceBrokerInfoAndSendToWorker() {
    String traceId = tracer.currentSpan().context().traceId();
    log.info("TraceId: {}", traceId);
    return traceService.getTraceInfoFromBrokerAndSendToWorker();
  }

}
