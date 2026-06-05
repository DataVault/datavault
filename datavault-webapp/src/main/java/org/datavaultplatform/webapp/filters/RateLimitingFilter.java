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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate Limiting Filter - runs after spring security.
 * Makes 1 big assumption - that spring security protects all RateLimited endpoints.
 * Even endpoints that are bypassed with permitAll might have 'anonymoususer'.
 */
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    //TODO : this counter is only for testing - we can remove for production
    public static final AtomicLong counter  = new AtomicLong(0);
    
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String TOO_MANY_REQUESTS_MESSAGE = "Too Many Requests - Rate limit exceeded.";
    private static final String PATH_PATTERN = "PATH_PATTERN";

    // Using String as the key (e.g., IP address or API Key)
    private final ProxyManager<Object> proxyManager;
    private final BucketConfiguration bucketConfiguration;
    private final List<String> pathPatterns;
    private final AntPathMatcher matcher = new AntPathMatcher();

     public RateLimitingFilter(ProxyManager<Object> proxyManager, BucketConfiguration bucketConfiguration, RateLimitedProperties rateLimitedProperties) {
         this.proxyManager = proxyManager;
         this.bucketConfiguration = bucketConfiguration;
         this.pathPatterns = rateLimitedProperties.getFilter().pathPatterns();
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

    private Optional<String> getMatchingPattern(HttpServletRequest request){
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

        long count = counter.incrementAndGet();

        log.info("count[{}] Available tokens: {}",count, bucket.getAvailableTokens());

        // 3. Try to consume 1 token for this request
        boolean allowed = bucket.tryConsume(1);

        log.info("count[{}] Remaining tokens: {}", count, bucket.getAvailableTokens());
        log.info("count[{}] Allowed: {}", count, allowed);
        log.info("count[{}] Cache Key: {}", count, cacheKey);

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        
        String full = (query == null) ? uri : uri + "?" + query;
        log.debug("RequestURI: {} - Allowed: {}", full, allowed);

        // 4. Evaluate the result
        if (allowed) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write(TOO_MANY_REQUESTS_MESSAGE);
        }
    }

    public static String getCacheKey(String username, String uriPattern) {
        Assert.hasText(username, "The username cannot be empty");
        Assert.hasText(uriPattern, "The uriPattern cannot be empty");
        Assert.isTrue(!uriPattern.startsWith("http"), "The uriPattern should NOT start with 'http'");
        String cacheKey = "%s:%s".formatted(username, uriPattern);
        return cacheKey;
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
}