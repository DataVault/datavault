package org.datavaultplatform.webapp.config.ratelimited;

import java.time.Clock;
import java.time.Instant;

public class ClockUtils {

    private ClockUtils() {
    }

    public static long toEpochNanos(Clock clock) {
        Instant instant = clock.instant();
        return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
    }

}
