package org.datavaultplatform.broker.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.authentication.BaseControllerAuthTest.MyMvcConfigurer;
import org.datavaultplatform.broker.config.MockRabbitConfig;
import org.datavaultplatform.broker.config.MockServicesConfig;
import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.RoleName;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@TestPropertySource(properties = {
    "broker.controllers.enabled=true",
    //we have to pull in original controllers so we can override them
    "broker.properties.enabled=true",
    "broker.security.enabled=true",
    "broker.scheduled.enabled=false",
    "broker.initialise.enabled=false",
    "broker.services.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.database.enabled=false",
    "broker.email.enabled=true",
    "broker.ldap.enabled=false"})
@Import({MockServicesConfig.class, MyMvcConfigurer.class, MockRabbitConfig.class}) //spring security relies on services
@Slf4j
@AutoConfigureMockMvc
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
public abstract class BaseControllerAuthTest {

  public static final String USER_ID_1 = "test-user-01";
  public static final String API_KEY_1 = "api-key-01";
  public static final String IP_ADDRESS = "1.2.3.4";
  @Autowired
  protected MockMvc mvc;
  @Autowired
  protected UsersService mUserService;

  @Autowired
  protected AdminService mAdminService;

  @Autowired
  protected RolesAndPermissionsService mRolesAndPermissionService;
  @Autowired
  protected ClientsService mClientService;
  @Mock
  User mLoginUser;
  @Autowired
  ObjectMapper mapper;

  protected Client clientForIp(String ipAddress) {
    Client result = new Client();
    result.setIpAddress(ipAddress);
    return result;
  }

  protected MockHttpServletRequestBuilder setupAuthentication(MockHttpServletRequestBuilder builder) {
    return builder.with(req -> {
          req.setRemoteAddr(IP_ADDRESS);
          return req;
        })
        .header(Constants.HEADER_CLIENT_KEY, API_KEY_1)
        .header(Constants.HEADER_USER_ID, USER_ID_1);
  }

  protected final void checkWorksWhenAuthenticatedFailsOtherwise(
      MockHttpServletRequestBuilder requestBuilder, Object expectedResponse,
      Permission... permissions) {
    this.checkWorksWhenAuthenticatedFailsOtherwise(requestBuilder, expectedResponse, HttpStatus.OK,
        false, permissions);
  }

  /*
    invokes the endpoint described by requestBuilder 2 times - with authentication and without.
    Without authentication should fail, with authentication should succeed with expectedResponse as json
    NOTE : you still have to mock the controller method associated with endpoint to return expectedResponse
   */
  protected final void checkWorksWhenAuthenticatedFailsOtherwise(
      MockHttpServletRequestBuilder requestBuilder, Object expectedResponse,
      HttpStatus expectedSuccessStatus, boolean isAdminUser, Permission... permissions) {

    //CHECK UNAUTHENTICATED
    checkUnauthorizedWhenNotAuthenticated(requestBuilder);

    //CHECK AUTHENTICATED
    checkSuccessWhenAuthenticated(requestBuilder, expectedResponse, expectedSuccessStatus,
        isAdminUser, permissions);

  }

  protected Set<Permission> getPermissions(Permission... permissions) {
    return new HashSet<>(Arrays.asList(permissions));
  }

