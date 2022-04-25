package org.datavaultplatform.webapp.app.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.webapp.services.NotifyLoginService;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.datavaultplatform.webapp.test.TestUtils;
import org.datavaultplatform.webapp.test.WaitForLogoutNotificationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

/**
 * Checks that a session times out.
 * The minimum timeout for tomcat is 1 minute.
 * Tomcat takes about 2 minutes to perform the timeout.
 * TODO - make sure SLOW tests like this only run in CICD
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ProfileStandalone
@TestPropertySource(properties = "server.servlet.session.timeout=1m")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Slf4j
@Import(WaitForLogoutNotificationConfig.class)
@Disabled
public class SessionTimedOutTest {

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

  @Value("${server.servlet.session.timeout}")
  String sessionTimeoutRaw;

  String sessionIdOne;

  private static ResponseEntity<String> makeGetRequestWithSession(TestRestTemplate template,
      String url, String sessionId) {
    HttpHeaders headers = new HttpHeaders();
    TestUtils.setSessionCookieHeader(headers, sessionId);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    return template.exchange(url, HttpMethod.GET, entity, String.class);
  }

  @BeforeEach
  void setup() {
    Mockito.when(mNotifyLoginService.getGroups()).thenReturn(new Group[0]);
  }

  @Test
  void testLoginUserAndWaitForSessionTimeout() throws InterruptedException {
    assertEquals("1m", sessionTimeoutRaw);
    checkInitialLoginSuccessful();
    checkLoggedInUserCanAccessSecurePage(sessionIdOne, true);

    assertEquals(1, latch.getCount());

    long start = System.currentTimeMillis();
    checkLogoutNotified(sessionIdOne, 150);
    long diff = System.currentTimeMillis() - start;
    assertTrue(diff >= 60);
    log.info("Took Tomcat [{}] to time session out - should have taken around 60",
        TimeUnit.MILLISECONDS.toSeconds(diff));
  }

  private void checkLogoutNotified(String sessionId, int seconds) throws InterruptedException {
    log.info("Waiting for Logout Notification of session {}",sessionId);
    latch.await(seconds, TimeUnit.SECONDS);
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
    ResponseEntity<String> securePage = makeGetRequestWithSession(template, "/test/hello",
        sessionId);
    boolean accessOkay = securePage.getStatusCode().is2xxSuccessful();
    assertEquals(accessExpected, accessOkay);
    return securePage;
  }

}
