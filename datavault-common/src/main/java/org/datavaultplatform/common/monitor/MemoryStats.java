package org.datavaultplatform.common.monitor;

import lombok.Builder;
import lombok.Data;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

@Data
@Builder
public class MemoryStats {

  private long totalMemory;
  private long freeMemory;
  private long maxMemory;

  public static MemoryStats getCurrent() {
    Runtime rt = Runtime.getRuntime();
    MemoryStats stats = MemoryStats.builder()
        .freeMemory(rt.freeMemory())
        .maxMemory(rt.maxMemory())
        .totalMemory(rt.totalMemory())
        .build();
    return stats;
  }

  public String toPretty() {
    String prettyFree = humanReadableByteCountSI(freeMemory);
    String prettyMax = humanReadableByteCountSI(maxMemory);
    String prettyTotal = humanReadableByteCountSI(totalMemory);
    String result = String.format("MemoryStats(totalMemory=%s, freeMemory=%s, maxMemory=%s)",prettyTotal, prettyFree, prettyMax);
    return result;
  }

  public static String humanReadableByteCountSI(long bytes) {
    if (-1000 < bytes && bytes < 1000) {
      return bytes + " B";
    }
    CharacterIterator ci = new StringCharacterIterator("kMGTPE");
    while (bytes <= -999_950 || bytes >= 999_950) {
      bytes /= 1000;
      ci.next();
    }
    return String.format("%.1f %cB", bytes / 1000.0, ci.current());
  }
}