  @SneakyThrows
  protected void checkSuccessWhenAuthenticated(MockHttpServletRequestBuilder builder,
      Object expectedSuccessResponse, HttpStatus expectedSuccessStatus, boolean isAdminUser,
      Permission... permissions) {

    when(mClientService.getClientByApiKey(API_KEY_1)).thenReturn(clientForIp(IP_ADDRESS));
    when(mUserService.getUser(USER_ID_1)).thenReturn(mLoginUser);
    when(mAdminService.isAdminUser(mLoginUser)).thenReturn(isAdminUser);
    when(mLoginUser.getID()).thenReturn(USER_ID_1);
    when(mRolesAndPermissionService.getUserPermissions(USER_ID_1)).thenReturn(
        getPermissions(permissions));

    ResultActions resultActions = mvc.perform(setupAuthentication(builder))
        .andDo(print())
        .andExpect(status().is(expectedSuccessStatus.value()));

    if (expectedSuccessResponse == null) {
      resultActions.andExpect(content().string(""));
    } else {
      //an expected response of type String means we should have 'text/plain' response
      if (expectedSuccessResponse instanceof String) {
        resultActions
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string(expectedSuccessResponse.toString()));
      } else {
        resultActions
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(mapper.writeValueAsString(expectedSuccessResponse)));
      }
    }

    verify(mClientService).getClientByApiKey(API_KEY_1);
    verify(mUserService).getUser(USER_ID_1);
    verify(mAdminService).isAdminUser(mLoginUser);
    verify(mLoginUser, atLeast(1)).getID();
    verify(mRolesAndPermissionService, atLeast(1)).getUserPermissions(USER_ID_1);
    verifyNoMoreInteractions(mClientService, mUserService, mAdminService, mLoginUser);
  }

  @SneakyThrows
  private void checkUnauthorizedWhenNotAuthenticated(MockHttpServletRequestBuilder builder) {
    mvc.perform(builder)
        .andDo(print())
        .andExpect(status().isUnauthorized());
    verify(mClientService).getClientByApiKey(null);
    verifyNoMoreInteractions(mClientService, mUserService, mAdminService, mLoginUser);
  }

  @Configuration
  static class MyMvcConfigurer implements WebMvcConfigurer {

    public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(new HandlerInterceptor() {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
          SECURITY_CONTEXT_TL.set(SecurityContextHolder.getContext());
          log.info("IN INTERCEPTOR {}", SECURITY_CONTEXT_TL.get());
          return true;
        }
      });
    }
  }

  public static final ThreadLocal<SecurityContext> SECURITY_CONTEXT_TL = new ThreadLocal<>();

  public void checkSecurityRoles(String... roles) {
    Set<String> expectedRoles = getExpectedRoles(roles);
    Set<String> actualRoles = getActualRoles();
    assertEquals(expectedRoles, actualRoles);
  }

  private Set<String> getExpectedRoles(String... roles){
    return Arrays.stream(roles).collect(Collectors.toSet());
  }

  protected Set<String> getActualRoles(){
    SecurityContext sc = SECURITY_CONTEXT_TL.get();
    if (sc == null) {
      return Collections.emptySet();
    }
    Authentication auth = sc.getAuthentication();
    if (auth == null) {
      return Collections.emptySet();
    }
    return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
  }

  public void checkHasSecurityRoles(String... roles) {
    Set<String> expectedRoles = getExpectedRoles(roles);
    Set<String> actualRoles = getActualRoles();
    assertTrue(actualRoles.containsAll(expectedRoles));
  }

  public void checkHasSecurityUserAndClientUserRolesOnly() {
    checkSecurityRoles(RoleName.ROLE_USER, RoleName.ROLE_CLIENT_USER);
  }

  protected void checkHasSecurityUserAndClientUserRoles() {
    checkHasSecurityRoles(RoleName.ROLE_USER, RoleName.ROLE_CLIENT_USER);
  }

  public void checkHasSecurityAdminRole() {
    checkHasSecurityRoles(RoleName.ROLE_ADMIN, RoleName.ROLE_CLIENT_USER);
  }

  public void checkHasSecurityAdminArchiveStoresRole() {
    checkHasSecurityUserAndClientUserRoles();
    checkHasSecurityRoles(RoleName.ROLE_ADMIN_ARCHIVESTORES);
  }

  public void checkHasSecurityAdminDepositsRole() {
    checkHasSecurityUserAndClientUserRoles();
    checkHasSecurityRoles(RoleName.ROLE_ADMIN_DEPOSITS);
  }

  public void checkHasSecurityAdminRetrievesRole() {
    checkHasSecurityUserAndClientUserRoles();
    checkHasSecurityRoles(RoleName.ROLE_ADMIN_RETRIEVES);
  }

  public void checkHasSecurityAdminVaultsRole() {
    checkHasSecurityUserAndClientUserRoles();
    checkHasSecurityRoles(RoleName.ROLE_ADMIN_VAULTS);
  }
  public void checkHasSecurityAdminPendingVaultsRole() {
    checkHasSecurityUserAndClientUserRoles();
    checkHasSecurityRoles(RoleName.ROLE_ADMIN_PENDING_VAULTS);
  }

  public void checkHasSecurityAdminEventsRole() {
    checkHasSecurityUserAndClientUserRoles();
    checkHasSecurityRoles(RoleName.ROLE_ADMIN_EVENTS);
  }

  public void checkHasSecurityAdminBillingRole() {
    checkHasSecurityUserAndClientUserRoles();
    checkHasSecurityRoles(RoleName.ROLE_ADMIN_BILLING);
  }

  @BeforeEach
  void setup() {
    SECURITY_CONTEXT_TL.remove();
  }
}
