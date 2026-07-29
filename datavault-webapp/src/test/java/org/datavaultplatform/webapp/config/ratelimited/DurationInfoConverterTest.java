package org.datavaultplatform.webapp.config.ratelimited;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DurationInfoConverterTest {

    private final DurationInfoConverter converter = new DurationInfoConverter();

    private static Stream<Arguments> daysSource() {
        return Stream.of(
                Arguments.of("1d", "PT24H"),
                Arguments.of("P1D", "PT24H"),
                Arguments.of("2d", "PT48H"),
                Arguments.of("P2D", "PT48H")
        );
    }

    private static Stream<Arguments> hoursSource() {
        return Stream.of(
                Arguments.of("1h", "PT1H"),
                Arguments.of("PT1H", "PT1H"),
                Arguments.of("2h", "PT2H"),
                Arguments.of("PT2H", "PT2H")
        );
    }

    private static Stream<Arguments> minutesSource() {
        return Stream.of(
                Arguments.of("1m", "PT1M"),
                Arguments.of("PT1M", "PT1M"),
                Arguments.of("2m", "PT2M"),
                Arguments.of("PT2M", "PT2M")
        );
    }

    private static Stream<Arguments> secondsSource() {
        return Stream.of(
                Arguments.of("1s", "PT1S"),
                Arguments.of("PT1S", "PT1S"),
                Arguments.of("2s", "PT2S"),
                Arguments.of("PT2S", "PT2S")
        );
    }

    @ParameterizedTest
    @MethodSource("daysSource")
    void testDaysOnly(String days, String expected) {
        DurationInfo info = converter.convert(days);
        String str = info.duration().toString();
        assertThat(str).isEqualTo(expected);
        assertThat(info.type()).isEqualTo(DurationType.DAYS);
    }

    @ParameterizedTest
    @MethodSource("hoursSource")
    void testHoursOnly(String hour, String expected) {
        DurationInfo info = converter.convert(hour);
        String str = info.duration().toString();
        assertThat(str).isEqualTo(expected);
        assertThat(info.type()).isEqualTo(DurationType.HOURS);
    }

    @ParameterizedTest
    @MethodSource("minutesSource")
    void testMinutesOnly(String mins, String expected) {
        DurationInfo info = converter.convert(mins);
        String str = info.duration().toString();
        assertThat(str).isEqualTo(expected);
        assertThat(info.type()).isEqualTo(DurationType.MINUTES);
    }

    @ParameterizedTest
    @MethodSource("secondsSource")
    void testSecondsOnly(String seconds, String expected) {
        DurationInfo info = converter.convert(seconds);
        String str = info.duration().toString();
        assertThat(str).isEqualTo(expected);
        assertThat(info.type()).isEqualTo(DurationType.SECONDS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PT0S", "PT0H", "PT0M", "P0D", "0s", "0h", "0m", "0d"})
    void testZeroAnythingDoesNotWork(String zeroString) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> converter.convert(zeroString));
        assertThat(ex).hasMessage("Cannot convert [%s] to DurationInfo - valid examples : [1s, 2m, 3h, 4d, PT1S, PT2M, PT3H, P4D]".formatted(zeroString));
    }

    @ParameterizedTest
    @ValueSource(strings = {"P1DT1S", "P2DT1M", "P3DT3H", "P4DT1H32M1S"})
    void testMixedDoesNotWork(String mixedString) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> converter.convert(mixedString));
        assertThat(ex).hasMessage("Cannot convert [%s] to DurationInfo - valid examples : [1s, 2m, 3h, 4d, PT1S, PT2M, PT3H, P4D]".formatted(mixedString));
    }
}