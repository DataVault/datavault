package org.datavaultplatform.webapp.ratelimited;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.TimeMeter;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.filters.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SameParameterValue")
@Slf4j
public abstract class BaseApiEndpointTests {

    public static final String UN_AUTH_URL = "/actuator/info";
    public static final String AUTH_URL = "/api/protected/limited/{msg}";
    public static final String VAULT_AUTOCOMPLETE_URL = "/vaults/autocompleteuun/{term}";
    public static final String VAULT_ISUUN_URL = "/vaults/isuun/{uun}";
    public static final String UNAUTHENTICATED_USER = "anonymous/127.0.0.1";
    public static final String AUTHENTICATED_USER = "testuser";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected Clock clock;

    @Autowired
    protected TimeMeter timeMeter;
    
    @Autowired
    ProxyManager<Object> proxyManager;  
    
    @Autowired
    BucketConfiguration bucketConfig;

    /**
     * Note: we have to use string concatenation here to build up the query string because
     * Spring Security Test Support via "SecurityContextHolderAwareRequestWrapper" doesn't keep 
     * "?msg={msg}" in the query string - it just keeps the msg value in the "parameter map" which works
     * except when you examine the HttpServletRequest getQueryString and find it's null.
     */
    @SneakyThrows
    protected final void makeUnAuthRequest(String url, String msg) {
        mockMvc.perform(get(url + "?msg=%s".formatted(msg)))
                .andExpect(status().isOk());

    }

    @SneakyThrows
    protected final void makeAuthenticatedRequest(String urlWithMsgPathParam, String msg) {
        mockMvc.perform(get(urlWithMsgPathParam, msg))
                .andExpect(status().isOk());
    }

    public abstract void advanceTimeBySeconds(int seconds);

    private void checkBeforeAfter(String key, int before, int after, Runnable runnable){
        assertThat(getBucketProxy(key).getAvailableTokens()).isEqualTo(before);
        runnable.run();
        assertThat(getBucketProxy(key).getAvailableTokens()).isEqualTo(after);
    }

    void checkBeforeAfterUnAuth(int before, int after, String msg){
        String cacheKey = RateLimitingFilter.getCacheKey(UNAUTHENTICATED_USER, UN_AUTH_URL);
        checkBeforeAfter(cacheKey, before, after, () -> makeUnAuthRequest(UN_AUTH_URL, msg));
    }

    void checkBeforeAfterAuthenticated(int before, int after, String msg) {
        checkBeforeAfterAuthenticated(before, after, msg, AUTH_URL);    
    }

    void checkBeforeAfterAuthenticatedAutocomplete(int before, int after, String term) {
        checkBeforeAfterAuthenticated(before, after, term, VAULT_AUTOCOMPLETE_URL);
    }

    void checkBeforeAfterAuthenticatedisUun(int before, int after, String uun) {
        checkBeforeAfterAuthenticated(before, after, uun, VAULT_ISUUN_URL);
    }

    void checkBeforeAfterAuthenticated(int before, int after, String msg, String url) {
        String cacheKey = RateLimitingFilter.getCacheKey(AUTHENTICATED_USER, url);
        checkBeforeAfter(cacheKey, before, after, () -> makeAuthenticatedRequest(url, msg));
    }

    protected  final BucketProxy getUnAuthBucketProxy(String uriPattern) {
        String cacheKey = RateLimitingFilter.getCacheKey(UNAUTHENTICATED_USER, uriPattern);
        return getBucketProxy(cacheKey);
    }

    protected  final BucketProxy getAuthBucketProxy(String uriPattern) {
        String cacheKey = RateLimitingFilter.getCacheKey(AUTHENTICATED_USER, uriPattern);
        return getBucketProxy(cacheKey);
    }

    private BucketProxy getBucketProxy(String key) {
        return proxyManager.getProxy(key, () -> bucketConfig);
    }
    
    public final long getTokenCount(String key) {
        return proxyManager.getProxy(key, () -> bucketConfig).getAvailableTokens();
    }
    
    @SneakyThrows
    protected void checkUnAuthUrlIsTooManyRequests(String msg){
        mockMvc.perform(get(UN_AUTH_URL).param("msg",msg))
                .andExpect(status().isTooManyRequests());
    }

    @SneakyThrows
    @WithMockUser(AUTHENTICATED_USER)
    protected void checkAuthUrlIsTooManyRequests(String msg) {
        mockMvc.perform(get(AUTH_URL,msg))
                .andExpect(status().isTooManyRequests());
    }

    @SneakyThrows
    @WithMockUser(AUTHENTICATED_USER)
    protected void checkAuthVaultAutocompleteUrlIsTooManyRequests(String term) {
        mockMvc.perform(get(VAULT_AUTOCOMPLETE_URL, term))
                .andExpect(status().isTooManyRequests());
    }

    @SneakyThrows
    @WithMockUser(AUTHENTICATED_USER)
    protected void checkAuthVaultIsUunUrlIsTooManyRequests(String uun) {
        mockMvc.perform(get(VAULT_ISUUN_URL, uun))
                .andExpect(status().isTooManyRequests());
    }

    abstract void testRateLimitedAuthenticatedEndpoint();

    abstract void testRateLimitedUnAuthenticatedEndpoint();

    abstract void testRateLimitedAuthenticatedVaultIsUunEndpoint();

    abstract void testRateLimitedAuthenticatedVaultAutoCompleteEndpoint();
}