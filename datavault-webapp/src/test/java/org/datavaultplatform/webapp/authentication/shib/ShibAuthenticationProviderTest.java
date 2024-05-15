package org.datavaultplatform.webapp.authentication.shib;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.webapp.security.ScopedGrantedAuthority;
import org.datavaultplatform.webapp.services.RestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIterable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShibAuthenticationProviderTest {

    @Mock
    RestService mRestService;
    
    @Mock
    LDAPService mLdapService;
    
    @Mock
    ShibGrantedAuthorityService mShibGrantedAuthorityService;
    
    @Mock
    ShibWebAuthenticationDetails mDetails;
    
    @Captor
    ArgumentCaptor<User> argUser;        

    final GrantedAuthority ga1 = new SimpleGrantedAuthority("role1");
    final GrantedAuthority ga2 = new SimpleGrantedAuthority("role2");
    final GrantedAuthority ga3 = new SimpleGrantedAuthority("role3");

    @Captor
    ArgumentCaptor<ValidateUser> argValidateUser;

    ShibAuthenticationProvider provider;
            
    @BeforeEach
    void setup(){
        provider = new ShibAuthenticationProvider(mRestService, mLdapService, true, mShibGrantedAuthorityService);
        lenient().when(mDetails.getEmail()).thenReturn("test@test.com");
        lenient().when(mDetails.getFirstname()).thenReturn("first");
        lenient().when(mDetails.getLastname()).thenReturn("last");
    }

    @Nested
    class UserExists {

        @Test
        void testExisingUserValidGrantedAuthorities1() {
            checkGrantedAuthoritiesForUser(List.of(ga1, ga2, ga3), List.of(ga1, ga2, ga3, ShibUtils.ROLE_USER));
        }

        @Test
        void testExisingUserValidGrantedAuthorities2() {
            checkGrantedAuthoritiesForUser(null, List.of(ShibUtils.ROLE_USER));
        }

        @Test
        void testExisingUserInvalidGrantedAuthorities1() {
            ArrayList<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(null);
            checkInvalidGrantedAuthorities(authorities, "Cannot have a null GrantedAuthority");
        }

        @Test
        void testExisingUserInvalidGrantedAuthorities2() {
            ArrayList<GrantedAuthority> authorities = new ArrayList<>();
            GrantedAuthority ga = new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return null;
                }

                @Override
                public String toString() {
                    return "test-ga";
                }
            };
            authorities.add(ga);
            checkInvalidGrantedAuthorities(authorities, "Cannot have a GrantedAuthority [test-ga] with a null Authority");
        }

        @Test
        void testExisingUserInvalidGrantedAuthorities3() {
            ArrayList<GrantedAuthority> authorities = new ArrayList<>();
            GrantedAuthority ga = new ScopedGrantedAuthority(Vault.class, "vault-id", Collections.emptyList()) {
                @Override
                public String getAuthority() {
                    return null;
                }
            };
            authorities.add(ga);
            checkInvalidGrantedAuthorities(authorities, "Cannot have a GrantedAuthority [ScopedGrantedAuthority(type=class org.datavaultplatform.common.model.Vault, id=vault-id, permissions=[])] with a null Authority");
        }

        private void checkInvalidGrantedAuthorities(List<GrantedAuthority> userAuthorities, String expectedError) {

            Collection<GrantedAuthority> authorities = Collections.emptyList();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("test-user-id", null, authorities);
            authentication.setDetails(mDetails);

            when(mRestService.userExists(argValidateUser.capture())).thenReturn(true);

            doReturn(userAuthorities).when(mShibGrantedAuthorityService).getGrantedAuthoritiesForUser("test-user-id", authentication);

            Exception ex = assertThrows(Exception.class, () -> provider.authenticate(authentication));
            assertThat(ex).hasMessage(expectedError);
        }

        private void checkGrantedAuthoritiesForUser(List<GrantedAuthority> userAuthorities, List<GrantedAuthority> grantedAuthorities) {
 
            Collection<GrantedAuthority> authorities = Collections.emptyList();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("test-user-id", null, authorities);
            authentication.setDetails(mDetails);
            
            when(mRestService.userExists(argValidateUser.capture())).thenReturn(true);

            when(mShibGrantedAuthorityService.getGrantedAuthoritiesForUser("test-user-id", authentication)).thenReturn(userAuthorities);

            Authentication result = provider.authenticate(authentication);

            assertThat(result).isInstanceOfSatisfying(PreAuthenticatedAuthenticationToken.class, preAuthToken -> {
                assertThat(preAuthToken).isNotNull();
                assertThat(preAuthToken.isAuthenticated()).isTrue();
                assertThat(preAuthToken.getPrincipal()).isEqualTo("test-user-id");
                assertThat(preAuthToken.getCredentials()).isEqualTo("N/A");
                assertThat(preAuthToken.getDetails()).isNull();
                assertThatIterable(preAuthToken.getAuthorities()).containsAll(grantedAuthorities);
            });

            ValidateUser actualUser = argValidateUser.getValue();
            assertThat(actualUser.getUserid()).isEqualTo("test-user-id");
            assertThat(actualUser.getPassword()).isEqualTo("N/A");

            verify(mShibGrantedAuthorityService).getGrantedAuthoritiesForUser("test-user-id", authentication);

        }
    }
    
    @Nested
    class UserDoesNotExist {
        
        @BeforeEach
        void setup() throws CursorException, LdapException {
            when(mRestService.addUser(argUser.capture())).thenReturn(null);
            when(mLdapService.getLDAPAttributes("test-user-id")).thenReturn(new HashMap<>(Map.of("p1","v1","p2","v2")));
        }
        
        @Test
        void testUserDoesNotExist() throws CursorException, LdapException {
            
            Collection<GrantedAuthority> authorities = Collections.emptyList();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("test-user-id", null, authorities);
            authentication.setDetails(mDetails);

            when(mRestService.userExists(argValidateUser.capture())).thenReturn(false);

            Authentication result = provider.authenticate(authentication);

            assertThat(result).isInstanceOfSatisfying(PreAuthenticatedAuthenticationToken.class, preAuthToken -> {
                assertThat(preAuthToken).isNotNull();
                assertThat(preAuthToken.isAuthenticated()).isTrue();
                assertThat(preAuthToken.getPrincipal()).isEqualTo("test-user-id");
                assertThat(preAuthToken.getCredentials()).isEqualTo("N/A");
                assertThat(preAuthToken.getDetails()).isNull();
                assertThatIterable(preAuthToken.getAuthorities()).containsExactly(ShibUtils.ROLE_USER);
            });

            ValidateUser actualValidatedUser = argValidateUser.getValue();
            assertThat(actualValidatedUser.getUserid()).isEqualTo("test-user-id");
            assertThat(actualValidatedUser.getPassword()).isEqualTo("N/A");

            //verify(mShibGrantedAuthorityService).getGrantedAuthoritiesForUser("test-user-id", authentication);
            
            User actualUser = argUser.getValue();
            assertThat(actualUser.getEmail()).isEqualTo("test@test.com");
            assertThat(actualUser.getFirstname()).isEqualTo("first");
            assertThat(actualUser.getLastname()).isEqualTo("last");
            assertThat(actualUser.getPassword()).isEqualTo(null);
            assertThat(actualUser.getProperties()).isEqualTo(new HashMap<>(Map.of("p1","v1","p2","v2")));
            
            verify(mLdapService).getLDAPAttributes("test-user-id");
            verify(mRestService).addUser(actualUser);
            
            verifyNoMoreInteractions(mRestService, mLdapService);
            
        }
    }
    
    @Nested
    class SupportTests  {
        
        @Test
        void testPreAuthenticationToken() {
          assertThat(provider.supports(PreAuthenticatedAuthenticationToken.class)).isTrue();   
        }

        @Test
        void testNonPreAuthenticationToken() {
            assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isFalse();
        }

        @Test
        void testNullAuthentication() {
            assertThat(provider.supports(null)).isFalse();
        }
    }
}