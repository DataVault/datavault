package org.datavaultplatform.webapp.controllers.admin;

import lombok.SneakyThrows;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.webapp.controllers.admin.AdminPendingVaultsController.MAX_RECORDS_PER_PAGE;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("DefaultAnnotationParam")
@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ProfileDatabase
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminPendingVaultsControllerTest {

    @Autowired
    Environment env;

    @Autowired
    AdminPendingVaultsController adminPendingVaultsController;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RestService restService;

    @MockitoBean
    UserLookupService userLookupService;

    @Captor
    ArgumentCaptor<CreateVault> argCreateVault;

    @BeforeEach
    final void setup() {
        assertThat(adminPendingVaultsController).isNotNull();
        assertThat(env.getActiveProfiles()).containsExactly("database");
    }


    @Test
    @Order(1)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testSearchdPendingVaults_ForbiddenForNonAdmins() {

        mockMvc.perform(get("/admin/pendingVaults")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(2)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void testSearchPendingVaults_AllowedForSuperAdmins() {

        VaultsData savedVaultsData = new VaultsData();
        savedVaultsData.setData(List.of());
        VaultsData confirmedVaultsData = new VaultsData();
        confirmedVaultsData.setData(List.of());

        when(restService.searchPendingVaults(anyString(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean())).thenReturn(savedVaultsData, confirmedVaultsData);
        mockMvc.perform(get("/admin/pendingVaults")).andDo(print())
                .andExpect(view().name("admin/pendingVaults/index"))
                .andReturn();

        verify(restService, times(2)).searchPendingVaults(anyString(), anyString(), anyString(), anyInt(), eq(MAX_RECORDS_PER_PAGE), anyBoolean());
        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(3)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testGetPendingVaultForm_ForbiddenForNonAdmins() {

        mockMvc.perform(get("/admin/pendingVaults/edit/pendingVaultId123")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(4)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void testGetPendingVaultForm_AllowedForSuperAdmins() {

        VaultInfo mVaultInfo = mock(VaultInfo.class);
        CreateVault mCreateVault = mock(CreateVault.class);

        when(mVaultInfo.convertToCreate()).thenReturn(mCreateVault);

        when(restService.getPendingVault("pendingVaultId123")).thenReturn(mVaultInfo);
        mockMvc.perform(get("/admin/pendingVaults/edit/pendingVaultId123")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("admin/pendingVaults/edit/editPendingVault"))
                .andReturn();

        verify(restService).getPendingVault("pendingVaultId123");
        verify(restService).getRetentionPolicyListing();
        verify(restService).getGroups();
        verify(mVaultInfo).convertToCreate();
        verifyNoMoreInteractions(restService, mVaultInfo);
    }

    @Test
    @Order(5)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testSearchSavedPendingVaults_ForbiddenForNonAdmins() {

        mockMvc.perform(get("/admin/pendingVaults/saved")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(6)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void testSearchSavedPendingVaults_AllowedForSuperAdmins() {
        VaultsData filteredVaultsData = new VaultsData();
        filteredVaultsData.setData(List.of());

        when(restService.searchPendingVaults(anyString(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean())).thenReturn(filteredVaultsData);
        mockMvc.perform(get("/admin/pendingVaults/saved")).andDo(print())
                .andExpect(view().name("admin/pendingVaults/saved"))
                .andReturn();

        verify(restService, times(1)).searchPendingVaults(anyString(), anyString(), anyString(), anyInt(), eq(MAX_RECORDS_PER_PAGE), eq(false));
        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(7)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testSearchConfirmedPendingVaults_ForbiddenForNonAdmins() {

        mockMvc.perform(get("/admin/pendingVaults/confirmed")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(8)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void testSearchConfirmedPendingVaults_AllowedForSuperAdmins() {

        VaultsData confirmedVaultsData = new VaultsData();
        confirmedVaultsData.setData(List.of());

        when(restService.searchPendingVaults(anyString(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean())).thenReturn(confirmedVaultsData);
        mockMvc.perform(get("/admin/pendingVaults/confirmed")).andDo(print())
                .andExpect(view().name("admin/pendingVaults/confirmed"))
                .andReturn();

        verify(restService, times(1)).searchPendingVaults(anyString(), anyString(), anyString(), anyInt(), eq(MAX_RECORDS_PER_PAGE), eq(true));
        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(9)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void getGetVaultSummary_ForbiddenForNonAdmins() {

        mockMvc.perform(get("/admin/pendingVaults/summary/pendingVault123")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(10)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void getGetVaultSummary_AllowedForSuperAdmins() {

        VaultInfo vaultInfo = new VaultInfo();
        vaultInfo.setID("pendingVault123");
        vaultInfo.setGroupID("group-id-123");

        when(restService.getPendingVault("pendingVault123")).thenReturn(vaultInfo);

        Group group = new Group();
        group.setID("group-id-123");

        when(restService.getGroup("group-id-123")).thenReturn(group);

        mockMvc.perform(get("/admin/pendingVaults/summary/pendingVault123")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("admin/pendingVaults/summary"))
                .andReturn();

        verify(restService).getPendingVault("pendingVault123");
        verify(restService).getGroup("group-id-123");
        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(11)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testUpgradeVault_ForbiddenForNonAdmins() {

        mockMvc.perform(get("/admin/pendingVaults/upgrade/pendingVault123")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(12)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void testUpgradeVault_AllowedForSuperAdmins() {

        VaultInfo mVaultInfo1 = mock(VaultInfo.class);
        VaultInfo mVaultInfo2 = mock(VaultInfo.class);
        CreateVault mCreateVault = mock(CreateVault.class);
        when(mVaultInfo1.convertToCreate()).thenReturn(mCreateVault);
        when(mVaultInfo2.getID()).thenReturn("new-vault-id");
        when(mCreateVault.getPendingID()).thenReturn("pendingVault234");

        when(restService.getPendingVault("pendingVault123")).thenReturn(mVaultInfo1);
        when(restService.addVault(any(CreateVault.class))).thenReturn(mVaultInfo2);

        mockMvc.perform(get("/admin/pendingVaults/upgrade/pendingVault123?reviewDate=31-05-2035")).andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/vaults/new-vault-id/"));

        verify(restService).getPendingVault("pendingVault123");
        verify(restService).addVault(mCreateVault);
        verify(restService).deletePendingVault("pendingVault234");

        verifyNoMoreInteractions(restService, userLookupService);
    }

    @Test
    @Order(13)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testSubmitEditPendingVault_ForbiddenForNonAdmins() {
        mockMvc.perform(post("/admin/pendingVaults/edit").with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);

    }

    @Test
    @Order(14)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void testSubmitEditPendingVault_AllowedForSuperAdmins() {

        VaultInfo vaultInfo = new VaultInfo();
        vaultInfo.setID("vault-info-id");

        when(restService.editPendingVault(any(CreateVault.class))).thenReturn(vaultInfo);

        mockMvc.perform(post("/admin/pendingVaults/edit").with(csrf())
                        .param("action", "the-action")
                        .param("pendingID", "pendingID123"))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/pendingVaults/edit/vault-info-id"))
                .andReturn();

        verify(userLookupService).checkNewRolesUserExists(argCreateVault.capture(), eq("/admin/pendingVaults/"));

        CreateVault actual = argCreateVault.getValue();
        assertThat(actual.getPendingID()).isEqualTo("pendingID123");

        verify(restService).editPendingVault(actual);

        verifyNoMoreInteractions(restService, userLookupService);
    }


    @Test
    @Order(15)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testDeletePendingVaultForbiddenForNonAdmins() {

        // Yes, delete pending vault users GET method
        mockMvc.perform(get("/admin/pendingVaults/pendingVault123")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(16)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN"})
    void testDeletePendingVaultAllowsForSuperAdmins() {

        // Yes, delete pending vault users GET method
        mockMvc.perform(get("/admin/pendingVaults/pendingVault123")).andDo(print())
                .andExpect(status().isFound())
                .andReturn();

        verify(restService).deletePendingVault("pendingVault123");
        verifyNoMoreInteractions(restService);

    }
}