package org.datavaultplatform.webapp.actuator;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record MemoryInfo(Map<String, Object> memDetails) {

  @Override
  @JsonAnyGetter
  public Map<String, Object> memDetails() {
    return this.memDetails;
  }
}

