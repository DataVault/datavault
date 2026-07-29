package org.datavaultplatform.webapp.filters;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.config.ratelimited.RateLimitedProperties;
import org.datavaultplatform.webapp.config.ratelimited.RateLimitExceededEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Rate Limiting Filter - runs after spring security.
 * Makes 1 big assumption - that spring security protects all RateLimited endpoints.
 * Even endpoints that are bypassed with permitAll might have 'anonymoususer'.
 */
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String TOO_MANY_REQUESTS_MESSAGE = "Too Many Requests - Rate limit exceeded.";
    private static final String PATH_PATTERN = "PATH_PATTERN";

    // Using String as the key (e.g., IP address or API Key)
    private final ProxyManager<Object> proxyManager;
    private final BucketConfiguration bucketConfiguration;
    private final List<String> pathPatterns;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    public RateLimitingFilter(ProxyManager<Object> proxyManager, BucketConfiguration bucketConfiguration, RateLimitedProperties rateLimitedProperties, Clock clock, ApplicationEventPublisher eventPublisher) {
        this.proxyManager = proxyManager;
        this.bucketConfiguration = bucketConfiguration;
        this.pathPatterns = rateLimitedProperties.getFilter().pathPatterns();
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    public static String getCacheKey(String username, String uriPattern) {
        Assert.hasText(username, "The username cannot be empty");
        Assert.hasText(uriPattern, "The uriPattern cannot be empty");
        Assert.isTrue(!uriPattern.startsWith("http"), "The uriPattern should NOT start with 'http'");
        String cacheKey = "%s:%s".formatted(username, uriPattern);
        return cacheKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !shouldFilter(request);
    }

    /**
     * To save calling gettingMatchingPattern twice, we store the pattern as a request attribute
     */
    protected boolean shouldFilter(HttpServletRequest request) {
        return getMatchingPattern(request).map(pattern -> {
            request.setAttribute(PATH_PATTERN, pattern);
            return true;
        }).orElse(false);
    }

    private Optional<String> getMatchingPattern(HttpServletRequest request) {
        return pathPatterns.stream().filter(pathPattern -> matcher.match(pathPattern, request.getRequestURI())).findFirst();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Identify the client (Resolve the unique key)
        String cacheKey = resolveCacheKey(request);

        // 2. Get the bucket from the proxy manager.
        // This will either retrieve an existing bucket or create a new one
        // based on the provided configuration.
        Bucket bucket = proxyManager.getProxy(cacheKey, () -> bucketConfiguration);

        log.info("Available tokens: {}", bucket.getAvailableTokens());

        // 3. Try to consume 1 token for this request
        boolean allowed = bucket.tryConsume(1);

        log.info("Remaining tokens: {}", bucket.getAvailableTokens());
        log.info("Allowed: {}", allowed);
        log.info("Cache Key: {}", cacheKey);

        String uri = request.getRequestURI();
        String query = request.getQueryString();

        String full = (query == null) ? uri : uri + "?" + query;
        log.debug("RequestURI: {} - Allowed: {}", full, allowed);

        // 4. Evaluate the result
        if (allowed) {
            filterChain.doFilter(request, response);
        } else {
            publishRateLimitedEvent(cacheKey);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write(TOO_MANY_REQUESTS_MESSAGE);
        }
    }

    private String resolveCacheKey(HttpServletRequest request) {
        String username = resolveUserName(request);
        String uriPattern = resolveUriPattern(request);
        return getCacheKey(username, uriPattern);
    }

    private String resolveUriPattern(HttpServletRequest request) {
        String pathPattern = (String) request.getAttribute(PATH_PATTERN);
        if (pathPattern == null) {
            throw new IllegalStateException("PATH PATTERN NOT FOUND");
        } else {
            return pathPattern;
        }
    }

    private String resolveUserName(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }
        String result;
        if (StringUtils.hasText(username)) {
            result = username;
        } else {
            String remoteAddress = getRemoteAddress(request);
            result = "anonymous/%s".formatted(remoteAddress);
        }
        return result.trim().toLowerCase();
    }

    private String getRemoteAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (StringUtils.hasText(forwardedFor)) {
            // The X-Forwarded-For header can contain a comma-separated list of IPs.
            // The first one is the original client.
            return forwardedFor.split(",")[0];
        } else {
            return request.getRemoteAddr();
        }
    }

    private void publishRateLimitedEvent(String cacheKey) {
        String[] parts = cacheKey.split(":");
        String username = parts[0];
        String requestURI = parts[1];
        RateLimitExceededEvent event = createRateLimitedEvent(username, requestURI);
        eventPublisher.publishEvent(event);
    }

    private RateLimitExceededEvent createRateLimitedEvent(String username, String requestUri) {
        return new RateLimitExceededEvent(this, clock, username, requestUri);
    }
}