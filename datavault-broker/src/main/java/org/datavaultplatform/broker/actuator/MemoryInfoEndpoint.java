package org.datavaultplatform.broker.actuator;

import org.datavaultplatform.common.monitor.MemoryStats;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Endpoint(id="memoryinfo")
public class MemoryInfoEndpoint {
  public static final String MEMORY = "memory";
  public static final String TIMESTAMP = "timestamp";

  public static final String MEMORY_FREE = "free";
  public static final String MEMORY_MAX = "max";
  public static final String MEMORY_TOTAL = "total";

  private final Clock clock;
  public MemoryInfoEndpoint(Clock clock){
    this.clock = clock;
  }
  @ReadOperation
  public MemoryInfo memoryInfo() {
    Map<String,Object> inner = new LinkedHashMap<>();
    MemoryStats stats = MemoryStats.getCurrent();
    inner.put(MEMORY_FREE, MemoryStats.humanReadableByteCountSI(stats.getFreeMemory()));
    inner.put(MEMORY_TOTAL, MemoryStats.humanReadableByteCountSI(stats.getTotalMemory()));
    inner.put(MEMORY_MAX, MemoryStats.humanReadableByteCountSI(stats.getMaxMemory()));

    Map<String, Object> outer = new LinkedHashMap<>();

    ZonedDateTime zdt = clock.instant().atZone(ZoneOffset.UTC);
    String timestamp = zdt.format(DateTimeFormatter.ISO_INSTANT);

    outer.put(TIMESTAMP, timestamp);
    outer.put(MEMORY, inner);

    MemoryInfo info = new MemoryInfo(outer);
    return info;
  }

}