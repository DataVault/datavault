package org.datavaultplatform.broker.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

class SecurityConfigTest {

    public static final Logger LOG = LoggerFactory.getLogger(SecurityConfigTest.class);
    
    @Test
    void testCustomUserDetailsService(){
        
        SecurityConfig securityConfig = new SecurityConfig();
        var customizer = securityConfig.getAuthZCustomizer();

        var mAuthz = Mockito.mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);

        AuthorizeHttpRequestsConfigurer.AuthorizedUrl mAuthURL = mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
        AtomicInteger counter = new AtomicInteger(0);
        doAnswer(invocation -> {
            LOG.info("requestMatchers {} {}", counter.incrementAndGet(), Arrays.toString(invocation.getArguments()));
            return mAuthURL;
        }).when(mAuthz).requestMatchers(any(String[].class));
        
        when(mAuthz.anyRequest()).thenReturn(mAuthURL);
        customizer.customize(mAuthz);

        var inOrder = inOrder(mAuthz, mAuthURL);

        //1
        inOrder.verify(mAuthz).requestMatchers("/admin/users/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN");

        //2
        inOrder.verify(mAuthz).requestMatchers("/admin/archivestores/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_ARCHIVESTORES");

        //3
        inOrder.verify(mAuthz).requestMatchers("/admin/deposits/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_DEPOSITS");

        //4
        inOrder.verify(mAuthz).requestMatchers("/admin/retrieves/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_RETRIEVES");

        //5
        inOrder.verify(mAuthz).requestMatchers("/admin/vaults/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_VAULTS");

        //6
        inOrder.verify(mAuthz).requestMatchers("/admin/pendingVaults/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_PENDING_VAULTS");

        //7
        inOrder.verify(mAuthz).requestMatchers("/admin/events/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_EVENTS");

        //8
        inOrder.verify(mAuthz).requestMatchers("/admin/billing/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_BILLING");

        //9
        inOrder.verify(mAuthz).requestMatchers("/admin/reviews/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_REVIEWS");

        //10
        inOrder.verify(mAuthz).requestMatchers("/admin/paused/deposit/toggle/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN");

        //11
        inOrder.verify(mAuthz).requestMatchers("/admin/paused/retrieve/toggle/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN");

        //12
        inOrder.verify(mAuthz).anyRequest();
        inOrder.verify(mAuthURL).authenticated();
        
        inOrder.verifyNoMoreInteractions();
    }
}