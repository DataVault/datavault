package org.datavaultplatform.broker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.authentication.*;
import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.config.SecurityMethod;
import org.datavaultplatform.common.util.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

@SuppressWarnings("DefaultAnnotationParam")
@ConditionalOnExpression("${broker.security.enabled:true}")
@Configuration
@EnableWebSecurity
@Slf4j
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

  @Value("${spring.security.debug:false}")
  boolean securityDebug;

  @Bean
  WebSecurityCustomizer webSecurityCustomizer() {
    return web -> {
      web.debug(securityDebug);
      web.ignoring().requestMatchers("/retrieve/**");
    };
  }

  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(
          HttpSecurity http,
          RestAuthenticationProvider restAuthenticationProvider,
          AuthenticationManager authenticationManager
  ) throws Exception {

    http.securityMatcher("/**")
      .csrf(AbstractHttpConfigurer::disable)
      .authenticationProvider(restAuthenticationProvider)
      .addFilterAt(restFilter(authenticationManager), AbstractPreAuthenticatedProcessingFilter.class)
      .sessionManagement(cust -> cust.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(ex -> ex.authenticationEntryPoint(http403EntryPoint()))
      .authorizeHttpRequests(getAuthZCustomizer());

    return http.build();
  }

  public static final Map<String,String> SECURITY_PATH_MAP = new LinkedHashMap<>();
  
  static {
    SECURITY_PATH_MAP.put("/admin/users/**", "hasAuthority('ROLE_ADMIN')");
    SECURITY_PATH_MAP.put("/admin/archivestores/**", "hasAuthority('ROLE_ADMIN_ARCHIVESTORES')");
    SECURITY_PATH_MAP.put("/admin/deposits/**", "hasAuthority('ROLE_ADMIN_DEPOSITS')");
    SECURITY_PATH_MAP.put("/admin/retrieves/**", "hasAuthority('ROLE_ADMIN_RETRIEVES')");
    SECURITY_PATH_MAP.put("/admin/vaults/**", "hasAuthority('ROLE_ADMIN_VAULTS')");
    SECURITY_PATH_MAP.put("/admin/pendingVaults/**", "hasAuthority('ROLE_ADMIN_PENDING_VAULTS')");
    SECURITY_PATH_MAP.put("/admin/events/**", "hasAuthority('ROLE_ADMIN_EVENTS')");
    SECURITY_PATH_MAP.put("/admin/billing/**", "hasAuthority('ROLE_ADMIN_BILLING')");
    /* TODO : DavidHay : no controller mapped to /admin/reviews ! */
    SECURITY_PATH_MAP.put("/admin/reviews/**", "hasAuthority('ROLE_ADMIN_REVIEWS')");
    SECURITY_PATH_MAP.put("/admin/paused/deposit/toggle/**", "hasAuthority('ROLE_ADMIN')");
    SECURITY_PATH_MAP.put("/admin/paused/retrieve/toggle/**", "hasAuthority('ROLE_ADMIN')");
  }
  
  public Customizer<org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> getAuthZCustomizer() {
    return authz -> {

      for(Map.Entry<String, String> entry : SECURITY_PATH_MAP.entrySet()) {
        var matchers = authz.requestMatchers(entry.getKey());
        SecurityMethod sm = SecurityMethod.from(entry.getValue());
        if (sm.isPermitAll()) {
          matchers.permitAll();

        } else if (sm.isHasRole()) {
          matchers.hasRole(sm.arg());

        } else if (sm.isHasAuthority()) {
          matchers.hasAuthority(sm.arg());

        } else {
          throw new RuntimeException("Unknown security method: " + sm.method());
        }
      }
      authz.anyRequest().authenticated();

//      authz
//              .requestMatchers("/admin/users/**").hasAuthority("ROLE_ADMIN")
//              .requestMatchers("/admin/archivestores/**").hasAuthority("ROLE_ADMIN_ARCHIVESTORES")
//              .requestMatchers("/admin/deposits/**").hasAuthority("ROLE_ADMIN_DEPOSITS")
//              .requestMatchers("/admin/retrieves/**").hasAuthority("ROLE_ADMIN_RETRIEVES")
//              .requestMatchers("/admin/vaults/**").hasAuthority("ROLE_ADMIN_VAULTS")
//              .requestMatchers("/admin/pendingVaults/**").hasAuthority("ROLE_ADMIN_PENDING_VAULTS")
//              .requestMatchers("/admin/events/**").hasAuthority("ROLE_ADMIN_EVENTS")
//              .requestMatchers("/admin/billing/**").hasAuthority("ROLE_ADMIN_BILLING")
//              /* TODO : DavidHay : no controller mapped to /admin/reviews ! */
//              .requestMatchers("/admin/reviews/**").hasAuthority("ROLE_ADMIN_REVIEWS")
//              .requestMatchers("/admin/paused/deposit/toggle/**").hasAuthority("ROLE_ADMIN")
//              .requestMatchers("/admin/paused/retrieve/toggle/**").hasAuthority("ROLE_ADMIN")
//              .anyRequest().authenticated();
    };
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

  private RestAuthenticationFilter restFilter(
          AuthenticationManager authenticationManager) throws Exception {
    RequestMatcher matcher = request -> {
      log.info("FILTER PROCESSING {}", request.getPathInfo());
      return true;
    };
    RestAuthenticationFilter result = new RestAuthenticationFilter(matcher);
    result.setPrincipalRequestHeader(HEADER_USER_ID);
    result.setAuthenticationManager(authenticationManager);
    result.setAuthenticationDetailsSource(restWebAuthenticationDetailsSource());
    result.setAuthenticationSuccessHandler(authenticationSuccessHandler());
    result.setAuthenticationFailureHandler(authenticationFailureHandler());
    //result.setFilterProcessesUrl("/**");
    RequestMatcher actualRM = result.getRequestMatcher();
    Assert.isTrue(matcher.equals(actualRM), () -> String.format("Request Matcher [%s] has been overridden ", actualRM));
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

  RestWebAuthenticationDetailsSource restWebAuthenticationDetailsSource(){
    RestWebAuthenticationDetailsSource result = new RestWebAuthenticationDetailsSource();
    result.setClientKeyRequestHeader(Constants.HEADER_CLIENT_KEY);
    return result;
  }

  /**
   <bean id="authenticationSuccessHandler" class="org.datavaultplatform.broker.authentication.RestAuthenticationSuccessHandler" />
   */

  RestAuthenticationSuccessHandler authenticationSuccessHandler(){
    return new RestAuthenticationSuccessHandler();
  }

  /**
   <bean id="authenticationFailureHandler" class="org.datavaultplatform.broker.authentication.RestAuthenticationFailureHandler" />
   */

  RestAuthenticationFailureHandler authenticationFailureHandler(){
    return new RestAuthenticationFailureHandler();
  }

  /*
    <!-- If user is not authorised for request then throw back a 403 -->
    <bean id="http403EntryPoint" class="org.springframework.security.web.authentication.Http403ForbiddenEntryPoint" />
   */

  Http403ForbiddenEntryPoint http403EntryPoint() {
      return new Http403ForbiddenEntryPoint();
  }

  @Bean
  @Order(1)
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    log.info("creating logging filter");
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setIncludeHeaders(true);
    loggingFilter.setMaxPayloadLength(100_000);
    return loggingFilter;
  }

  @Component
  @Order(2)
  static class PrincipalLoggingFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      log.debug("AUTH IS {}",auth);
      chain.doFilter(request, response);
    }
  }

  @Bean
  AuthenticationManager authenticationManager(
          AuthenticationEventPublisher eventPublisher,
          @Qualifier("actuatorAuthenticationProvider") AuthenticationProvider authenticationProvider1,
          @Qualifier("restAuthenticationProvider") AuthenticationProvider authenticationProvider2
  ) {
    ProviderManager result = new ProviderManager(authenticationProvider1, authenticationProvider2);
    result.setAuthenticationEventPublisher(eventPublisher);
    result.afterPropertiesSet();
    return result;
  }
}
