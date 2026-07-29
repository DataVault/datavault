package org.datavaultplatform.webapp.config.ratelimited;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DurationTypeTest {
    
    @ParameterizedTest
    @ValueSource(strings = {"2m","2 m", "PT2M"})
    void testMinutes(String value){
        Optional<Long> optMins = DurationType.MINUTES.matches(value);
        assertThat(optMins).hasValue(2L);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"2h","2 h", "PT2H"})
    void testHours(String value){
        Optional<Long> optMins = DurationType.HOURS.matches(value);
        assertThat(optMins).hasValue(2L);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"2s","2 s", "PT2S"})
    void testSeconds(String value){
        Optional<Long> optSeconds = DurationType.SECONDS.matches(value);
        assertThat(optSeconds).hasValue(2L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2d","2 d", "P2D"})
    void testDays(String value){
        Optional<Long> optDays = DurationType.DAYS.matches(value);
        assertThat(optDays).hasValue(2L);
    }
}