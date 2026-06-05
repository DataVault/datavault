package org.datavaultplatform.webapp.config.ratelimited;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;

import java.time.Duration;

public record BandwidthInfo(BandwidthType type, long capacity, DurationInfo refillPeriod, long refillUnit) {

    public BandwidthInfo {
        Duration refillDuration = refillPeriod.duration();
        if (refillDuration.isNegative() || refillDuration.isZero()) {
            throw new IllegalArgumentException("The refillPeriod must be greater than 0");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("The capacity value cannot be less than 0");
        }
        if (refillUnit <= 0) {
            throw new IllegalArgumentException("The refillUnit value must be greater than 0");
        }
    }

    @JsonIgnore
    public Bandwidth getBandwidth() {
        var refillStage = BandwidthBuilder.builder().capacity(capacity);
        var buildStage = switch (type) {
            case INTERVAL -> refillStage.refillIntervally(refillUnit, refillPeriod.duration());
            case GREEDY -> refillStage.refillGreedy(refillUnit, refillPeriod.duration());
        };
        return buildStage.build();
    }
    
    public DurationType getDurationType(){
        return refillPeriod.type();
    }

    public double getRefillRate() {
        return refillUnit / (double) refillPeriod.duration().toSeconds();
    }
}