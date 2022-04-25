package org.datavaultplatform.webapp.config.shib;

import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationProvider;
import org.datavaultplatform.webapp.authentication.shib.ShibWebAuthenticationDetailsSource;
import org.datavaultplatform.webapp.services.PermissionsService;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

@Configuration
@Profile("shib")
@Import(ShibWebSecurityConfig.class)
public class ShibProfileConfig {

  /*
   <bean id="shibAuthenticationProvider" class="org.datavaultplatform.webapp.authentication.ShibAuthenticationProvider">
      <property name="restService" ref="restService" />
      <property name="ldapService" ref="ldapService" />
      <property name="permissionsService" ref="permissionsService" />
      <property name="ldapEnabled" value="${ldap.enabled}" />
   </bean>
   */
  @Bean
  ShibAuthenticationProvider shibAuthenticationProvider(
      RestService restService,
      LDAPService ldapService,
      PermissionsService permissionService,
      @Value("${ldap.enabled}") boolean ldapEnabled
  ){
    ShibAuthenticationProvider result = new ShibAuthenticationProvider();
    result.setRestService(restService);
    result.setLdapService(ldapService);
    result.setPermissionsService(permissionService);
    result.setLdapEnabled(ldapEnabled);
    return result;
  }

  /*
   <bean id="shibWebAuthenticationDetailsSource" class="org.datavaultplatform.webapp.authentication.ShibWebAuthenticationDetailsSource">
      <property name="firstnameRequestHeader" value="${shibboleth.firstname}"/>
      <property name="lastnameRequestHeader" value="${shibboleth.lastname}"/>
      <property name="emailRequestHeader" value="${shibboleth.email}"/>
   </bean>
   */
  @Bean
  ShibWebAuthenticationDetailsSource shibWebAuthenticationDetailsSource(
      @Value("${shibboleth.firstname}") String firstNameHeader,
      @Value("${shibboleth.lastname}")String lastNameHeader,
      @Value("${shibboleth.email}")String emailHeader
  ) {
    ShibWebAuthenticationDetailsSource result = new ShibWebAuthenticationDetailsSource();
    result.setFirstnameRequestHeader(firstNameHeader);
    result.setLastnameRequestHeader(lastNameHeader);
    result.setEmailRequestHeader(emailHeader);
    return result;
  }


  @Bean
  Http403ForbiddenEntryPoint http403EntryPoint() {
    return new Http403ForbiddenEntryPoint();
  }

}
