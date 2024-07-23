package org.datavaultplatform.common.event.deposit;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Map;

public interface ChunksDigestEvent {
  @JsonGetter
  Map<Integer, String> getChunksDigest();

  @JsonGetter
  String getDigestAlgorithm();
}
