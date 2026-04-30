package org.datavaultplatform.webapp.controllers.admin;

import lombok.SneakyThrows;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @MockitoBean
    RestService restService;

    @Autowired
    AdminPendingVaultsController adminPendingVaultsController;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    final void setup() {
        assertThat(adminPendingVaultsController).isNotNull();
        assertThat(env.getActiveProfiles()).containsExactly("database");
    }
    
    @Test
    @Order(1)
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
    @Order(2)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER","IS_ADMIN"})
    void testDeletePendingVaultAllowsForSuperAdmins() {

        // Yes, delete pending vault users GET method
        mockMvc.perform(get("/admin/pendingVaults/pendingVault123")).andDo(print())
                .andExpect(status().isFound())
                .andReturn();
        
        verify(restService).deletePendingVault("pendingVault123");
        verifyNoMoreInteractions(restService);

    }
}