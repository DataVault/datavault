package org.datavaultplatform.broker.config;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.authentication.RestAuthenticationFailureHandler;
import org.datavaultplatform.broker.authentication.RestAuthenticationFilter;
import org.datavaultplatform.broker.authentication.RestAuthenticationProvider;
import org.datavaultplatform.broker.authentication.RestAuthenticationSuccessHandler;
import org.datavaultplatform.broker.authentication.RestWebAuthenticationDetailsSource;
import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.util.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@ConditionalOnExpression("${broker.security.enabled:true}")
@EnableWebSecurity
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${spring.security.debug:false}")
  boolean securityDebug;

  @Override
  public void configure(WebSecurity web) {
    web.debug(securityDebug);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.antMatcher("/**")
    /*
    http.authorizeRequests( requests -> {
      requests.antMatchers("/actuator/info").permitAll();  //OKAY
      requests.antMatchers("/actuator/health").permitAll();  //OKAY
      requests.antMatchers("/actuator/customtime").permitAll();  //OKAY
      requests.antMatchers("/actuator","/actuator/").permitAll(); //OKAY
      requests.antMatchers("/actuator/**").hasRole("ADMIN"); //TODO - check this
      requests.antMatchers("/","/resources/**","jsondoc").permitAll();
    });
    */
        .csrf().disable()
        .addFilterAt(restFilter(), AbstractPreAuthenticatedProcessingFilter.class)
        .exceptionHandling(ex -> ex.authenticationEntryPoint(http403EntryPoint()))
        .sessionManagement(cust -> cust.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeRequests()
          /*
              <security:http pattern="/" security="none"/>
    <security:http pattern="/resources/**" security="none"/>
    <security:http pattern="/jsondoc" security="none"/>
           */
          .antMatchers("/","/resources/**","jsondoc").permitAll() //pretty sure this DOES NOT WORK
        /*
        <security:intercept-url pattern="/admin/users/**" access="hasRole('ROLE_ADMIN')" />
        <security:intercept-url pattern="/admin/archivestores/**" access="hasRole('ROLE_ADMIN_ARCHIVESTORES')" />
        <security:intercept-url pattern="/admin/deposits/**" access="hasRole('ROLE_ADMIN_DEPOSITS')" />
        <security:intercept-url pattern="/admin/retrieves/**" access="hasRole('ROLE_ADMIN_RETRIEVES')" />
        <security:intercept-url pattern="/admin/vaults/**" access="hasRole('ROLE_ADMIN_VAULTS')" />
        <security:intercept-url pattern="/admin/pendingVaults/**" access="hasRole('ROLE_ADMIN_VAULTS')" />
        <security:intercept-url pattern="/admin/events/**" access="hasRole('ROLE_ADMIN_EVENTS')" />
        <security:intercept-url pattern="/admin/billing/**" access="hasRole('ROLE_ADMIN_BILLING')" />
        <security:intercept-url pattern="/admin/reviews/**" access="hasRole('ROLE_ADMIN_REVIEWS')" />
         */
        .antMatchers("/admin/users/**").access("hasRole('ROLE_ADMIN')")
        .antMatchers("/admin/archivestores/**").access("hasRole('ROLE_ADMIN_ARCHIVESTORES')")
        .antMatchers("/admin/deposits/**").access("hasRole('ROLE_ADMIN_DEPOSITS')")
        .antMatchers("/admin/retrieves/**").access("hasRole('ROLE_ADMIN_RETRIEVES')")
        .antMatchers("/admin/vaults/**").access("hasRole('ROLE_ADMIN_VAULTS')")
        .antMatchers("/admin/pendingVaults/**").access("hasRole('ROLE_ADMIN_VAULTS')")
        .antMatchers("/admin/events/**").access("hasRole('ROLE_ADMIN_EVENTS')")
        .antMatchers("/admin/billing/**").access("hasRole('ROLE_ADMIN_BILLING')")
        /* TODO : DavidHay : no controller mapped to /admin/reviews ! */
        .antMatchers("/admin/reviews/**").access("hasRole('ROLE_ADMIN_REVIEWS')");

  }

  /**
   <bean id="restFilter" class="org.datavaultplatform.broker.authentication.RestAuthenticationFilter">
   <property name="principalRequestHeader" value="X-UserID" />
   <property name="authenticationManager" ref="authenticationManager" />
   <property name="authenticationDetailsSource" ref="restWebAuthenticationDetailsSource" />
   <property name="authenticationSuccessHandler" ref="authenticationSuccessHandler" />
   <property name="authenticationFailureHandler" ref="authenticationFailureHandler" />
   <property name="filterProcessesUrl" value="/**" />
   </bean>
   */

  RestAuthenticationFilter restFilter() throws Exception {
    RestAuthenticationFilter result = new RestAuthenticationFilter();
    result.setPrincipalRequestHeader(HEADER_USER_ID);
    result.setAuthenticationManager(authenticationManager());
    result.setAuthenticationDetailsSource(restWebAuthenticationDetailsSource());
    result.setAuthenticationSuccessHandler(authenticationSuccessHandler());
    result.setAuthenticationFailureHandler(authenticationFailureHandler());
    result.setFilterProcessesUrl("/**");
    return result;
  }

  /**
   *     <bean id="restAuthenticationProvider" class="org.datavaultplatform.broker.authentication.RestAuthenticationProvider">
   *         <property name="usersService" ref="usersService" />
   *         <property name="clientsService" ref="clientsService" />
   *         <property name="rolesAndPermissionsService" ref="rolesAndPermissionsService" />
   *         <property name="adminService" ref="adminService"/>
   *         <property name="validateClient" value="${broker.validateclient}" />
   *     </bean>
   */
  @Bean
  RestAuthenticationProvider restAuthenticationProvider(
      UsersService usersService,
      ClientsService clientsService,
      RolesAndPermissionsService rolesAndPermissionsService,
      AdminService adminService,
      @Value("${broker.validateclient}") boolean validateClient){
    RestAuthenticationProvider provider = new RestAuthenticationProvider();
    provider.setUsersService(usersService);
    provider.setClientsService(clientsService);
    provider.setRolesAndPermissionsService(rolesAndPermissionsService);
    provider.setAdminService(adminService);
    provider.setValidateClient(validateClient);
    return provider;
  }

  /*
    <bean id="restWebAuthenticationDetailsSource" class="org.datavaultplatform.broker.authentication.RestWebAuthenticationDetailsSource">
      <property name="clientKeyRequestHeader" value="X-Client-Key"/>
    </bean>
   */
  @Bean
  RestWebAuthenticationDetailsSource restWebAuthenticationDetailsSource(){
    RestWebAuthenticationDetailsSource result = new RestWebAuthenticationDetailsSource();
    result.setClientKeyRequestHeader(Constants.HEADER_CLIENT_KEY);
    return result;
  }

  /**
   <bean id="authenticationSuccessHandler" class="org.datavaultplatform.broker.authentication.RestAuthenticationSuccessHandler" />
   */
  @Bean
  RestAuthenticationSuccessHandler authenticationSuccessHandler(){
    return new RestAuthenticationSuccessHandler();
  }

  /**
   <bean id="authenticationFailureHandler" class="org.datavaultplatform.broker.authentication.RestAuthenticationFailureHandler" />
   */
  @Bean
  RestAuthenticationFailureHandler authenticationFailureHandler(){
    return new RestAuthenticationFailureHandler();
  }

  /*
    <!-- If user is not authorised for request then throw back a 403 -->
    <bean id="http403EntryPoint" class="org.springframework.security.web.authentication.Http403ForbiddenEntryPoint" />
   */
  @Bean
  Http403ForbiddenEntryPoint http403EntryPoint() {
      return new Http403ForbiddenEntryPoint();
  }
}
