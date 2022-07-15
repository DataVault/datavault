package org.datavaultplatform.webapp.app.actuator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import lombok.SneakyThrows;
import org.datavaultplatform.webapp.actuator.CurrentTime;
import org.datavaultplatform.webapp.actuator.CurrentTimeEndpoint;
import org.junit.jupiter.api.Test;

public class CurrentTimeEndpointTest {

  ObjectMapper mapper = new ObjectMapper();

  @Test
  void testCurrentTimeGMT() {

    Clock clockGMT = Clock.fixed(
        Instant.parse("2022-12-21T13:15:16.101Z"),
        ZoneId.of("Europe/London"));

    String resultGMT = getTimeString(clockGMT);
    assertEquals("{\"current-time\":\"Wed Dec 21 13:15:16 GMT 2022\"}", resultGMT);
  }

  @Test
  void testCurrentTimeBST() {

    Clock clockBST = Clock.fixed(
        Instant.parse("2022-07-31T13:15:16.101Z"),
        ZoneId.of("Europe/London"));

    String resultGMT = getTimeString(clockBST);
    assertEquals("{\"current-time\":\"Sun Jul 31 14:15:16 BST 2022\"}", resultGMT);
  }

  @SneakyThrows
  private String getTimeString(Clock clock) {
    CurrentTimeEndpoint endpoint = new CurrentTimeEndpoint(clock);
    CurrentTime time = endpoint.currentTime();
    return mapper.writeValueAsString(time);
  }

}
