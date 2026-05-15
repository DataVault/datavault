package org.datavaultplatform.webapp.controllers.admin;

import lombok.SneakyThrows;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.test.MvcUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("DefaultAnnotationParam")
abstract class BaseAdminUsersControllerTest {

    @Autowired
    Environment env;

    @MockitoBean
    RestService restService;

    @Autowired
    AdminUsersController adminUsersController;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    final void setup() {
        assertThat(adminUsersController).isNotNull();
        assertThat(env.getActiveProfiles()).containsExactly(getExpectedProfile());
    }
    
    abstract String getExpectedProfile();
    
    @Test
    @Order(2)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user")
    void testListUsers_VanillaUserCannotListUsers() {
        MvcUtils.performWithForward(mockMvc, get("/admin/users").accept(MediaType.TEXT_HTML)).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @Order(3)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"IS_ADMIN", "USER"})
    void testListUsers_SuperUserCanListUsers() {
        MvcUtils.performWithForward(mockMvc, get("/admin/users").accept(MediaType.TEXT_HTML)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(Matchers.containsString("<title>Admin - Users</title>")))
                .andReturn();
    }


    @Test
    @Order(4)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testShowAddUserForm_VanillaUserCannotShowCreateUserForm() {
        MvcUtils.performWithForward(mockMvc, get("/admin/users/create")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    abstract void testShowAddUserForm_SuperUserShowCreateUserForm();

    @Test
    @Order(6)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testSubmitAddUserForm_VanillaUserCannotSubmitCreateUserForm() {
        MvcUtils.performWithForward(mockMvc, post("/admin/users/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("action", "Save")
                        .param("firstname", "Robert")
                        .param("lastname", "Roberts")
                        .param("email", "bob.123@example.com")
                        .param("id", "bob")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    abstract void testSubmitAddUserForm_SuperUserSubmitCreateUserForm();

    @Test
    @Order(8)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testShowEditUsersForm_UserCannotShowEditUserFormForThemselves() {
        MvcUtils.performWithForward(mockMvc, get("/admin/users/edit/bob")).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(9)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"IS_ADMIN", "USER"})
    void testShowEditUsersForm_SuperUserCanShowCreateUserForm() {
        User bobUser = new User();
        bobUser.setID("bob");
        bobUser.setFirstname("Robert");
        bobUser.setLastname("Roberts");
        bobUser.setEmail("bob.123@example.com");
        when(restService.getUser("bob")).thenReturn(bobUser);
        mockMvc.perform(get("/admin/users/edit/bob")).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        verify(restService).getUser("bob");
    }

    @Test
    @Order(10)
    @SneakyThrows
    @WithMockUser(username = "bob", roles = {"USER"})
    void testShowEditUsersForm_UserCanShowEditUserFormForThemselves() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getName()).isEqualTo("bob");
        User bobUser = new User();
        bobUser.setID("bob");
        bobUser.setFirstname("Robert");
        bobUser.setLastname("Roberts");
        bobUser.setEmail("bob.123@example.com");
        when(restService.getUser("bob")).thenReturn(bobUser);
        mockMvc.perform(get("/admin/users/edit/bob")).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        verify(restService).getUser("bob");
    }

    @Test
    @Order(11)
    @SneakyThrows
    @WithMockUser(username = "vanilla-user", roles = {"USER"})
    void testSubmitEditUsersForm_UserCannotSubmitEditUserFormForOthers() {
        MvcUtils.performWithForward(mockMvc, post("/admin/users/edit/bob")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("action", "Save")
                        .param("firstname", "Robert")
                        .param("lastname", "Roberts")
                        .param("email", "bob.123@example.com")
                        .param("id", "bob")
                ).andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(12)
    @SneakyThrows
    @WithMockUser(username = "super-user", roles = {"IS_ADMIN", "USER"})
    void testSubmitEditUsersForm_SuperAdminCanSubmitEditUserFormForOthers() {

        User bobUser = new User();
        bobUser.setID("bob");
        bobUser.setFirstname("Robert");
        bobUser.setLastname("Roberts");
        bobUser.setEmail("bob.123@example.com");

        when(restService.getUser("bob")).thenReturn(bobUser);

        mockMvc.perform(post("/admin/users/edit/bob")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("action", "Save")
                        .param("firstname", "Robert1")
                        .param("lastname", "Roberts1")
                        .param("email", "bob.1234@example.com")
                        .param("id", "bob"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        verify(restService).getUser("bob");
        verify(restService).editUser(bobUser);
        verifyNoMoreInteractions(restService);
    }

    @Test
    @Order(13)
    @SneakyThrows
    @WithMockUser(username = "bob", roles = {"USER"})
    void testSubmitEditUsersForm_UserCanSubmitEditUserFormForThemselves() {

        User bobUser = new User();
        bobUser.setID("bob");
        bobUser.setFirstname("Robert");
        bobUser.setLastname("Roberts");
        bobUser.setEmail("bob.123@example.com");
        when(restService.getUser("bob")).thenReturn(bobUser);

        mockMvc.perform(post("/admin/users/edit/bob")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("action", "Save")
                        .param("firstname", "Robert1")
                        .param("lastname", "Roberts1")
                        .param("email", "bob.1234@example.com")
                        .param("id", "bob"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        verify(restService).getUser("bob");
        verify(restService).editUser(bobUser);
        verifyNoMoreInteractions(restService);
    }
}