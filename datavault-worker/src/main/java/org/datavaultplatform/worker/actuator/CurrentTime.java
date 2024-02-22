package org.datavaultplatform.worker.actuator;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CurrentTime(Map<String, Object> timeDetails) {

  @Override
  @JsonAnyGetter
  public Map<String, Object> timeDetails() {
    return this.timeDetails;
  }
}

