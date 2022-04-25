package org.datavaultplatform.webapp.app.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.webapp.services.NotifyLoginService;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ProfileStandalone
public class LoginTest {

  @MockBean
  NotifyLoginService mNotifyLoginService;

  @Autowired
  MockMvc mvc;

  String username = "user";

  String password = "password";

  @Test
  void testUnsecure() throws Exception {
    mvc.perform(get("/index")).andExpect(status().isOk());
    mvc.perform(get("/test/hello")).andExpect(status().isOk());
    mvc.perform(get("/error")).andExpect(status().isInternalServerError());

    Mockito.verifyNoInteractions(mNotifyLoginService);
  }

  @Test
  void testSecure() throws Exception {
    MvcResult result = mvc.perform(get("/secure")).andReturn();
    assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
    assertEquals("http://localhost/auth/login", result.getResponse().getRedirectedUrl());

    Mockito.verifyNoInteractions(mNotifyLoginService);
  }

  @Nested
  class LoginTests {

    @Captor
    ArgumentCaptor<CreateClientEvent> argClientEvent;

    ResultActions login(String username, String password) throws Exception {
      return mvc.perform(formLogin().loginProcessingUrl("/auth/security_check").user(username).password(password));
    }

    @Test
    void success() throws Exception {
      Mockito.when(mNotifyLoginService.notifyLogin(argClientEvent.capture())).thenReturn("NOTIFIED");
      Mockito.when(mNotifyLoginService.getGroups()).thenReturn(new Group[0]);

      MvcResult result =
          login(username, password)
          .andExpect(authenticated())
          .andExpect(redirectedUrl("/"))
          .andReturn();
      HttpSession session = result.getRequest().getSession();
      SecurityContext ctx = (SecurityContext) session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
      String sessionId = session.getId();
      assertEquals(username, ctx.getAuthentication().getName());
      Set<String> actualGrantedAuthorityNames = ctx.getAuthentication()
          .getAuthorities()
          .stream()
          .map(GrantedAuthority::getAuthority)
          .collect(Collectors.toSet());

      assertEquals(Collections.singleton("ROLE_USER"), actualGrantedAuthorityNames);

      Mockito.verify(mNotifyLoginService, times(1)).notifyLogin(argClientEvent.getValue());
      Mockito.verify(mNotifyLoginService, times(1)).getGroups();
      Mockito.verifyNoMoreInteractions(mNotifyLoginService);

      assertEquals(sessionId, argClientEvent.getValue().getSessionId());
    }

    @Test
    void fail() throws Exception {

      login(username, "XXXX")
          .andExpect(unauthenticated())
          .andExpect(redirectedUrl("/auth/login?error=true"));

      Mockito.verifyNoInteractions(mNotifyLoginService);
    }

  }

}
