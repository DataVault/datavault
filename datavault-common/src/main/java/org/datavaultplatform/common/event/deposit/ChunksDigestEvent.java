package org.datavaultplatform.common.event.deposit;

import java.util.Map;

public interface ChunksDigestEvent {
  Map<Integer, String> getChunksDigest();
  String getDigestAlgorithm();
}
