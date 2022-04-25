package org.datavaultplatform.webapp.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.webapp.services.NotifyLoginService;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.datavaultplatform.webapp.test.TestUtils;
import org.datavaultplatform.webapp.test.WaitForLogoutNotificationConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Checks that when a user logs in, their session is registered in the SessionRegistry
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Import(WaitForLogoutNotificationConfig.class)
@ProfileStandalone
public class SessionRegistryUsageTest {

  @MockBean
  NotifyLoginService mNotifyLoginService;

  String username = "user";

  String password = "password";

  @Autowired
  SessionRegistry sessionRegistry;

  @Autowired
  TestRestTemplate template;

  //Used to wait for NotifyLogoutService
  @Autowired
  CountDownLatch latch;

  //Used to capture events passed to NotifyLogoutService
  @Autowired
  List<CreateClientEvent> events;

  String sessionIdOne;

  String sessionIdTwo;

  @LocalServerPort
  private int port;

  @BeforeEach
  void setup() {
    Mockito.when(mNotifyLoginService.getGroups()).thenReturn(new Group[0]);
  }

  private static ResponseEntity<String> makeGetRequestWithSession(TestRestTemplate template,
      String url, String sessionId) {
    HttpHeaders headers = new HttpHeaders();
    TestUtils.setSessionCookieHeader(headers, sessionId);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    return template.exchange(url, HttpMethod.GET, entity, String.class);
  }

  /**
   * This is a sophisticated test. It performs a sequence of actions and tests.
   * <p>
   * Given we have valid credentials
   * When we perform an initial login
   * Then the login is successful
   * And the session is registered
   * TODO - we need to add checks when we bring in AuthenticationSuccessHandler
   * <p>
   * Given we are logged in
   * When we attempt to access a secure page
   * Then we are successful
   * <p>
   * Given we are logged in
   * When we login again
   * Then the 2nd session is valid and registered
   * TODO - we need to add checks when we bring in AuthenticationSuccessHandler
   * And the initial session is invalidated
   * And the broker is notified of the 1st session logout
   * <p>
   * Given that the 2nd session is now invalid
   * When the 2nd session attempts to access a secure page
   * Then it is redirected to 'auth/login?secure'
   */
  @Test
  void testSameUserLoggedInTwice() throws InterruptedException {
    checkInitialLoginSuccessful();
    checkLoggedInUserCanAccessSecurePage(sessionIdOne, true);

    assertEquals(1, latch.getCount());
    checkNewLoginFailsOldConcurrentLoginIsExpired();
    checkLoggedInUserCanAccessSecurePage(sessionIdTwo, true);

    //check that the 'previous' session is expired and redirected back to the login page
    ResponseEntity<String> notOkay = checkLoggedInUserCanAccessSecurePage(sessionIdOne, false);
    assertEquals(notOkay.getStatusCode(), HttpStatus.FOUND);//REDIRECT
    assertEquals(TestUtils.getFullURI(port, "/auth/login?security"),
        notOkay.getHeaders().getLocation());

    checkLogoutNotified(sessionIdOne);
  }

  private void checkLogoutNotified(String sessionId) throws InterruptedException {
    latch.await(5, TimeUnit.SECONDS);
    CreateClientEvent event1 = events.get(0);
    assertEquals(sessionId, event1.getSessionId());
  }

  void checkInitialLoginSuccessful() {
    ResponseEntity<String> responseEntity = TestUtils.login(template, username, password);

    sessionIdOne = TestUtils.getSessionId(responseEntity.getHeaders());

    TestUtils.waitForSessionRegistryToHaveAnEntry(sessionRegistry);

    assertThat(sessionRegistry.getAllPrincipals()).hasSize(1);
    User principal = (User) sessionRegistry.getAllPrincipals().get(0);
    assertThat(principal.getUsername()).isEqualTo(username);

    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, true);
    assertThat(sessions).hasSize(1);
    SessionInformation registeredSession = sessions.get(0);

    assertThat(registeredSession.getSessionId()).isEqualTo(sessionIdOne);
    assertThat(registeredSession.isExpired()).isFalse();
  }

  ResponseEntity<String> checkLoggedInUserCanAccessSecurePage(String sessionId,
      boolean accessExpected) {
    ResponseEntity<String> securePage = makeGetRequestWithSession(template, "/secure", sessionId);
    boolean accessOkay = securePage.getStatusCode().is2xxSuccessful();
    assertEquals(accessExpected, accessOkay);
    return securePage;
  }

  /**
   * When the same user logs in a second time, their previous session becomes invalid.
   */
  void checkNewLoginFailsOldConcurrentLoginIsExpired() {
    ResponseEntity<String> responseEntity = TestUtils.login(template, username, password);
    sessionIdTwo = TestUtils.getSessionId(responseEntity.getHeaders());
    Assertions.assertNotEquals(sessionIdOne, sessionIdTwo);

    assertThat(sessionRegistry.getAllPrincipals()).hasSize(1);
    User principal = (User) sessionRegistry.getAllPrincipals().get(0);
    assertThat(principal.getUsername()).isEqualTo(username);

    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, true);
    assertThat(sessions).hasSize(2);

    //The old session WILL be expired
    SessionInformation session1 = findSession(sessions, sessionIdOne);
    assertTrue(session1.isExpired());

    //The new session will NOT be expired
    SessionInformation session2 = findSession(sessions, sessionIdTwo);
    assertFalse(session2.isExpired());

  }

  private SessionInformation findSession(List<SessionInformation> sessions, String sessionId) {
    return sessions.stream().filter(s -> s.getSessionId().equals(sessionId)).findFirst().get();
  }

}
