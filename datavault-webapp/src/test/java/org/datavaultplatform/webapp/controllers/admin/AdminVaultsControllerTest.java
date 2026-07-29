package org.datavaultplatform.webapp.controllers.admin;

import lombok.SneakyThrows;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.datavaultplatform.webapp.test.MvcUtils;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.experimental.runners.Enclosed;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(Enclosed.class)
@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ProfileDatabase
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminVaultsControllerTest {

    @Autowired
    Environment env;

    @Autowired
    AdminVaultsController adminVaultsController;

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
        assertThat(adminVaultsController).isNotNull();
        assertThat(env.getActiveProfiles()).containsExactly("database");
    }

    @Nested
    class ShowVaultForVaultIdTests {
        @Test
        @SneakyThrows
        @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN", "ADMIN_VAULTS"})
        void testShowVaultForVaultIdForModelAndViewAndTemplate() {

            VaultInfo vaultInfo = new VaultInfo();
            vaultInfo.setPolicyID("2112");
            vaultInfo.setGroupID("group-id");
            when(restService.getVault("vault1234")).thenReturn(vaultInfo);

            Group group = new Group();
            group.setID("group-id");

            DepositInfo[] deposits = new DepositInfo[0];

            CreateRetentionPolicy retentionPolicy = new CreateRetentionPolicy();
            retentionPolicy.setId(2112);

            when(restService.getRetentionPolicy("2112")).thenReturn(retentionPolicy);
            when(restService.getGroup("group-id")).thenReturn(group);
            when(restService.getDepositsListing("vault1234")).thenReturn(deposits);

            MvcResult result = mockMvc.perform(
                    get("/admin/vaults/vault1234")
            ).andReturn();

            ModelAndView mav = result.getModelAndView();
            String view = mav.getViewName();

            assertThat(view).isEqualTo("admin/vaults/vault");
            Map<String, Object> model = mav.getModel();

            assertThat(model.get("vault")).isEqualTo(vaultInfo);
            assertThat(model.get("retentionPolicy")).isEqualTo(retentionPolicy);
            assertThat(model.get("group")).isEqualTo(group);
            assertThat(model.get("deposits")).isEqualTo(deposits);
        }
        @Test
        @SneakyThrows
        @WithMockUser(username = "super-user", roles = {"USER", "IS_ADMIN", "ADMIN_VAULTS"})
        void testShowVaultForVaultIdForResultHtml() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);

            VaultInfo vaultInfo = new VaultInfo();
            vaultInfo.setPolicyID("2112");
            vaultInfo.setGroupID("group-id");
            when(restService.getVault("vault1234")).thenReturn(vaultInfo);

            LocalDateTime now = LocalDateTime.now();
            Group group = new Group();
            group.setID("group-id");
            group.setName("TST-GROUP-NAME");

            DepositInfo depositInfo1 = new DepositInfo();
            depositInfo1.setID("deposit-info1-id");
            depositInfo1.setName("deposit-info1-name");
            depositInfo1.setStatus(Deposit.Status.COMPLETE);
            depositInfo1.setFileOrigin("deposit-info1-file-origin");
            depositInfo1.setShortFilePath("deposit-info1-short-file-path");
            depositInfo1.setDepositSize(1_222_333);
            depositInfo1.setCreationTime(now);


            DepositInfo depositInfo2 = new DepositInfo();
            depositInfo2.setID("deposit-info2-id");
            depositInfo2.setName("deposit-info2-name");
            depositInfo2.setStatus(Deposit.Status.IN_PROGRESS);
            depositInfo2.setFileOrigin("deposit-info2-file-name");
            depositInfo2.setShortFilePath("deposit-info2-short-file-path");
            depositInfo2.setDepositSize(2_333_444);
            depositInfo2.setCreationTime(now.plusHours(1));
            
            DepositInfo[] deposits = new DepositInfo[]{depositInfo1, depositInfo2};

            CreateRetentionPolicy retentionPolicy = new CreateRetentionPolicy();
            retentionPolicy.setName("TST-RETENTION-POLICY-NAME");
            retentionPolicy.setId(2112);

            when(restService.getRetentionPolicy("2112")).thenReturn(retentionPolicy);
            when(restService.getGroup("group-id")).thenReturn(group);
            when(restService.getDepositsListing("vault1234")).thenReturn(deposits);

            MvcResult result = MvcUtils.performWithForward(mockMvc,
                    get("/admin/vaults/vault1234")
            ).andReturn();

            String html = result.getResponse().getContentAsString();
            
            assertThat(html).contains("TST-RETENTION-POLICY-NAME");
            assertThat(html).contains("TST-GROUP-NAME");
            
            assertThat(html).contains(depositInfo1.getID());
            assertThat(html).contains(depositInfo1.getName());
            assertThat(html).contains("Complete");
            assertThat(html).contains(depositInfo1.getFileOrigin());
            assertThat(html).contains(depositInfo1.getShortFilePath());
            assertThat(html).contains(depositInfo1.getSizeStr());
            assertThat(html).contains(formatter.format(depositInfo1.getCreationTime()));

            assertThat(html).contains(depositInfo2.getID());
            assertThat(html).contains(depositInfo2.getName());
            assertThat(html).contains("In progress");
            assertThat(html).contains(depositInfo2.getFileOrigin());
            assertThat(html).contains(depositInfo2.getShortFilePath());
            assertThat(html).contains(depositInfo2.getSizeStr());
            assertThat(html).contains(formatter.format(depositInfo2.getCreationTime()));
        }
    }
}