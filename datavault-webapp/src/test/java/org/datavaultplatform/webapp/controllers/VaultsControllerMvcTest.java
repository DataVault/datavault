package org.datavaultplatform.webapp.controllers;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.SneakyThrows;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.common.util.TestUtils;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.datavaultplatform.webapp.test.MvcUtils;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ProfileDatabase
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VaultsControllerMvcTest {

//    private static final String SESSION_ATTR_CSRF_TOKEN = HttpSessionCsrfTokenRepository.class.getName()
//        .concat(".CSRF_TOKEN");
    @Autowired
    MockMvc mockMvc;
    @Captor
    ArgumentCaptor<RoleAssignment> roleAssignmentArg;
    @MockitoBean
    private RestService restService;

    @MockitoBean
    private UserLookupService userLookupService;
    
    @Test
    @Order(1)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"IS_ADMIN", "USER"})
    void testGetVault_FailsIfVaultNotFoundAsSuperUser() {

        when(restService.getVault("vault123")).thenReturn(null);

        mockMvc.perform(get("/vaults/vault123/user123"))
                .andDo(print()).andExpect(status().isNotFound());

        verify(restService).getVault("vault123");
        verifyNoMoreInteractions(restService, userLookupService);

    }
    
    @Test
    @Order(2)
    @SneakyThrows
    @WithMockUser(username = "user123", roles = {"USER"})
    void testGetVault_FailsIfVaultNotFoundAsVanillaUser() {

        when(restService.getVault("vault123")).thenReturn(null);

        mockMvc.perform(get("/vaults/vault123/user123"))
                .andDo(print()).andExpect(status().isNotFound());

        verify(restService).getVault("vault123");
        verifyNoMoreInteractions(restService, userLookupService);
    }

    @Test
    @Order(3)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"IS_ADMIN", "USER"})
    void testGetVault_ForbiddenIfCannotAccessVaultAsSuperUser() {

        VaultInfo vaultInfo = new VaultInfo();
        when(restService.getVault("vault123")).thenReturn(vaultInfo);

        RoleAssignment ra1 = new RoleAssignment();
        when(restService.getRoleAssignmentsForUser("super-user")).thenReturn(List.of(ra1));

        try (MockedStatic<RoleUtils> roleUtils = Mockito.mockStatic(RoleUtils.class)) {
            roleUtils.when(() -> RoleUtils.isISAdmin(roleAssignmentArg.capture())).thenReturn(false);

            mockMvc.perform(get("/vaults/vault123/user123"))
                    .andDo(print()).andExpect(status().isForbidden());
        }

        verify(restService).getVault("vault123");
        verify(restService).getRoleAssignmentsForUser("super-user");
        verifyNoMoreInteractions(restService, userLookupService);
    }

    @Test
    @Order(4)
    @SneakyThrows
    @WithMockUser(username = "user123", roles = {"USER"})
    void testGetVault_ForbiddenIfCannotAccessVaultAsVanillaUser() {

        VaultInfo vaultInfo = new VaultInfo();
        when(restService.getVault("vault123")).thenReturn(vaultInfo);

        RoleAssignment ra1 = new RoleAssignment();
        when(restService.getRoleAssignmentsForUser("user123")).thenReturn(List.of(ra1));

        try (MockedStatic<RoleUtils> roleUtils = Mockito.mockStatic(RoleUtils.class)) {
            roleUtils.when(() -> RoleUtils.isISAdmin(roleAssignmentArg.capture())).thenReturn(false);

            mockMvc.perform(get("/vaults/vault123/user123"))
                    .andDo(print()).andExpect(status().isForbidden());
        }

        verify(restService).getVault("vault123");
        verify(restService).getRoleAssignmentsForUser("user123");
        verifyNoMoreInteractions(restService, userLookupService);

    }

    @Test
    @Order(5)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"IS_ADMIN", "USER"})
    void testGetVault_AllowedIfCanAccessVaultAsSuperUser() {

        VaultInfo vaultInfo = new VaultInfo();
        when(restService.getVault("vault123")).thenReturn(vaultInfo);

        RoleAssignment ra1 = new RoleAssignment();
        when(restService.getRoleAssignmentsForUser("super-user")).thenReturn(List.of(ra1));

        VaultInfo[] vaultListing = new VaultInfo[0];
        when(restService.getVaultsListingAll("user123")).thenReturn(vaultListing);

        try (MockedStatic<RoleUtils> roleUtils = Mockito.mockStatic(RoleUtils.class)) {
            roleUtils.when(() -> RoleUtils.isISAdmin(roleAssignmentArg.capture())).thenReturn(true);

            mockMvc.perform(get("/vaults/vault123/user123"))
                    .andDo(print())
                    .andExpect(model().attribute("vaults", vaultListing))
                    .andExpect(view().name("vaults/userVaults"));
        }

        verify(restService).getVault("vault123");
        verify(restService).getRoleAssignmentsForUser("super-user");
        verify(restService).getVaultsListingAll("user123");
        verifyNoMoreInteractions(restService, userLookupService);

    }

    @Test
    @Order(6)
    @SneakyThrows
    @WithMockUser(username = "user123", roles = {"USER"})
    void testGetVault_AllowedIfCanAccessVaultAsVanillaUser() {

        VaultInfo vaultInfo = new VaultInfo();
        when(restService.getVault("vault123")).thenReturn(vaultInfo);

        RoleAssignment ra1 = new RoleAssignment();
        when(restService.getRoleAssignmentsForUser("user123")).thenReturn(List.of(ra1));

        VaultInfo[] vaultListing = new VaultInfo[0];
        when(restService.getVaultsListingAll("user123")).thenReturn(vaultListing);

        try (MockedStatic<RoleUtils> roleUtils = Mockito.mockStatic(RoleUtils.class)) {
            roleUtils.when(() -> RoleUtils.isISAdmin(roleAssignmentArg.capture())).thenReturn(true);

            mockMvc.perform(get("/vaults/vault123/user123"))
                    .andDo(print())
                    .andExpect(model().attribute("vaults", vaultListing))
                    .andExpect(view().name("vaults/userVaults"));
        }

        verify(restService).getVault("vault123");
        verify(restService).getRoleAssignmentsForUser("user123");
        verify(restService).getVaultsListingAll("user123");
        verifyNoMoreInteractions(restService, userLookupService);

    }

    @Test
    @Order(7)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    @Disabled("This test is disabled as we think how to secure uun search")
    void testIsUUN_ForbiddenAsVanillaUser() {
        MvcUtils.performWithForward(mockMvc, get("/vaults/isuun/v1dhay3")).andExpect(status().isForbidden());
        verifyNoMoreInteractions(restService, userLookupService);

    }

    @Order(8)
    @SneakyThrows
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(username = "super-user", roles = {"USER","IS_ADMIN"})
    void testIsUUN_AllowedAsSuperUser(boolean isUUN) {
        when(userLookupService.isUUN("v1dhay3")).thenReturn(isUUN);
        mockMvc.perform(get("/vaults/isuun/v1dhay3"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(isUUN)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(userLookupService).isUUN("v1dhay3");
        verifyNoMoreInteractions(restService, userLookupService);
    }

    @Test
    @Order(9)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    @Disabled("This test is disabled as we think how to secure uun search")
    void testAutocompleteUUN_ForbiddenAsVanillaUser() {
        MvcUtils.performWithForward(mockMvc, get("/vaults/autocompleteuun/blah")).andExpect(status().isForbidden());
        verifyNoMoreInteractions(restService, userLookupService);
    }


    @Test
    @Order(10)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER","IS_ADMIN"})
    void testAutocompleteUUN_AllowedAsSuperUser() {
        when(userLookupService.getSuggestedUuns("blah")).thenReturn(List.of("blah1","blah2","blah3"));
        mockMvc.perform(get("/vaults/autocompleteuun/blah"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        """
                        ["blah1","blah2","blah3"]"""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(userLookupService).getSuggestedUuns("blah");
        verifyNoMoreInteractions(restService, userLookupService);
    }
    
    @Test
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER","IS_ADMIN"})
    void testStepCreateWithNoCsrfToken() {
        List<ILoggingEvent> logEvents = TestUtils.captureLogging(CsrfFilter.class, () -> {
            MvcResult result = mockMvc.perform(post("/vaults/stepCreate")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);  
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Your session has expired due to inactivity. Please refresh the page and try again.");
        });
        assertThat(logEvents).hasSize(1);
        assertThat(logEvents.get(0).getFormattedMessage()).isEqualTo("Invalid CSRF token found for http://localhost/vaults/stepCreate");
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void testStepCreateWithInvalidCsrfToken() {

        //This will create a session and add a CSRF token to it.
        MvcResult mvcResult = mockMvc.perform(get("/test/hello")).andReturn();
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();

        List<ILoggingEvent> logEvents = TestUtils.captureLogging(CsrfFilter.class, () -> {
            MvcResult result = mockMvc.perform(
                    post("/vaults/stepCreate")
                        .session(session)
                        .with(csrf().useInvalidToken()))
                .andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(403);
            assertThat(result.getResponse().getContentAsString()).isEqualTo(
                "Invalid security token. Please refresh the page and try again.");
        });
        assertThat(logEvents).hasSize(1);
        assertThat(logEvents.get(0).getFormattedMessage()).isEqualTo(
            "Invalid CSRF token found for http://localhost/vaults/stepCreate");
    }

}