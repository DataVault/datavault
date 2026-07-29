package org.datavaultplatform.webapp.services;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.common.util.TestUtils;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLookupServiceTest {

    @Mock
    LDAPService ldapService;

    @Mock
    RestService restService;
    
    @InjectMocks
    UserLookupService userLookupService;
    
    @Nested
    class GetSuggestedUunsTests {
        
        @Test
        @SneakyThrows
        void testGetSuggestedUUN(){
            
            when(ldapService.autocompleteUID("bob")).thenReturn(List.of("aaa","bbb","ccc"));

            List<String> result = userLookupService.getSuggestedUuns("bob");
            assertThat(result).isEqualTo(List.of("aaa","bbb","ccc"));
            
            verify(ldapService).autocompleteUID("bob");
            verifyNoMoreInteractions(ldapService, restService);
        }
        @Test
        @SneakyThrows
        void testGetSuggestedUUNWithLdapException() {
            
            User user1 = new User();
            user1.setID("user1id");
            user1.setFirstname("user1first");
            user1.setLastname("user1last");

            User user2 = new User();
            user2.setID("user2id");
            user2.setFirstname("user2first");
            user2.setLastname("user2last");

            when(ldapService.autocompleteUID("bob")).thenThrow(new LdapException("oops"));
            when(restService.getUsers()).thenReturn(new User[]{user1, user2});
            List<String> result = userLookupService.getSuggestedUuns("bob");
            
            
            assertThat(result).isEqualTo(List.of("user1id - user1first user1last","user2id - user2first user2last"));

            verify(ldapService).autocompleteUID("bob");
            verify(restService).getUsers();
            verifyNoMoreInteractions(ldapService, restService);
        }
        @Test
        @SneakyThrows
        void testGetSuggestedUUNWithCursorException() {

            User user1 = new User();
            user1.setID("user1id");
            user1.setFirstname("user1first");
            user1.setLastname("user1last");

            User user2 = new User();
            user2.setID("user2id");
            user2.setFirstname("user2first");
            user2.setLastname("user2last");

            when(ldapService.autocompleteUID("bob")).thenThrow(new CursorException("oops"));
            when(restService.getUsers()).thenReturn(new User[]{user1, user2});
            List<String> result = userLookupService.getSuggestedUuns("bob");


            assertThat(result).isEqualTo(List.of("user1id - user1first user1last","user2id - user2first user2last"));

            verify(ldapService).autocompleteUID("bob");
            verify(restService).getUsers();
            verifyNoMoreInteractions(ldapService, restService);
        }
    }

    @Nested
    class EnsureUserExistsTests {

        @Test
        @SneakyThrows
        void testUserDoesNotExistWithCursorException() {
            when(restService.getUser("bob")).thenReturn(null);
            when(ldapService.getLdapUserInfo("bob")).thenThrow(new CursorException("oops"));

            InvalidUunException ex = assertThrows(InvalidUunException.class, ()->{
               userLookupService.ensureUserExists("bob"); 
            });
            assertThat(ex).hasMessage("Invalid UUN: bob");
            
            verify(ldapService).getLdapUserInfo("bob");
            verify(restService).getUser("bob");
            verifyNoMoreInteractions(restService, ldapService);
        }

        @Test
        @SneakyThrows
        void testUserDoesNotExistWithLdapException() {
            when(restService.getUser("bob")).thenReturn(null);
            when(ldapService.getLdapUserInfo("bob")).thenThrow(new LdapException("oops"));

            InvalidUunException ex = assertThrows(InvalidUunException.class, ()->{
                userLookupService.ensureUserExists("bob");
            });
            assertThat(ex).hasMessage("Invalid UUN: bob");

            verify(ldapService).getLdapUserInfo("bob");
            verify(restService).getUser("bob");
            verifyNoMoreInteractions(restService, ldapService);
        }

        @Test
        @SneakyThrows
        void testUserDoesNotExistWithEmptyLdapResponse() {
            when(restService.getUser("bob")).thenReturn(null);
            when(ldapService.getLdapUserInfo("bob")).thenReturn(new HashMap<>());

            InvalidUunException ex = assertThrows(InvalidUunException.class, ()->{
                userLookupService.ensureUserExists("bob");
            });
            assertThat(ex).hasMessage("Invalid UUN: bob");

            verify(ldapService).getLdapUserInfo("bob");
            verify(restService).getUser("bob");
            verifyNoMoreInteractions(restService, ldapService);
        }

        @Test
        @SneakyThrows
        void testUserDoesExistSameEmail() {
            User userBob = new User();
            userBob.setEmail("bob@test.com");
            when(restService.getUser("bob")).thenReturn(userBob);
            
            HashMap<String,String> bobMap = new HashMap<>();
            bobMap.put("mail", "bob@test.com");

            when(ldapService.getLdapUserInfo("bob")).thenReturn(bobMap);
            
            User user = userLookupService.ensureUserExists("bob");
            assertThat(user).isEqualTo(userBob);
            
            verify(restService).getUser("bob");
            verify(ldapService).getLdapUserInfo("bob");
            verify(restService, times(0)).editUser(userBob);
            
            verifyNoMoreInteractions(restService, ldapService);
        }

        @Test
        @SneakyThrows
        void testUserDoesExistDifferentEmail() {
            User userBob = new User();
            userBob.setEmail("old@test.com");
            when(restService.getUser("bob")).thenReturn(userBob);

            HashMap<String,String> bobMap = new HashMap<>();
            bobMap.put("mail", "new@test.com");

            when(ldapService.getLdapUserInfo("bob")).thenReturn(bobMap);

            User user = userLookupService.ensureUserExists("bob");
            assertThat(user).isEqualTo(userBob);
            assertThat(user.getEmail()).isEqualTo("new@test.com");

            verify(restService).getUser("bob");
            verify(ldapService).getLdapUserInfo("bob");
            verify(restService, times(1)).editUser(userBob);

            verifyNoMoreInteractions(restService, ldapService);
        }
        
        @Captor
        ArgumentCaptor<User> argUser;
        
        @Test
        @SneakyThrows
        void testUserDoesNotExistUserAddedValidEmail() {
            when(restService.getUser("bob")).thenReturn(null);

            HashMap<String,String> bobMap = new HashMap<>();
            bobMap.put("mail", "new@test.com");
            bobMap.put("cn", "bob roberts");

            when(ldapService.getLdapUserInfo("bob")).thenReturn(bobMap);

            User user = userLookupService.ensureUserExists("bob");
            assertThat(user.getFirstname()).isEqualTo("bob");
            assertThat(user.getLastname()).isEqualTo("roberts");
            assertThat(user.getEmail()).isEqualTo("new@test.com");

            verify(restService).getUser("bob");
            verify(ldapService).getLdapUserInfo("bob");
            verify(restService, times(1)).addUser(argUser.capture());

            assertThat(user).isEqualTo(argUser.getValue());
            verifyNoMoreInteractions(restService, ldapService);
        }
        @Test
        @SneakyThrows
        void testUserDoesNotExistUserAddedInValidEmail() {
            when(restService.getUser("bob")).thenReturn(null);

            HashMap<String,String> bobMap = new HashMap<>();
            bobMap.put("mail", "invalid");
            bobMap.put("cn", "bob roberts");

            when(ldapService.getLdapUserInfo("bob")).thenReturn(bobMap);
            
            when(restService.addUser(any(User.class))).thenAnswer((Answer<User>) invocation -> {
                User arg = invocation.getArgument(0);
                arg.setID("generated-user-id");
                return arg;
            });

            List<ILoggingEvent> logEvents = TestUtils.captureLogging(UserLookupService.class, () -> {
                User user = userLookupService.ensureUserExists("bob");
                assertThat(user.getFirstname()).isEqualTo("bob");
                assertThat(user.getLastname()).isEqualTo("roberts");
                assertThat(user.getEmail()).isEqualTo("invalid");

                verify(restService).getUser("bob");
                verify(ldapService).getLdapUserInfo("bob");
                verify(restService, times(1)).addUser(argUser.capture());

                assertThat(user.getID()).isEqualTo("generated-user-id");
                assertThat(user).isEqualTo(argUser.getValue());
                verifyNoMoreInteractions(restService, ldapService);
            });
            assertThat(logEvents).hasSize(2);
            assertThat(logEvents.get(0).getFormattedMessage()).isEqualTo("Adding user bob - bob roberts");
            assertThat(logEvents.get(1).getFormattedMessage()).isEqualTo("New User Does Not Have Valid Email Address From Ldap UserId[generated-user-id]Email[invalid]");
        }

        @Nested
        class UserExistsButLdapEmailUsed {
            @Test
            @SneakyThrows
            void testUserExistsButLdapReturnsAttributesWithDifferentEmail() {
                User bobUser = new User();
                bobUser.setID("bob");
                bobUser.setFirstname("first");
                bobUser.setLastname("last");
                bobUser.setEmail("bob@original.com");
                when(restService.getUser("bob")).thenReturn(bobUser);
                HashMap<String,String> attributes = new HashMap<>();
                attributes.put("mail","bob@new.com");
                when(ldapService.getLdapUserInfo(   "bob")).thenReturn(attributes);

                User user = userLookupService.ensureUserExists("bob");
                assertThat(user.getFirstname()).isEqualTo("first");
                assertThat(user.getLastname()).isEqualTo("last");
                assertThat(user.getEmail()).isEqualTo("bob@new.com");

                verify(restService).getUser("bob");
                verify(ldapService).getLdapUserInfo("bob");
                verify(restService, never()).addUser(any(User.class));
                verify(restService).editUser(argUser.capture());

                assertThat(user).isEqualTo(bobUser);
                assertThat(argUser.getValue()).isEqualTo(bobUser);
                verifyNoMoreInteractions(restService, ldapService);
            }
        }
        @Nested
        class UserExistsButLdapEmailNotUsed {
            @Test
            @SneakyThrows
            void testUserExistsButLdapThrowsLdapException() {
                User bobUser = new User();
                bobUser.setID("bob");
                bobUser.setFirstname("first");
                bobUser.setLastname("last");
                bobUser.setEmail("bob@original.com");
                when(restService.getUser("bob")).thenReturn(bobUser);

                when(ldapService.getLdapUserInfo("bob")).thenThrow(new LdapException("oops"));

                User user = userLookupService.ensureUserExists("bob");
                assertThat(user.getFirstname()).isEqualTo("first");
                assertThat(user.getLastname()).isEqualTo("last");
                assertThat(user.getEmail()).isEqualTo("bob@original.com");

                verify(restService).getUser("bob");
                verify(ldapService).getLdapUserInfo("bob");
                verify(restService, never()).addUser(any(User.class));

                assertThat(user).isEqualTo(bobUser);
                verifyNoMoreInteractions(restService, ldapService);
            }

            @Test
            @SneakyThrows
            void testUserExistsButLdapThrowsCursorException() {
                User bobUser = new User();
                bobUser.setID("bob");
                bobUser.setFirstname("first");
                bobUser.setLastname("last");
                bobUser.setEmail("bob@original.com");
                when(restService.getUser("bob")).thenReturn(bobUser);

                when(ldapService.getLdapUserInfo("bob")).thenThrow(new CursorException("oops"));

                User user = userLookupService.ensureUserExists("bob");
                assertThat(user.getFirstname()).isEqualTo("first");
                assertThat(user.getLastname()).isEqualTo("last");
                assertThat(user.getEmail()).isEqualTo("bob@original.com");

                verify(restService).getUser("bob");
                verify(ldapService).getLdapUserInfo("bob");
                verify(restService, never()).addUser(any(User.class));

                assertThat(user).isEqualTo(bobUser);
                verifyNoMoreInteractions(restService, ldapService);
            }

            @Test
            @SneakyThrows
            void testUserExistsButLdapReturnsEmptyAttributes() {
                User bobUser = new User();
                bobUser.setID("bob");
                bobUser.setFirstname("first");
                bobUser.setLastname("last");
                bobUser.setEmail("bob@original.com");
                when(restService.getUser("bob")).thenReturn(bobUser);

                when(ldapService.getLdapUserInfo("bob")).thenReturn(new HashMap<>());

                User user = userLookupService.ensureUserExists("bob");
                assertThat(user.getFirstname()).isEqualTo("first");
                assertThat(user.getLastname()).isEqualTo("last");
                assertThat(user.getEmail()).isEqualTo("bob@original.com");

                verify(restService).getUser("bob");
                verify(ldapService).getLdapUserInfo("bob");
                verify(restService, never()).addUser(any(User.class));

                assertThat(user).isEqualTo(bobUser);
                verifyNoMoreInteractions(restService, ldapService);
            }
            @Test
            @SneakyThrows
            void testUserExistsButLdapReturnsAttributesWithInvalidEmail() {
                User bobUser = new User();
                bobUser.setID("bob");
                bobUser.setFirstname("first");
                bobUser.setLastname("last");
                bobUser.setEmail("bob@original.com");
                when(restService.getUser("bob")).thenReturn(bobUser);
                HashMap<String,String> attributes = new HashMap<>();
                attributes.put("mail","invalid");
                when(ldapService.getLdapUserInfo(   "bob")).thenReturn(attributes);

                User user = userLookupService.ensureUserExists("bob");
                assertThat(user.getFirstname()).isEqualTo("first");
                assertThat(user.getLastname()).isEqualTo("last");
                assertThat(user.getEmail()).isEqualTo("bob@original.com");

                verify(restService).getUser("bob");
                verify(ldapService).getLdapUserInfo("bob");
                verify(restService, never()).addUser(any(User.class));

                assertThat(user).isEqualTo(bobUser);
                verifyNoMoreInteractions(restService, ldapService);
            }
            @Test
            @SneakyThrows
            void testUserExistsButLdapReturnsAttributesWithSameEmail() {
                User bobUser = new User();
                bobUser.setID("bob");
                bobUser.setFirstname("first");
                bobUser.setLastname("last");
                bobUser.setEmail("bob@original.com");
                when(restService.getUser("bob")).thenReturn(bobUser);
                HashMap<String,String> attributes = new HashMap<>();
                attributes.put("mail","bob@original.com");
                when(ldapService.getLdapUserInfo(   "bob")).thenReturn(attributes);

                User user = userLookupService.ensureUserExists("bob");
                assertThat(user.getFirstname()).isEqualTo("first");
                assertThat(user.getLastname()).isEqualTo("last");
                assertThat(user.getEmail()).isEqualTo("bob@original.com");

                verify(restService).getUser("bob");
                verify(ldapService).getLdapUserInfo("bob");
                verify(restService, never()).addUser(any(User.class));

                assertThat(user).isEqualTo(bobUser);
                verifyNoMoreInteractions(restService, ldapService);
            }
        }
    }
    
    @Nested
    class isUUNTests {
        
        @Test
        @SneakyThrows
        void isUunTrue() {
            HashMap<String,String> ldapMap = new HashMap<>();
            ldapMap.put("mail", "bob@test.com");
            when(ldapService.getLdapUserInfo("bob")).thenReturn(ldapMap);
            
            assertThat(userLookupService.isUUN("bob")).isTrue();
            verify(ldapService).getLdapUserInfo("bob");
            verifyNoMoreInteractions(ldapService, restService);
        }
        
        @Test
        @SneakyThrows
        void isUunFalseBecauseNoAttributes() {
            HashMap<String,String> ldapMap = new HashMap<>();
            when(ldapService.getLdapUserInfo("bob")).thenReturn(ldapMap);

            assertThat(userLookupService.isUUN("bob")).isFalse();
            verify(ldapService).getLdapUserInfo("bob");
            verifyNoMoreInteractions(ldapService, restService);
        }
        @Test
        @SneakyThrows
        void isUunFalseBecauseException() {
            when(ldapService.getLdapUserInfo("bob")).thenThrow(new RuntimeException("oops"));

            assertThat(userLookupService.isUUN("bob")).isFalse();
            verify(ldapService).getLdapUserInfo("bob");
            verifyNoMoreInteractions(ldapService, restService);
        }
    }
    
    @Nested
    class CheckNewRolesUserExistsTests {
        
        UserLookupService spyUserLookupService;

        @BeforeEach
        void setup(){
             spyUserLookupService = Mockito.spy(userLookupService);
        }
        
        @Test
        void testCheckNewRolesUserExistsDepositors() {
            
            String depResult = "http://dep-result.com";
            doReturn(depResult).when(spyUserLookupService).checkUserList(any(List.class), any(String.class));
            
            List<String> depositors = List.of("depositor1","depositor2");
            CreateVault createVault = new CreateVault();
            createVault.setDepositors(depositors);
            String result = spyUserLookupService.checkNewRolesUserExists(createVault, "buildURL");
            
            assertThat(result).isEqualTo("redirect:http://dep-result.com");
            
        }

        @Test
        void testCheckNewRolesUserExistsNominatedDataManagers() {

            String depResult = "";
            String ndmResult = "http://ndm-result.com";
            doReturn(depResult, ndmResult).when(spyUserLookupService).checkUserList(any(List.class), any(String.class));

            List<String> depositors = List.of("depositor1","depositor2");
            List<String> ndms = List.of("ndm1","ndm2");
            CreateVault createVault = new CreateVault();
            createVault.setDepositors(depositors);
            createVault.setNominatedDataManagers(ndms);
            String result = spyUserLookupService.checkNewRolesUserExists(createVault, "buildURL");

            assertThat(result).isEqualTo("redirect:http://ndm-result.com");

        }
        @Test
        void testCheckNewRolesUserExistsCreators() {

            String depResult = "";
            String ndmResult = "";
            String creatorsResult = "http://creator-result.com";
            doReturn(depResult, ndmResult, creatorsResult).when(spyUserLookupService).checkUserList(any(List.class), any(String.class));

            List<String> depositors = List.of("depositor1","depositor2");
            List<String> ndms = List.of("ndm1","ndm2");
            List<String> creators = List.of("creator1","creator2");

            CreateVault createVault = new CreateVault();
            createVault.setDepositors(depositors);
            createVault.setNominatedDataManagers(ndms);
            createVault.setDataCreators(creators);
            
            String result = spyUserLookupService.checkNewRolesUserExists(createVault, "buildURL");

            assertThat(result).isEqualTo("redirect:http://creator-result.com");

        }
        @Test
        void testCheckNewRolesUserExistsVaultOwner() {

            String depResult = "";
            String ndmResult = "";
            String creatorsResult = "";
            doReturn(depResult, ndmResult, creatorsResult).when(spyUserLookupService).checkUserList(any(List.class), any(String.class));

            String ownerResult = "http://owner-result.com";
            doReturn(ownerResult).when(spyUserLookupService).checkUser(any(String.class), any(String.class));
            
            List<String> depositors = List.of("depositor1","depositor2");
            List<String> ndms = List.of("ndm1","ndm2");
            List<String> creators = List.of("creator1","creator2");
            

            CreateVault createVault = new CreateVault();
            createVault.setDepositors(depositors);
            createVault.setNominatedDataManagers(ndms);
            createVault.setDataCreators(creators);
            createVault.setVaultOwner("vaultOwner");
            String result = spyUserLookupService.checkNewRolesUserExists(createVault, "buildURL");

            assertThat(result).isEqualTo("redirect:http://owner-result.com");

        }
        @Test
        void testCheckNewRolesUserExistsContactPerson() {

            String depResult = "";
            String ndmResult = "";
            String creatorsResult = "";
            doReturn(depResult, ndmResult, creatorsResult).when(spyUserLookupService).checkUserList(any(List.class), any(String.class));

            String ownerResult = "";
            String contactResult = "http://contact-result.com"; 
            doReturn(ownerResult, contactResult).when(spyUserLookupService).checkUser(any(String.class), any(String.class));

            List<String> depositors = List.of("depositor1","depositor2");
            List<String> ndms = List.of("ndm1","ndm2");
            List<String> creators = List.of("creator1","creator2");


            CreateVault createVault = new CreateVault();
            createVault.setDepositors(depositors);
            createVault.setNominatedDataManagers(ndms);
            createVault.setDataCreators(creators);
            createVault.setVaultOwner("vaultOwner");
            createVault.setContactPerson("contactPerson");
            
            String result = spyUserLookupService.checkNewRolesUserExists(createVault, "buildURL");

            assertThat(result).isEqualTo("redirect:http://contact-result.com");

        }
        @Test
        void testCheckNewRolesUserExistsDefaultReturn() {

            String depResult = "";
            String ndmResult = "";
            String creatorsResult = "";
            doReturn(depResult, ndmResult, creatorsResult).when(spyUserLookupService).checkUserList(any(List.class), any(String.class));

            String ownerResult = "";
            String contactResult = "";
            doReturn(ownerResult, contactResult).when(spyUserLookupService).checkUser(any(String.class), any(String.class));

            List<String> depositors = List.of("depositor1","depositor2");
            List<String> ndms = List.of("ndm1","ndm2");
            List<String> creators = List.of("creator1","creator2");


            CreateVault createVault = new CreateVault();
            createVault.setDepositors(depositors);
            createVault.setNominatedDataManagers(ndms);
            createVault.setDataCreators(creators);
            createVault.setVaultOwner("vaultOwner");
            createVault.setContactPerson("contactPerson");

            String result = spyUserLookupService.checkNewRolesUserExists(createVault, "buildURL");

            assertThat(result).isEqualTo("");

        }
    }

    @Nested
    class UserCheckTests {

        UserLookupService spyUserLookupService;

        @BeforeEach
        void setup(){
            spyUserLookupService = Mockito.spy(userLookupService);
        }

        @Test
        void testNullUser() {
            String result = spyUserLookupService.checkUser(null, "errorURL");
            assertThat(result).isEmpty();
            verifyNoMoreInteractions(ldapService, restService);
        }
        @Test
        void testEmptyUser() {
            String result = spyUserLookupService.checkUser("", "errorURL");
            assertThat(result).isEmpty();
            verifyNoMoreInteractions(ldapService, restService);
        }
        @Test
        @SneakyThrows
        void testUserExists() {
            doReturn(new User()).when(spyUserLookupService).ensureUserExists("bob");
            
            String result = spyUserLookupService.checkUser("bob", "errorURL");
            assertThat(result).isEqualTo("");
        }
        
        @Test
        @SneakyThrows
        void testUserLookupFailsInvalidUunException() {
            doThrow(new InvalidUunException("oops")).when(spyUserLookupService).ensureUserExists("bob");
            String result = spyUserLookupService.checkUser("bob", "errorURL");
            assertThat(result).isEqualTo("errorURL");
        }
    }

    @Nested
    class CheckUserListTests {

        UserLookupService spyUserLookupService;

        @BeforeEach
        void setup() {
            spyUserLookupService = Mockito.spy(userLookupService);
        }

        @Test
        void testWithNullList() {
            String result = spyUserLookupService.checkUserList(null, "errorUrl");
            assertThat(result).isEmpty();
        }
        @Test
        void testEmptyList() {
            String result = spyUserLookupService.checkUserList(List.of(), "errorUrl");
            assertThat(result).isEmpty();
        }
        @Test
        void testNonEmptyListReturnsNullThenEmptyThenNonEmpty() {
            doReturn(
                    null, "", "result-for-item3"
            ).when(spyUserLookupService).checkUser(any(String.class), any(String.class));

            String result = spyUserLookupService.checkUserList(List.of("item1","item2","item3"), "errorUrl");
            assertThat(result).isEqualTo("result-for-item3");
        }
    }
}
