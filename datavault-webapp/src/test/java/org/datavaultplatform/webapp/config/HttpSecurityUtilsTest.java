package org.datavaultplatform.webapp.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpSecurityUtilsTest {

    public static final Logger LOG = LoggerFactory.getLogger(HttpSecurityUtilsTest.class);

    @Captor
    ArgumentCaptor<Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>> argAuthorizeHttpRequestsCustomizer;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testHttpSecurityConfig(boolean isStandalone) throws Exception {

        HttpSecurity http = mock(HttpSecurity.class);
        HttpSecurityUtils.authorizeRequests(http, isStandalone);

        verify(http).authorizeHttpRequests(argAuthorizeHttpRequestsCustomizer.capture());

        List<Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>> values = argAuthorizeHttpRequestsCustomizer.getAllValues();
        assertEquals(1, values.size());

        var lambda = values.get(0);

        var mAuthz = Mockito.mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);

        AuthorizeHttpRequestsConfigurer.AuthorizedUrl mAuthURL = mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
        AtomicInteger counter = new AtomicInteger(0);
        doAnswer(invocation -> {
            LOG.info("requestMatchers {} {}", counter.incrementAndGet(), Arrays.toString(invocation.getArguments()));
            return mAuthURL;
        }).when(mAuthz).requestMatchers(any(String[].class));

        // pass the mock to the "captured" lambda to verify calls the lambda makes to its argument (authz)
        lambda.customize(mAuthz);

        var inOrder = inOrder(mAuthz, mAuthURL);

        //0 - OPTIONAL
        if (isStandalone) {
            inOrder.verify(mAuthz).requestMatchers("/test/**", "/index");
            inOrder.verify(mAuthURL).permitAll();
        }

        //1
        inOrder.verify(mAuthz).requestMatchers("/favicon.ico");
        inOrder.verify(mAuthURL).permitAll();

        //2
        inOrder.verify(mAuthz).requestMatchers("/resources/**");
        inOrder.verify(mAuthURL).permitAll();

        //3
        inOrder.verify(mAuthz).requestMatchers("/error");
        inOrder.verify(mAuthURL).permitAll();

        //4
        inOrder.verify(mAuthz).requestMatchers("/auth/**");
        inOrder.verify(mAuthURL).permitAll();

        //5
        inOrder.verify(mAuthz).requestMatchers("/admin/paused/deposit/toggle");
        inOrder.verify(mAuthURL).hasRole("IS_ADMIN");

        //6
        inOrder.verify(mAuthz).requestMatchers("/admin/paused/retrieve/toggle");
        inOrder.verify(mAuthURL).hasRole("IS_ADMIN");

        //7
        inOrder.verify(mAuthz).requestMatchers("/admin/paused/deposit/history");
        inOrder.verify(mAuthURL).hasRole("USER");

        //8
        inOrder.verify(mAuthz).requestMatchers("/admin/paused/retrieve/history");
        inOrder.verify(mAuthURL).hasRole("USER");

        //9
        inOrder.verify(mAuthz).requestMatchers("/admin/archivestores/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_ARCHIVESTORES");

        //10
        inOrder.verify(mAuthz).requestMatchers("/admin/billing/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_BILLING");

        //11
        inOrder.verify(mAuthz).requestMatchers("/admin/deposits/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_DEPOSITS");

        //12
        inOrder.verify(mAuthz).requestMatchers("/admin/events/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_EVENTS");

        //13
        inOrder.verify(mAuthz).requestMatchers("/admin/retentionpolicies/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_RETENTIONPOLICIES");

        //13
        inOrder.verify(mAuthz).requestMatchers("/admin/retrieves/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_RETRIEVES");

        //15
        inOrder.verify(mAuthz).requestMatchers("/admin/roles/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_ROLES");

        //16
        inOrder.verify(mAuthz).requestMatchers("/admin/schools/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_SCHOOLS");

        //17
        inOrder.verify(mAuthz).requestMatchers("/admin/vaults/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_VAULTS");

        //18
        inOrder.verify(mAuthz).requestMatchers("/admin/reviews/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN_REVIEWS");

        //19
        inOrder.verify(mAuthz).requestMatchers("/admin/");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_ADMIN");

        //20
        inOrder.verify(mAuthz).requestMatchers("/admin");
        inOrder.verify(mAuthURL).hasRole("ADMIN");

        //21
        inOrder.verify(mAuthz).requestMatchers("/**");
        inOrder.verify(mAuthURL).hasAuthority("ROLE_USER");

        inOrder.verifyNoMoreInteractions();
    }
}