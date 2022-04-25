package org.datavaultplatform.webapp.app.authentication.shib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationListener;
import org.datavaultplatform.webapp.authentication.shib.ShibGrantedAuthorityService;
import org.datavaultplatform.webapp.authentication.shib.ShibUtils;
import org.datavaultplatform.webapp.authentication.shib.ShibWebAuthenticationDetails;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.test.ProfileShib;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ProfileShib
@Slf4j
public class LoginUsingShibTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ShibAuthenticationListener mAuthListener;

    @MockBean
    RestService mRestService;

    @MockBean
    LDAPService mLdapService;

    @MockBean
    ShibGrantedAuthorityService mGrantedAuthorityService;

    @Mock
    GrantedAuthority mGA1;

    @Mock
    GrantedAuthority mGA2;

    @Mock
    User mUser;

    @Captor
    ArgumentCaptor<AuthenticationSuccessEvent> argAuthSuccessEvent;

    @Test
    void testNoUidHeader()  {
        assertNotNull(mvc);

        PreAuthenticatedCredentialsNotFoundException ex = assertThrows(PreAuthenticatedCredentialsNotFoundException.class, ()-> {
            mvc.perform(get("/")).andDo(print()).andReturn();
        });
        assertEquals("uid header not found in request.", ex.getMessage());
    }


    @Test
    void testShibLoginExistingUser() throws Exception {

        ArgumentCaptor<String> argName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ValidateUser> argValidateUser = ArgumentCaptor.forClass(ValidateUser.class);
        ArgumentCaptor<Authentication> argAuthentication = ArgumentCaptor.forClass(Authentication.class);

        when(mRestService.userExists(argValidateUser.capture())).thenReturn(true);

        when(mGrantedAuthorityService.getGrantedAuthoritiesForUser(argName.capture(), argAuthentication.capture())).thenReturn(Arrays.asList(mGA1, mGA2));

        MockHttpSession session = new MockHttpSession();
        String sessionId = session.changeSessionId();

        doNothing().when(mAuthListener).onApplicationEvent(argAuthSuccessEvent.capture());

        MvcResult result = getWelcomePage(session);

        checkRedirect(result);

        assertEquals("shib-user-1",argValidateUser.getValue().getUserid());
        assertEquals("N/A",argValidateUser.getValue().getPassword());
        Mockito.verify(mRestService).userExists(argValidateUser.getValue());

        assertEquals("shib-user-1", argName.getValue());
        assertEquals("shib-user-1", argAuthentication.getValue().getName());
        assertEquals("N/A", argAuthentication.getValue().getCredentials());
        Mockito.verify(mGrantedAuthorityService).getGrantedAuthoritiesForUser(argName.getValue(), argAuthentication.getValue());

        checkLoggedInUser(result, sessionId, new HashSet<>(Arrays.asList(ShibUtils.ROLE_USER, mGA1, mGA2)));

        Mockito.verify(mAuthListener).onApplicationEvent(argAuthSuccessEvent.getValue());
        PreAuthenticatedAuthenticationToken auth = (PreAuthenticatedAuthenticationToken)argAuthSuccessEvent.getValue().getAuthentication();
        assertEquals("shib-user-1", auth.getName());
    }

    @Test
    void testShibLoginNewUser() throws Exception {

        ArgumentCaptor<String> argId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> argUser = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<ValidateUser> argValidateUser = ArgumentCaptor.forClass(ValidateUser.class);

        when(mRestService.userExists(argValidateUser.capture())).thenReturn(false);

        when(mRestService.addUser(argUser.capture())).thenReturn(mUser);

        doNothing().when(mLdapService).closeConnection();
        doNothing().when(mLdapService).getConnection();
        HashMap<String,String> ldapInfo = new HashMap<>();
        when(mLdapService.search(argId.capture())).thenReturn(ldapInfo);

        MockHttpSession session = new MockHttpSession();
        String sessionId = session.changeSessionId();

        doNothing().when(mAuthListener).onApplicationEvent(argAuthSuccessEvent.capture());

        MvcResult result = getWelcomePage(session);

        checkRedirect(result);

        verify(mRestService).userExists(argValidateUser.getValue());
        assertEquals("shib-user-1",argValidateUser.getValue().getUserid());
        assertEquals("N/A",argValidateUser.getValue().getPassword());

        verify(mLdapService).getConnection();
        verify(mLdapService).search(argId.getValue());
        verify(mLdapService).closeConnection();
        assertEquals("shib-user-1", argId.getValue());

        verify(mRestService).addUser(argUser.getValue());
        assertEquals(ldapInfo, argUser.getValue().getProperties());
        assertEquals("shib-user-1", argUser.getValue().getID());
        assertEquals("James", argUser.getValue().getFirstname());
        assertEquals("Bond", argUser.getValue().getLastname());
        assertEquals("james.bond@test.com", argUser.getValue().getEmail());
        assertNull(argUser.getValue().getPassword());
        assertNull(argUser.getValue().getFileStores());
        assertNull(argUser.getValue().getVaults());

        checkLoggedInUser(result, sessionId, Collections.singleton(ShibUtils.ROLE_USER));


        Mockito.verify(mAuthListener).onApplicationEvent(argAuthSuccessEvent.getValue());
        PreAuthenticatedAuthenticationToken auth = (PreAuthenticatedAuthenticationToken)argAuthSuccessEvent.getValue().getAuthentication();
        assertEquals("shib-user-1", auth.getName());
    }

    void checkLoggedInUser(MvcResult result, String expectedSessionId, Set<GrantedAuthority> expectedAuthorities){
        SecurityContext sc = (SecurityContext) result.getRequest().getSession().getAttribute(ShibUtils.SPRING_SECURITY_CONTEXT);
        Authentication auth = sc.getAuthentication();

        assertTrue(auth instanceof PreAuthenticatedAuthenticationToken);

        PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken)auth;
        assertEquals("shib-user-1", token.getName());
        assertEquals("shib-user-1", token.getPrincipal());
        assertEquals("N/A", token.getCredentials());

        assertTrue(token.getDetails() instanceof ShibWebAuthenticationDetails);
        ShibWebAuthenticationDetails details = (ShibWebAuthenticationDetails)token.getDetails();
        assertEquals(expectedSessionId, details.getSessionId());
        assertEquals("james.bond@test.com", details.getEmail());
        assertEquals("James", details.getFirstname());
        assertEquals("Bond", details.getLastname());
        assertEquals("127.0.0.1", details.getRemoteAddress());

        assertEquals(expectedAuthorities,
                new HashSet<>(token.getAuthorities()));

    }

    private MvcResult getWelcomePage(MockHttpSession session) throws Exception {
        return mvc.perform(get("/")
                        .session(session)
                        .header("uid", "shib-user-1")
                        .header("givenName", "James")
                        .header("sn", "Bond")
                        .header("mail", "james.bond@test.com"))
                .andDo(print()).andReturn();
    }


    @AfterEach
    void tearDown() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
        Mockito.verify(mGA1, atLeast(0)).getAuthority();
        Mockito.verify(mGA2, atLeast(0)).getAuthority();
        Mockito.verifyNoMoreInteractions(mRestService, mLdapService, mUser, mGA1, mGA2, mAuthListener);
    }

    void checkRedirect(MvcResult result) {
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals("/vaults", result.getResponse().getRedirectedUrl());
    }
}
