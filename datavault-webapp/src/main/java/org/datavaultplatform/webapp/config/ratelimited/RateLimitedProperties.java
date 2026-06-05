package org.datavaultplatform.webapp.config.ratelimited;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.bucket4j.Bandwidth;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "ratelimited")
@Slf4j
public class RateLimitedProperties {

    private final List<Bandwidth> bucket4jBandwidths = new ArrayList<>();
    private boolean enabled;
    private CacheInfo cache;
    private FilterProperties filter;
    private List<BandwidthInfo> bandwidths;

    @JsonIgnore
    public List<Bandwidth> getBucket4jBandwidths() {
        return bucket4jBandwidths;
    }

    @PostConstruct
    void init() {
    
        validate();
        
        // now we are happy everything is valid, we can convert the BandwidthInfo objects to Bandwidth objects and add them to the bucket4jBandwidths list
        for (BandwidthInfo bandwidth : bandwidths) {
            bucket4jBandwidths.add(bandwidth.getBandwidth());
        }
    }
    
    protected void validate() {
        if (!enabled) {
            log.warn("RATE LIMITING DISABLED");
            return;
        }
        if (bandwidths == null || bandwidths.isEmpty()) {
            throw new IllegalStateException("RateLimiting Enabled : you must have at least 1 bandwidth specified");
        }

        // group the bandwidths by the bandwidth refill type (GREEDY or INTERVAL) then validate each list
        // sortedMap here helps with testing
        SortedMap<BandwidthType, List<BandwidthInfo>> bandwidthTypeMap =
                new TreeMap<>(bandwidths.stream().collect(groupingBy(BandwidthInfo::type)));

        bandwidthTypeMap.forEach(this::validateBandwidthsWithSameBandwidthType);
    }
    
    // Validates that all bandwidths in the list have the same bandwidth type and that there are no duplicate duration types
    // also validates that "a shorter window should not have a higher rate than a larger window"
    protected void validateBandwidthsWithSameBandwidthType(BandwidthType bandwidthType, List<BandwidthInfo> bandwidths) {
        Assert.isTrue(bandwidths.stream().allMatch(b -> b.type() == bandwidthType), "All bandwidths must be of the same type [%s]".formatted(bandwidthType));
        if (bandwidths.size() < 2) {
            return;
        }
        validateMultipleBandwidthsWithSameBandwidthType(bandwidthType, bandwidths);
    }

    protected void validateMultipleBandwidthsWithSameBandwidthType(BandwidthType bandwidthType, List<BandwidthInfo> bandwidths) {

        Assert.isTrue(bandwidths.stream().allMatch(b -> b.type() == bandwidthType), "All bandwidths must be of the same type [%s]".formatted(bandwidthType));

        // just to reinforce: we have at least 2 bandwidths
        Assert.isTrue(bandwidths.size() > 1, "The bandwidths has to have at least 2 elements");

        SortedMap<DurationType, List<BandwidthInfo>> durationTypeListMap = new TreeMap<>();
        for (BandwidthInfo bandwidth : bandwidths) {
            durationTypeListMap.computeIfAbsent(bandwidth.refillPeriod().type(), k -> new ArrayList<>()).add(bandwidth);
        }

        durationTypeListMap.forEach( (durationType,bandWidthInfoList) -> {
            if (bandWidthInfoList.size() > 1) {
                throw new IllegalStateException("Multiple bandwidths with the same duration type [%s] are not allowed".formatted(durationType));
            }
        });
        
        // create a SortedMap of DurationType to BandwidthInfo - since we know there's only one BandwidthInfo per DurationType
        SortedMap<DurationType, BandwidthInfo> durationTypeBandwidthMap = convert(durationTypeListMap);

        // get the bandwidths sorted by duration type - since the map is sorted by DurationType, we can just get the values
        // so bandwidths for seconds will be followed by bandwidths for minutes, then hours, etc.
        List<BandwidthInfo> bandwidthsSortedByDurationType = durationTypeBandwidthMap.values().stream().toList();

        if (!bandwidthsSortedByDurationType.equals(bandwidths)) {
            // this isn't 100% required, it just makes things easier to understand in the yaml/properties files
            throw new IllegalStateException("The bandwidths should be in the order of shortest window to longest window. Seconds, Minutes, Hours then Days");
        }
        
        // we go from first to last-but-one!
        for (int i = 0; i < bandwidths.size() - 1; i++) {
            
            BandwidthInfo window = bandwidths.get(i);
            BandwidthInfo nextWindow = bandwidths.get(i + 1);

            double windowRate = window.getRefillRate();
            double nextWindowRate = nextWindow.getRefillRate();

            if (window.getRefillRate() > nextWindow.getRefillRate()) {
                String refillMessage = "The refill-rate for [%s] is [%.2f]. This is a higher refill-rate than [%s] which is [%.2f]. This is inconsistent.".formatted(window, windowRate, nextWindow, nextWindowRate);
                throw new IllegalStateException(refillMessage);
            }
        }
    }

    protected static <K, V> SortedMap<K, V> convert(Map<K, List<V>> sourceMap) {
        Map<K, V> temp = sourceMap.entrySet().
                stream().
                collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get(0)));

        return new TreeMap<>(temp);
    }
}