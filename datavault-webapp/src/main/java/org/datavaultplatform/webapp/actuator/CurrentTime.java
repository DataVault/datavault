package org.datavaultplatform.webapp.actuator;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CurrentTime {

  private final Map<String, Object> timeDetails;

  public CurrentTime(Map<String, Object> timeDetails) {
    this.timeDetails = timeDetails;
  }

  @JsonAnyGetter
  public Map<String, Object> getTimeDetails() {
    return this.timeDetails;
  }
}

