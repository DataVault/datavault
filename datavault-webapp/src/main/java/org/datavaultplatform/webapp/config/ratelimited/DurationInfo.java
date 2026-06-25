package org.datavaultplatform.webapp.config.ratelimited;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Comparator;

public record DurationInfo(Duration duration, DurationType type) implements Comparable<DurationInfo> {
    
    @Override
    public int compareTo(@NonNull DurationInfo other) {
        return Comparator.comparing(DurationInfo::type)
                .compare(this, other);
    }
}
