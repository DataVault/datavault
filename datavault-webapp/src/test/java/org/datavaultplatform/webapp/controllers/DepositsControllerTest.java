package org.datavaultplatform.webapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.datavaultplatform.common.dto.PausedDepositStateDTO;
import org.datavaultplatform.common.dto.PausedRetrieveStateDTO;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@SpringBootTest(classes = DataVaultWebApp.class)
@AutoConfigureMockMvc
@ProfileDatabase
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
@SuppressWarnings("DefaultAnnotationParam")
class DepositsControllerTest {

    final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RestService mRestService;

    @MockBean
    PermissionEvaluator mEvaluator;
    
    @Nested
    class DeniedBecauseOfPermissions {
        @BeforeEach
        void setup() {
            Mockito.lenient().when(mEvaluator.hasPermission(
                    any(Authentication.class),
                    any(Serializable.class),
                    any(String.class),
                    any(Object.class))).thenReturn(false, false);
        }

        @SneakyThrows
        @WithMockUser(username = "user1", roles = {"USER"})
        @Test
        void testAddDepositDeniedBecauseOfPermissions() {
            MvcResult result = performDeposit();
            checkDenied(result);

            checkPermissionsForDeposit("user1");
            Mockito.verifyNoMoreInteractions(mEvaluator, mRestService);
        }

        @SneakyThrows
        @WithMockUser(username = "user2", roles = {"USER"})
        @Test
        void testProcessRetrieveDeniedBecauseOfPermissions() {
            MvcResult result = performRetrieve();
            checkDenied(result);

            checkPermissionsForRetrieve("user2");

            Mockito.verifyNoMoreInteractions(mEvaluator, mRestService);
        }
    }

    @Nested
    class DeniedBecausePausedAndNotISAdmin {
        final PausedRetrieveStateDTO PAUSED_RETRIEVE = new PausedRetrieveStateDTO(true, null);
        final PausedDepositStateDTO PAUSED_DEPOSIT = new PausedDepositStateDTO(true, null);
        @BeforeEach
        void setup() {
            Mockito.lenient().when(mEvaluator.hasPermission(
                    any(Authentication.class),
                    any(Serializable.class),
                    any(String.class),
                    any(Object.class))).thenReturn(false, true);
            Mockito.lenient().when(mRestService.getCurrentRetrievePausedState()).thenReturn(PAUSED_RETRIEVE);
            Mockito.lenient().when(mRestService.getCurrentDepositPausedState()).thenReturn(PAUSED_DEPOSIT);
        }

        @SneakyThrows
        @WithMockUser(username = "user11", roles = {"USER"})
        @Test
        void testAddDepositDeniedBecauseDepositsPaused() {
            MvcResult result = performDeposit();

            checkDenied(result);

            checkPermissionsForDeposit("user11");

            Mockito.verify(mRestService).getCurrentDepositPausedState();
            Mockito.verifyNoMoreInteractions(mEvaluator, mRestService);
        }

        @SneakyThrows
        @WithMockUser(username = "user3", roles = {"USER"})
        @Test
        void testProcessRetrieveDeniedBecauseRetrievesPaused() {
            MvcResult result = performRetrieve();

            checkDenied(result);

            checkPermissionsForRetrieve("user3");

            Mockito.verify(mRestService).getCurrentRetrievePausedState();
            Mockito.verifyNoMoreInteractions(mEvaluator, mRestService);
        }
    }

    @Nested
    class IsAdminCanBypassPaused {
        @BeforeEach
        void setup() {
            Mockito.lenient().when(mEvaluator.hasPermission(
                    any(Authentication.class),
                    any(Serializable.class),
                    any(String.class),
                    any(Object.class))).thenReturn(false, true);
        }

