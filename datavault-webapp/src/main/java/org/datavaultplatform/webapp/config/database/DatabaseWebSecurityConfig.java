package org.datavaultplatform.webapp.config.database;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.authentication.AuthenticationSuccess;
import org.datavaultplatform.webapp.authentication.database.DatabaseAuthenticationProvider;
import org.datavaultplatform.webapp.config.HttpSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;

@EnableWebSecurity
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class DatabaseWebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${spring.security.debug:false}")
  boolean securityDebug;

  @Autowired
  SessionRegistry sessionRegistry;

  @Autowired
  AuthenticationSuccess authenticationSuccess;

  @Autowired
  DatabaseAuthenticationProvider authenticationProvider;

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.debug(securityDebug);
    //TODO - do we need to do this?
    //web.expressionHandler(expressionHandler);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authenticationProvider);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    HttpSecurityUtils.formLogin(http, authenticationSuccess);

    HttpSecurityUtils.authorizeRequests(http);

    HttpSecurityUtils.sessionManagement(http, sessionRegistry);

  }

}