package org.datavaultplatform.webapp.config;

import org.datavaultplatform.common.services.LDAPService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LdapConfig {

  @Value("${ldap.host}")
  String host;

  @Value("${ldap.port}")
  int port;

  @Value("${ldap.useSsl}")
  boolean useSSL;

  @Value("${ldap.dn}")
  String dn;

  @Value("${ldap.password}")
  String password;

  @Value("${ldap.searchContext}")
  String searchContext;

  @Value("${ldap.searchFilter}")
  String searchFilter;

  @Value("${ldap.attrs}")
  String attrs;

  @Bean
  LDAPService ldapService() {
    return LDAPService.builder()
        .host(host)
        .port(port)
        .useSSL(useSSL)
        .dn(dn)
        .password(password)
        .searchContext(searchContext)
        .searchFilter(searchFilter)
        .attrs(attrs)
        .build();
  }

}