        @SneakyThrows
        @WithMockUser(username = "user12", roles = {"USER", "IS_ADMIN"})
        @Test
        void testAddDepositAllowedDespiteDepositsPaused() {

            DepositInfo depositInfo = new DepositInfo();
            depositInfo.setID("8888");
            when(mRestService.addDeposit(any(CreateDeposit.class))).thenReturn(depositInfo);
            MvcResult result = performDeposit();

            assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.FOUND.value());
            assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/vaults/2112/deposits/8888/");

            checkPermissionsForDeposit("user12");
            verify(mRestService).addDeposit(any(CreateDeposit.class));
            Mockito.verifyNoMoreInteractions(mEvaluator, mRestService);
        }

        @SneakyThrows
        @WithMockUser(username = "user31", roles = {"USER", "IS_ADMIN"})
        @Test
        void testProcessRetrieveAllowedDespiteRetrievesPaused() {
            MvcResult result = performRetrieve();

            when(mRestService.retrieveDeposit(eq("1234"), any(Retrieve.class))).thenReturn(Boolean.TRUE);
            
            assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.FOUND.value());
            assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/vaults/2112/deposits/1234/");

            Mockito.verify(mRestService).retrieveDeposit(eq("1234"),any(Retrieve.class));
            
            checkPermissionsForRetrieve("user31");
            Mockito.verifyNoMoreInteractions(mEvaluator, mRestService);
        }
    }

    void verifyInOrder(InOrder inOrder,
                       ArgumentMatcher<Authentication> matchesAuthentication,
                       ArgumentMatcher<Serializable> matchesTargetId,
                       ArgumentMatcher<String> matchesTargetType,
                       ArgumentMatcher<Object> matchesPermission) {
        inOrder.verify(mEvaluator).hasPermission(
                argThat(matchesAuthentication),
                argThat(matchesTargetId),
                argThat(matchesTargetType),
                argThat(matchesPermission));
    }


    private void checkDenied(MvcResult result) {
        assertThat(result.getResponse().getForwardedUrl()).isEqualTo("/auth/denied");
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @SneakyThrows
    private MvcResult performRetrieve() {
        Retrieve retrieve = new Retrieve();
        retrieve.setNote("test retrieve");

        return mockMvc.perform(
                        post("/vaults/2112/deposits/1234/retrieve")
                                .content(mapper.writeValueAsString(retrieve))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andDo(print()).andReturn();
    }

    @SneakyThrows
    private MvcResult performDeposit() {
        CreateDeposit createDeposit = new CreateDeposit();
        createDeposit.setName("DEPOSIT 1");
        createDeposit.setVaultID("2112");

        return mockMvc.perform(
                        post("/vaults/2112/deposits/create")
                                .content(mapper.writeValueAsString(createDeposit))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andDo(print())
                .andReturn();
    }

    private void checkPermissionsForDeposit(String user) {
        InOrder orderVerifier = Mockito.inOrder(mEvaluator);

        verifyInOrder(orderVerifier, auth -> user.equals(auth.getName()),
                targetId -> targetId.equals("2112"),
                targetType -> targetType.equals("vault"),
                permission -> permission.equals("VIEW_DEPOSITS_AND_RETRIEVES"));

        verifyInOrder(orderVerifier, auth -> user.equals(auth.getName()),
                targetId -> targetId.equals("2112"),
                targetType -> targetType.equals("GROUP_VAULT"),
                permission -> permission.equals("MANAGE_SCHOOL_VAULT_DEPOSITS"));
    }

    private void checkPermissionsForRetrieve(String user) {
        InOrder orderVerifier = Mockito.inOrder(mEvaluator);

        verifyInOrder(orderVerifier, auth -> user.equals(auth.getName()),
                targetId -> targetId.equals("2112"),
                targetType -> targetType.equals("vault"),
                permission -> permission.equals("VIEW_DEPOSITS_AND_RETRIEVES"));

        verifyInOrder(orderVerifier, auth -> user.equals(auth.getName()),
                targetId -> targetId.equals("2112"),
                targetType -> targetType.equals("GROUP_VAULT"),
                permission -> permission.equals("CAN_RETRIEVE_DATA"));
    }
}