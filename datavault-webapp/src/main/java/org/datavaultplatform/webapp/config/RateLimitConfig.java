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
import org.datavaultplatform.webapp.config.ratelimited.RateLimitExceededEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.time.Clock;

@Configuration
@Slf4j
public class RateLimitConfig {

    
    private final RateLimitedProperties rateLimitedProperties;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    public RateLimitConfig(RateLimitedProperties rateLimitedProperties, Clock clock, ApplicationEventPublisher eventPublisher) {
        this.rateLimitedProperties = rateLimitedProperties;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
        
        log.info("rateLimitedProperties {}", rateLimitedProperties);
        log.info("fin.");
    }

    /**
     * Bucket4j uses a TimeMeter to get the current time - so we make sure the time is derived from Clock which can be
     * system time Clock or Mock Clock for testing.
     * @param clock
     * @return
     */
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

    /*
    The ProxyManager is the bridge between Bucket4j’s rate‑limiting logic and whatever storage backend you use (JCache, Hazelcast, Redis, JDBC, Infinispan, etc.).
    In this case - the storage backend we are using is Caffeine in-memory cache.
     */
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

    /* 
     The BucketConfiguration is a bucket4j class that holds the bucket4j configuration via a list of Bucket4j Bandwith objects.
     We derive the list of bucket4j bandwidth objects from the RateLimitiedProperties object.
     */
    @Bean
    public BucketConfiguration rateLimitBucketConfiguration() {
        return new BucketConfiguration(rateLimitedProperties.getBucket4jBandwidths());
    }
    
    /*
    The FilterRegistrationBean is used to create an instance of RateLimitingFilter web-filter to perform the actual rate-limiting.
    Spring will send all requests to the RateLimitingFilter - but the web filter decided which of those
    requests to rate-limit via the rateLimitedProperties.
    Note: the order of the RateLimitingFilter is important - it has to be done AFTER SpringSecurit filters have run because
    RateLimitingFilter requires access to currently Authenticated user.
     */
    @Bean
    @ConditionalOnBooleanProperty(name = "ratelimited.enabled")
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(
            ProxyManager<Object> proxyManager,
            BucketConfiguration rateLimitBlueprint,
            RateLimitedProperties rateLimitedProperties) {

        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();

        // Pass the proxy manager and configuration directly to the filter
        registrationBean.setFilter(new RateLimitingFilter(proxyManager, rateLimitBlueprint, rateLimitedProperties, clock, eventPublisher ));

        // the filter decides what to filter based on the FilterTargetConfiguration
        registrationBean.addUrlPatterns("/*");
        
        // Run immediately after Spring Security
        registrationBean.setOrder(SecurityProperties.BASIC_AUTH_ORDER + 1);

        return registrationBean;
    }

    /*
    This is not 100% required, but it makes is straightforward to list for RateLimitExceeded Events.
     */
    @EventListener
    public void onRateLimitedApplicationEvent(RateLimitExceededEvent event) {
        log.info("RateLimited(429) : [{}:{}]", event.getUsername(), event.getRequestUri());
    }
}