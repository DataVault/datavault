package org.datavaultplatform.webapp.config.ratelimited;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

public class MutableTestClock extends Clock {
    private final Instant startInstant; // Our "zero" marker for nanos
    private final AtomicReference<Instant> instantRef;
    private final ZoneId zone = ZoneId.of("UTC");

    public MutableTestClock(Instant initialInstant) {
        this.startInstant = initialInstant;
        this.instantRef = new AtomicReference<>(initialInstant);
    }

    public void setInstant(Instant instant) {
        this.instantRef.set(instant);
    }

    public void fastForward(Duration duration) {
        this.instantRef.updateAndGet(current -> current.plus(duration));
    }

    /**
     * Calculates nanoseconds elapsed since this clock was started.
     * Prevents long overflow bugs in Caffeine's Ticker.
     */
    public long getElapsedNanos() {
        Duration elapsed = Duration.between(startInstant, instantRef.get());
        return elapsed.toNanos();
    }

    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return instantRef.get(); }
}