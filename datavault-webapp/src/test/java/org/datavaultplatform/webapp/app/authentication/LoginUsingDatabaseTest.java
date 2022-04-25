package org.datavaultplatform.webapp.app.authentication;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

import java.util.Collections;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.datavaultplatform.webapp.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ProfileDatabase
public class LoginUsingDatabaseTest {

  @LocalServerPort
  int localServerPort;

  @Autowired
  TestRestTemplate template;

  @MockBean
  RestService mRestService;

  @Captor
  ArgumentCaptor<ValidateUser> argValidateUser;

  @Captor
  ArgumentCaptor<ValidateUser> argValidateUser2;

  @Captor
  ArgumentCaptor<CreateClientEvent> argCreateClientEvent;

  @Autowired
  SessionRegistry registry;

  @Test
  void testLoginUsesRestService() {

    assertThat(registry.getAllPrincipals()).isEmpty();

    Mockito.when(mRestService.isValid(argValidateUser.capture())).thenReturn(true);
    Mockito.when(mRestService.getGroups()).thenReturn(new Group[0]);
    Mockito.when(mRestService.getRoleAssignmentsForUser("dbuser")).thenReturn(Collections.emptyList());
    Mockito.when(mRestService.isAdmin(argValidateUser2.capture())).thenReturn(false);
    Mockito.when(mRestService.notifyLogin(argCreateClientEvent.capture())).thenReturn("NotifiedXXX");

    ResponseEntity<String> responseEntity = TestUtils.login(template, "dbuser", "dbpassword");

    String sessionId = TestUtils.getSessionId(responseEntity.getHeaders());

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(responseEntity.getHeaders().getFirst("Location")).isEqualTo(String.format("http://localhost:%d/",localServerPort));

    assertEquals("dbuser", argValidateUser.getValue().getUserid());
    assertEquals("dbpassword", argValidateUser.getValue().getPassword());

    assertEquals("dbuser", argValidateUser2.getValue().getUserid());
    assertEquals(null, argValidateUser2.getValue().getPassword());

    assertEquals(sessionId, argCreateClientEvent.getValue().getSessionId());

    Mockito.verify(mRestService).isValid(argValidateUser.getValue());
    Mockito.verify(mRestService).getGroups();

    //TODO : we might want to look into why getRoleAssignmentsForUser is called twice
    Mockito.verify(mRestService, times(2)).getRoleAssignmentsForUser("dbuser");

    Mockito.verify(mRestService).isAdmin(argValidateUser2.getValue());
    Mockito.verify(mRestService).notifyLogin(argCreateClientEvent.getValue());

    Mockito.verifyNoMoreInteractions(mRestService);

    TestUtils.waitForSessionRegistryToHaveAnEntry(registry);

    assertThat(registry.getAllPrincipals()).hasSize(1);

    SessionInformation info = registry.getSessionInformation(
        sessionId);
    assertEquals("dbuser", info.getPrincipal());
  }
}
