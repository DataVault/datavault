package org.datavaultplatform.webapp.actuator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.datavaultplatform.webapp.config.ratelimited.*;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.json.JsonAssert;
import org.springframework.test.json.JsonCompareMode;

import java.time.Duration;
import java.util.List;

@Disabled
class RateLimitedEndpointTest {

    public static final String EXPECTED_JSON = """
            {
              "rateLimited" : {
                "enabled" : true,
                "cache" : {
                  "expiration" : "PT1H",
                  "maxSize" : 1000
                },
                "bandwidths" : [ {
                  "type" : "GREEDY",
                  "capacity" : 100,
                  "refillPeriod" : "PT600H",
                  "refillUnit" : 25
                }, {
                  "type" : "INTERVAL",
                  "capacity" : 100,
                  "refillPeriod" : "PT1M",
                  "refillUnit" : 4
                } ],
                "filter" : {
                  "pathPatterns" : [ "/a/b/c", "/d/e/f" ]
                }
              }
            }
            """;
    private ObjectMapper yamlMapper;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.registerModule(new JavaTimeModule());
        yamlMapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        yamlMapper.enable(SerializationFeature.INDENT_OUTPUT);

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
        RateLimitedEndpoint.RateLimitedInfoWrapper wrapper = endpoint.getActuatorEndpointInfo();

        String actualYaml = yamlMapper.writeValueAsString(wrapper);
        System.out.println(actualYaml);
        
        String actualJson = mapper.writeValueAsString(wrapper);
        System.out.println(actualJson);
        
        JsonAssert.comparator(JsonCompareMode.STRICT).assertIsMatch(EXPECTED_JSON, actualJson);

    }

    private static @NonNull RateLimitedProperties getRateLimitedProperties() {
        CacheInfo cacheInfo = new CacheInfo(Duration.ofMinutes(60), 1000);
        BandwidthInfo bandwidthInfo1 = new BandwidthInfo(BandwidthType.GREEDY, 100, new DurationInfo(Duration.ofDays(25), DurationType.DAYS), 25);
        BandwidthInfo bandwidthInfo2 = new BandwidthInfo(BandwidthType.INTERVAL, 100, new DurationInfo(Duration.ofMinutes(1),DurationType.MINUTES), 4);
        List<BandwidthInfo> bandwidthInfos = List.of(bandwidthInfo1, bandwidthInfo2);
        FilterProperties filterProperties = new FilterProperties(List.of("/a/b/c", "/d/e/f"));
        RateLimitedProperties rateLimitedProperties = new RateLimitedProperties(true, cacheInfo, filterProperties, bandwidthInfos);
        return rateLimitedProperties;
    }
}