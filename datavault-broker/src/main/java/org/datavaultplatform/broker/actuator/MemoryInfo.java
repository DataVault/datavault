package org.datavaultplatform.broker.actuator;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MemoryInfo {

  private final Map<String, Object> memDetails;

  public MemoryInfo(Map<String, Object> memDetails) {
    this.memDetails = memDetails;
  }

  @JsonAnyGetter
  public Map<String, Object> getMemDetails() {
    return this.memDetails;
  }
}

