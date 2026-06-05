package org.datavaultplatform.webapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.TimeMeter;
import io.github.bucket4j.caffeine.Bucket4jCaffeine;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.config.ratelimited.ClockUtils;
import org.datavaultplatform.webapp.config.ratelimited.RateLimitedProperties;
import org.datavaultplatform.webapp.filters.RateLimitingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@Slf4j
public class RateLimitConfig {

    private final RateLimitedProperties rateLimitedProperties;

    public RateLimitConfig(RateLimitedProperties rateLimitedProperties) {
        this.rateLimitedProperties = rateLimitedProperties;
        log.info("rateLimitedProperties {}", rateLimitedProperties);
        log.info("fin.");
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public TimeMeter timeMeter(Clock clock) {
        return new TimeMeter() {
            @Override
            public long currentTimeNanos() {
                return ClockUtils.toEpochNanos(clock);
            }

            @Override
            public boolean isWallClockBased() {
                return true;
            }
        };
    }

    @Bean
    public ProxyManager<Object> proxyManager(TimeMeter timeMeter, RateLimitedProperties properties) {
        // Configure the underlying cache without an expiration policy
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .maximumSize(properties.getCache().maxSize());

        // Let Bucket4j control the expiration
        return Bucket4jCaffeine.builderFor(caffeineBuilder)
                .clientClock(timeMeter)
                .expirationAfterWrite(ExpirationAfterWriteStrategy.fixedTimeToLive(properties.getCache().expiration()))
                .build();
    }

    // This is the default blueprint used by the filter.
    @Bean
    public BucketConfiguration rateLimitBucketConfiguration() {
        return new BucketConfiguration(rateLimitedProperties.getBucket4jBandwidths());
    }

    @Bean
    @ConditionalOnBooleanProperty(name = "ratelimited.enabled")
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(
            ProxyManager<Object> proxyManager,
            BucketConfiguration rateLimitBlueprint,
            RateLimitedProperties rateLimitedProperties) {

        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();

        // Pass the proxy manager and configuration directly to the filter
        registrationBean.setFilter(new RateLimitingFilter(proxyManager, rateLimitBlueprint, rateLimitedProperties));

        // the filter decides what to filter based on the FilterTargetConfiguration
        registrationBean.addUrlPatterns("/*");
        
        // Run immediately after Spring Security
        registrationBean.setOrder(SecurityProperties.BASIC_AUTH_ORDER + 1);

        return registrationBean;
    }
}