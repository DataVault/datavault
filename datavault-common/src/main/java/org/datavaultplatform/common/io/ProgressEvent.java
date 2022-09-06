package org.datavaultplatform.common.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgressEvent {
  private final ProgressEventType type;
  private final long value;
}
