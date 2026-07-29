package org.datavaultplatform.webapp.config.ratelimited;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DurationInfoTest {

    @Test
    void testDurationInfo() {
        DurationInfo info = new DurationInfo(Duration.ofMinutes(1), DurationType.MINUTES);
        assertThat(info.type()).isEqualTo(DurationType.MINUTES);
        assertThat(info.duration()).isEqualTo(Duration.ofMinutes(1));
    }
    
    @Test
    void testSortByDurationInfo() {
        
        DurationInfo info4 = new DurationInfo(Duration.ofDays(1), DurationType.DAYS);
        DurationInfo info3 = new DurationInfo(Duration.ofHours(1), DurationType.HOURS);
        DurationInfo info2 = new DurationInfo(Duration.ofMinutes(1), DurationType.MINUTES);
        DurationInfo info1 = new DurationInfo(Duration.ofSeconds(1), DurationType.SECONDS );

        List<DurationInfo> input = List.of(info4, info3, info2, info1);
        
        List<DurationInfo> sorted = input.stream().sorted().toList();
        
        assertThat(sorted).isEqualTo(List.of(info1, info2, info3, info4));
        
    }

}