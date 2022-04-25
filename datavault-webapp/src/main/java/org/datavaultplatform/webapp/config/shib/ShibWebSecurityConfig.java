package org.datavaultplatform.webapp.config.shib;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationFilter;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationProvider;
import org.datavaultplatform.webapp.authentication.shib.ShibWebAuthenticationDetailsSource;
import org.datavaultplatform.webapp.config.HttpSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@EnableWebSecurity
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class ShibWebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${spring.security.debug:false}")
  boolean securityDebug;

  @Autowired
  SessionRegistry sessionRegistry;

  @Autowired
  ShibAuthenticationProvider shibAuthenticationProvider;

  @Autowired
  Http403ForbiddenEntryPoint http403EntryPoint;

  @Autowired
  ShibWebAuthenticationDetailsSource authDetailsSource;

  @Value("${shibboleth.principal}")
  String principalRequestHeader;

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.debug(securityDebug);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    // no form login for 'shib'

    HttpSecurityUtils.authorizeRequests(http);

    HttpSecurityUtils.sessionManagement(http, sessionRegistry);

    // 'shib' specific config
    http.addFilterAt(shibFilter(), AbstractPreAuthenticatedProcessingFilter.class);

    http.exceptionHandling(ex -> ex.authenticationEntryPoint(http403EntryPoint));
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(shibAuthenticationProvider);
  }

  /*
 <bean id="shibFilter" class="org.datavaultplatform.webapp.authentication.ShibAuthenticationFilter">
    <property name="principalRequestHeader" value="${shibboleth.principal}"/>
    <property name="exceptionIfHeaderMissing" value="true"/>
    <property name="authenticationManager" ref="authenticationManager" />
    <property name="authenticationDetailsSource" ref="shibWebAuthenticationDetailsSource" />
 </bean>
 */
  @Bean
  ShibAuthenticationFilter shibFilter() throws Exception {
    ShibAuthenticationFilter filter = new ShibAuthenticationFilter();
    filter.setPrincipalRequestHeader(principalRequestHeader);
    filter.setExceptionIfHeaderMissing(true);
    filter.setAuthenticationManager(authenticationManager());
    filter.setAuthenticationDetailsSource(authDetailsSource);
    return filter;
  }

}