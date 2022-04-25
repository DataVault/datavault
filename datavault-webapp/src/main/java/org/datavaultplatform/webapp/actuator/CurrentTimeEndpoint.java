package org.datavaultplatform.webapp.actuator;

import java.time.Clock;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

@Endpoint(id="customtime")
public class CurrentTimeEndpoint {

  private final Clock clock;

  @Autowired
  public CurrentTimeEndpoint(Clock clock) {
    this.clock = clock;
  }

  @ReadOperation
  public CurrentTime currentTime() {
    Map<String, Object> details = new LinkedHashMap<>();
    long ts = clock.millis();
    details.put("current-time", new Date(ts).toString());
    CurrentTime health = new CurrentTime(details);
    return health;
  }

}