package org.datavaultplatform.webapp.controllers.admin;

import lombok.SneakyThrows;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.datavaultplatform.webapp.test.ProfileShib;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ProfileShib
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
@AutoConfigureMockMvc
class AdminUsersControllerShibProfileTest extends baseAdminUsersControllerTest {

    @Override
    String getExpectedProfile() {
        return "shib";
    }
    
    @Test
    @Order(5)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"IS_ADMIN", "USER"})
    void testShowAddUserForm_SuperUserShowCreateUserForm() {
        mockMvc.perform(get("/admin/users/create")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @Order(7)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"IS_ADMIN", "USER"})
    void testSubmitAddUserForm_SuperUserSubmitCreateUserForm() {
        mockMvc.perform(post("/admin/users/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("action", "Save")
                        .param("firstname", "Robert")
                        .param("lastname", "Roberts")
                        .param("email", "bob.123@example.com")
                        .param("id", "bob"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }


}