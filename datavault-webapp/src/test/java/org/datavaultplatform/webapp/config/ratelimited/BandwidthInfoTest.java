package org.datavaultplatform.webapp.config.ratelimited;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BandwidthInfoTest {

    static Stream<Arguments> badDurationSource() {
        return Stream.of(Arguments.of(Duration.ZERO), Arguments.of(Duration.ofMinutes(-1)));
    }

    @ParameterizedTest
    @MethodSource("badDurationSource")
    void testBandwidthWithNonPositiveDuration(Duration badDuration) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new BandwidthInfo(BandwidthType.GREEDY, 123L, new DurationInfo(badDuration, DurationType.MINUTES), 123L);
        });
        assertThat(ex).hasMessage("The refillPeriod must be greater than 0");
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -2})
    void testBandwidthWithNegativeCapacity(long  badCapacity) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new BandwidthInfo(BandwidthType.GREEDY, badCapacity, new DurationInfo(Duration.ofMinutes(1), DurationType.MINUTES), 123L);
        });
        assertThat(ex).hasMessage("The capacity value cannot be less than 0");
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -2})
    void testBandwidthWithNegativeRefillUnits(long  badRefillUnits) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new BandwidthInfo(BandwidthType.GREEDY, 1, new DurationInfo(Duration.ofMinutes(1), DurationType.MINUTES), badRefillUnits);
        });
        assertThat(ex).hasMessage("The refillUnit value must be greater than 0");
    }
    
    @Test
    void testBandwidthInfo(){
        var info = new BandwidthInfo(BandwidthType.GREEDY, 10, new DurationInfo(Duration.ofMinutes(10), DurationType.MINUTES), 10);

        Bandwidth expectedBandwidth = BandwidthBuilder.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(10))
                .build();
        assertThat(info.getBandwidth()).isEqualTo(expectedBandwidth);
        
        assertThat(info.getDurationType()).isEqualTo(DurationType.MINUTES);
        assertThat(info.getRefillRate()).isCloseTo(1.0 / 60.0, within(1e-12));
    }
}