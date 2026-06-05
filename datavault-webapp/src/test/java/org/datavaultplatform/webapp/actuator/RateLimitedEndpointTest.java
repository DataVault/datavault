package org.datavaultplatform.webapp.actuator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.datavaultplatform.webapp.config.ratelimited.*;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.json.JsonAssert;
import org.springframework.test.json.JsonCompareMode;

import java.time.Duration;
import java.util.List;

class RateLimitedEndpointTest {

    public static final String EXPECTED_JSON = """
            {
              "actuatorEndpointInfo" : {
                "enabled" : true,
                "cache" : {
                  "expiration" : "PT1H",
                  "maxSize" : 1000
                },
                "filter" : {
                  "pathPatterns" : [ "/a/b/c", "/d/e/f" ]
                },
                "bandwidths" : [ {
                  "type" : "GREEDY",
                  "capacity" : 1000,
                  "refillPeriod" : {
                    "duration" : "PT720H",
                    "type" : "DAYS"
                  },
                  "refillUnit" : 1000,
                  "refillRate" : 3.8580246913580245E-4,
                  "durationType" : "DAYS"
                }, {
                  "type" : "INTERVAL",
                  "capacity" : 100,
                  "refillPeriod" : {
                    "duration" : "PT1M",
                    "type" : "MINUTES"
                  },
                  "refillUnit" : 100,
                  "refillRate" : 1.6666666666666667,
                  "durationType" : "MINUTES"
                } ]
              }
            }""";
    private ObjectMapper mapper;

    private static @NonNull RateLimitedProperties getRateLimitedProperties() {
        CacheInfo cacheInfo = new CacheInfo(Duration.ofMinutes(60), 1000);
        BandwidthInfo bandwidthInfo1 = new BandwidthInfo(BandwidthType.GREEDY, 1000, new DurationInfo(Duration.ofDays(30), DurationType.DAYS), 1000);
        BandwidthInfo bandwidthInfo2 = new BandwidthInfo(BandwidthType.INTERVAL, 100, new DurationInfo(Duration.ofMinutes(1), DurationType.MINUTES), 100);
        List<BandwidthInfo> bandwidthInfos = List.of(bandwidthInfo1, bandwidthInfo2);
        FilterProperties filterProperties = new FilterProperties(List.of("/a/b/c", "/d/e/f"));
        RateLimitedProperties rateLimitedProperties = new RateLimitedProperties(true, cacheInfo, filterProperties, bandwidthInfos);
        return rateLimitedProperties;
    }

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    @SneakyThrows
    void testJsonGeneration() {
        RateLimitedProperties rateLimitedProperties = getRateLimitedProperties();

        RateLimitedEndpoint endpoint = new RateLimitedEndpoint(rateLimitedProperties);

        String actualJson = mapper.writeValueAsString(endpoint);
        System.out.println(actualJson);

        JsonAssert.comparator(JsonCompareMode.STRICT).assertIsMatch(EXPECTED_JSON, actualJson);

    }
}