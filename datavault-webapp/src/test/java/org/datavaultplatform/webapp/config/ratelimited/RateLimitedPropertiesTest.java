package org.datavaultplatform.webapp.config.ratelimited;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitedPropertiesTest {
    
    public static final CacheInfo TEST_CACHE_INFO = new CacheInfo(Duration.ofDays(31), 10_000);

    RateLimitedProperties spy;

    void setupSpy(RateLimitedProperties rateLimitedProperties) {
        spy = Mockito.spy(rateLimitedProperties);
    }
    
    @Test
    void testValidateButRateLimitingDisabled() {
        
        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(false);
        
        setupSpy(properties);
        
        spy.validate();
        
        verify(spy, never()).validateBandwidthsWithSameBandwidthType(any(), any());
    }

    static Stream<Arguments> noBandwidthSource(){
        return Stream.of(Arguments.of(List.of()));
    }
    
    @ParameterizedTest
    @NullSource
    @MethodSource("noBandwidthSource")
    void testValidateButNoBandwidthInfos(List<BandwidthInfo> bandwidthInfos) {

        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(true);
        properties.setBandwidths(bandwidthInfos);

        setupSpy(properties);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            spy.validate();
        });
        assertThat(ex).hasMessage("RateLimiting Enabled : you must have at least 1 bandwidth specified");

        verify(spy, never()).validateBandwidthsWithSameBandwidthType(any(), any());
    }
    
    @Captor
    ArgumentCaptor<BandwidthType> argType;

    @Captor
    ArgumentCaptor<List<BandwidthInfo>> argBandwidthInfoList;
    
    @Test
    void testValidateByBandwidthType() {

        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(true);
        properties.setCache(TEST_CACHE_INFO);
        
        BandwidthInfo interval1 = new BandwidthInfo(BandwidthType.INTERVAL, 10, new DurationInfo(Duration.ofSeconds(10), DurationType.SECONDS), 10);
        BandwidthInfo greedy1 = new BandwidthInfo(BandwidthType.GREEDY, 10, new DurationInfo(Duration.ofSeconds(10), DurationType.SECONDS), 10);

        BandwidthInfo interval2 = new BandwidthInfo(BandwidthType.INTERVAL, 20, new DurationInfo(Duration.ofSeconds(20), DurationType.MINUTES), 10);
        BandwidthInfo greedy2 = new BandwidthInfo(BandwidthType.GREEDY, 20, new DurationInfo(Duration.ofSeconds(20), DurationType.MINUTES), 10);

        properties.setBandwidths(List.of(greedy1, interval1, greedy2, interval2));

        setupSpy(properties);

        doNothing().when(spy).validateMultipleBandwidthsWithSameBandwidthType(any(), any());

        spy.validate();

        verify(spy, times(2)).validateMultipleBandwidthsWithSameBandwidthType(argType.capture(), argBandwidthInfoList.capture());
        
        assertThat(argType.getAllValues().get(0)).isEqualTo(BandwidthType.INTERVAL);
        assertThat(argType.getAllValues().get(1)).isEqualTo(BandwidthType.GREEDY);

        assertThat(argBandwidthInfoList.getAllValues().get(0)).isEqualTo(List.of(interval1, interval2));
        assertThat(argBandwidthInfoList.getAllValues().get(1)).isEqualTo(List.of(greedy1, greedy2));
    }
    
    @Test
    void testSingleBandwidthOfEachType() {
        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(true);
        properties.setCache(TEST_CACHE_INFO);

        BandwidthInfo interval1 = new BandwidthInfo(BandwidthType.INTERVAL, 10, new DurationInfo(Duration.ofSeconds(10), DurationType.SECONDS), 10);
        BandwidthInfo greedy1 = new BandwidthInfo(BandwidthType.GREEDY, 10, new DurationInfo(Duration.ofSeconds(10), DurationType.SECONDS), 10);

        properties.setBandwidths(List.of(greedy1, interval1));

        setupSpy(properties);
        
        spy.validate();

        verify(spy, never()).validateMultipleBandwidthsWithSameBandwidthType(any(), any());
    }
    
    @Test
    void testDuplicateDurationTypesForBandwidthType() {
        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(true);

        BandwidthInfo interval1 = new BandwidthInfo(BandwidthType.INTERVAL, 10, new DurationInfo(Duration.ofSeconds(10), DurationType.SECONDS), 10);
        BandwidthInfo interval2 = new BandwidthInfo(BandwidthType.INTERVAL, 20, new DurationInfo(Duration.ofSeconds(20), DurationType.SECONDS), 20);

        properties.setBandwidths(List.of(interval1, interval2));

        setupSpy(properties);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            spy.validate();
        });
        assertThat(ex).hasMessage("Multiple bandwidths with the same duration type [SECONDS] are not allowed");   
    }

    @Test
    void testBadOrderingOfBandwidthsForDurationType() {
        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(true);

        BandwidthInfo interval1 = new BandwidthInfo(BandwidthType.INTERVAL, 10, new DurationInfo(Duration.ofMinutes(10), DurationType.MINUTES), 10);
        BandwidthInfo interval2 = new BandwidthInfo(BandwidthType.INTERVAL, 20, new DurationInfo(Duration.ofSeconds(20), DurationType.SECONDS), 20);

        properties.setBandwidths(List.of(interval1, interval2));

        setupSpy(properties);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            spy.validate();
        });
        assertThat(ex).hasMessage("The bandwidths should be in the order of shortest window to longest window. Seconds, Minutes, Hours then Days");
    }

    @Test
    void testConsistentRatesOfBandwidthsForDurationType() {
        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(true);
        properties.setCache(TEST_CACHE_INFO);

        BandwidthInfo interval1 = new BandwidthInfo(BandwidthType.INTERVAL, 10, new DurationInfo(Duration.ofSeconds(120), DurationType.SECONDS), 10);
        BandwidthInfo interval2 = new BandwidthInfo(BandwidthType.INTERVAL, 10, new DurationInfo(Duration.ofMinutes(1), DurationType.MINUTES), 10);

        properties.setBandwidths(List.of(interval1, interval2));

        setupSpy(properties);

        spy.validate();
    }

    @Test
    void testInconsistentRatesOfBandwidthsForDurationType() {
        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(true);

        BandwidthInfo interval1 = new BandwidthInfo(BandwidthType.INTERVAL, 140, new DurationInfo(Duration.ofSeconds(120), DurationType.SECONDS), 140);
        BandwidthInfo interval2 = new BandwidthInfo(BandwidthType.INTERVAL, 10, new DurationInfo(Duration.ofMinutes(1), DurationType.MINUTES), 10);

        properties.setBandwidths(List.of(interval1, interval2));

        setupSpy(properties);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            spy.validate();
        });
        
        assertThat(ex).hasMessage("The refill-rate for [BandwidthInfo[type=INTERVAL, capacity=140, refillPeriod=DurationInfo[duration=PT2M, type=SECONDS], refillUnit=140]] is [1.17]. This is a higher refill-rate than [BandwidthInfo[type=INTERVAL, capacity=10, refillPeriod=DurationInfo[duration=PT1M, type=MINUTES], refillUnit=10]] which is [0.17]. This is inconsistent.");
    }
    
    
    @Test
    void testBandwidthDurationIsLargerThanCacheDuration(){
        RateLimitedProperties properties = new RateLimitedProperties();
        properties.setEnabled(true);
        properties.setCache(new CacheInfo(Duration.ofHours(12), 10_000));

        BandwidthInfo interval1 = new BandwidthInfo(BandwidthType.INTERVAL, 140, new DurationInfo(Duration.ofHours(13), DurationType.HOURS), 140);

        properties.setBandwidths(List.of(interval1));

        setupSpy(properties);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            spy.validate();
        });

        assertThat(ex).hasMessage("The bandwidth [BandwidthInfo[type=INTERVAL, capacity=140, refillPeriod=DurationInfo[duration=PT13H, type=HOURS], refillUnit=140]] has a duration which is not less than the cache duration [PT12H]. This is not allowed.");
    }
}