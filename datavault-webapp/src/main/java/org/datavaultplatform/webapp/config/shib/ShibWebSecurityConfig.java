package org.datavaultplatform.webapp.config.shib;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationFilter;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationProvider;
import org.datavaultplatform.webapp.authentication.shib.ShibWebAuthenticationDetailsSource;
import org.datavaultplatform.webapp.config.HttpSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@EnableWebSecurity
@Slf4j
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class ShibWebSecurityConfig {

  @Value("${shibboleth.principal}")
  String principalRequestHeader;

  @Autowired
  SessionRegistry sessionRegistry;

  @Autowired
  ShibAuthenticationProvider shibAuthenticationProvider;

  @Autowired
  Http403ForbiddenEntryPoint http403EntryPoint;

  @Autowired
  ShibWebAuthenticationDetailsSource authenticationDetailsSource;

  /*
  @Override
  protected void configure(HttpSecurity http) throws Exception {

    // no form login for 'shib'

    HttpSecurityUtils.authorizeRequests(http);

    HttpSecurityUtils.sessionManagement(http, sessionRegistry);

    // 'shib' specific config
    http.addFilterAt(shibFilter(), AbstractPreAuthenticatedProcessingFilter.class);

    http.exceptionHandling(ex -> ex.authenticationEntryPoint(http403EntryPoint));
  }
   */

  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(
          HttpSecurity http,
          AuthenticationManager authManager
  ) throws Exception {

    // no form login for 'shib'
    http.authenticationProvider(shibAuthenticationProvider);

    HttpSecurityUtils.authorizeRequests(http);

    HttpSecurityUtils.sessionManagement(http, sessionRegistry);

    // 'shib' specific config
    ShibAuthenticationFilter shibFilter = this.shibAuthFilter(
            this.principalRequestHeader,
            this.authenticationDetailsSource,
            authManager
    );
    http.addFilterAt(shibFilter, AbstractPreAuthenticatedProcessingFilter.class);

    http.exceptionHandling(ex -> ex.authenticationEntryPoint(http403EntryPoint));
    return http.build();
  }

  /*
 <bean id="shibFilter" class="org.datavaultplatform.webapp.authentication.ShibAuthenticationFilter">
    <property name="principalRequestHeader" value="${shibboleth.principal}"/>
    <property name="exceptionIfHeaderMissing" value="true"/>
    <property name="authenticationManager" ref="authenticationManager" />
    <property name="authenticationDetailsSource" ref="shibWebAuthenticationDetailsSource" />
 </bean>
 */

  protected ShibAuthenticationFilter shibAuthFilter(
          String principalRequestHeader,
          ShibWebAuthenticationDetailsSource authDetailsSource,
          AuthenticationManager authenticationManager) {
    ShibAuthenticationFilter filter = new ShibAuthenticationFilter();
    filter.setPrincipalRequestHeader(principalRequestHeader);
    filter.setExceptionIfHeaderMissing(true);
    filter.setAuthenticationManager(authenticationManager);
    filter.setAuthenticationDetailsSource(authDetailsSource);
    return filter;
  }

  @Bean
  AuthenticationManager authenticationManager(
          AuthenticationEventPublisher publisher,
          @Qualifier("actuatorAuthenticationProvider") AuthenticationProvider authenticationProvider1
  ) {
    ProviderManager result =  new ProviderManager(authenticationProvider1,
            shibAuthenticationProvider);
    result.setAuthenticationEventPublisher(publisher);
    result.afterPropertiesSet();
    return result;
  }
}