package org.datavaultplatform.webapp.config.ratelimited;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ratelimited.cache")
public record CacheInfo(Duration expiration, long maxSize) {
}
