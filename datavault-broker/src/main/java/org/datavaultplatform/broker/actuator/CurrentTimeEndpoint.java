package org.datavaultplatform.broker.actuator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import org.datavaultplatform.common.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id="customtime")
public class CurrentTimeEndpoint {

  private final Clock clock;

  @Autowired
  public CurrentTimeEndpoint(Clock clock) {
    this.clock = clock;
  }

  @ReadOperation
  public CurrentTime currentTime() {
    DateFormat df = new SimpleDateFormat(DateTimeUtils.VERBOSE_DATE_TIME_FORMAT);
    df.setTimeZone(TimeZone.getTimeZone(clock.getZone()));
    Map<String, Object> details = new LinkedHashMap<>();
    long ts = clock.millis();
    details.put("current-time", df.format(new Date(ts)));
    CurrentTime health = new CurrentTime(details);
    return health;
  }

}