package org.datavaultplatform.webapp.config;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.services.LDAPService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
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
  List<String> attrs;

  @Bean
  LDAPService ldapService() {
    log.info("ldap.host[{}]", host);
    log.info("ldap.port[{}]", port);
    log.info("ldap.useSsl[{}]", useSSL);
    log.info("ldap.dn[{}]", dn);
    log.info("ldap.searchContext[{}]", searchContext);
    log.info("ldap.searchFilter[{}]", searchFilter);
    log.info("ldap.attrs[{}]", attrs);
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
