package org.datavaultplatform.broker.controllers;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockRabbitConfig;
import org.datavaultplatform.broker.config.MockServicesConfig;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.PausedState;
import org.datavaultplatform.common.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@EnableAutoConfiguration(exclude= {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class })
@TestPropertySource(properties = {
        "logging.level.org.springframework.security=TRACE",
        "broker.security.enabled=true",
        "broker.scheduled.enabled=false",
        "broker.controllers.enabled=true",
        "broker.services.enabled=false",
        "broker.rabbit.enabled=false",
        "broker.ldap.enabled=false",
        "broker.initialise.enabled=false",
        "broker.email.enabled=false",
        "broker.database.enabled=false"})
@Import({MockServicesConfig.class, MockRabbitConfig.class}) //cos spring security requires some services so we have to mock them
@AutoConfigureMockMvc
class PausedStateControllerIT {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneOffset.UTC);

    @Autowired
    RolesAndPermissionsService mRolesAndPermissionService;
    
    @Autowired
    UsersService mUsersService;
    
    @Autowired
    AdminService mAdminService;
    
    @Autowired
    MockMvc mvc;

    @Autowired
    PausedStateService mPausedStateService;

    @Autowired
    ClientsService mClientsService;

    @BeforeEach
    void setup() {
        assertThat(mockingDetails(mUsersService).isMock()).isTrue();
        assertThat(mockingDetails(mAdminService).isMock()).isTrue();
        assertThat(mockingDetails(mPausedStateService).isMock()).isTrue();
        assertThat(mockingDetails(mRolesAndPermissionService).isMock()).isTrue();
        assertThat(mockingDetails(mClientsService).isMock()).isTrue();
    }

    @Test
    public void testGetState() throws Exception {
        PausedState pausedState = new PausedState();
        pausedState.setPaused(false);
        pausedState.setCreated(LocalDateTime.now(CLOCK));
        when(mPausedStateService.getCurrentState()).thenReturn(pausedState);

        setupUser("bob",false);
        mvc.perform(get("/admin/paused/state")
                        .header(HEADER_CLIENT_KEY,"clientKey123")
                        .header(HEADER_USER_ID, "bob")
                ).
                andExpect(content().json(
                        """
  {
    "isPaused" : false,
    "created" : "2007-12-03T10:15:30"
  }
"""));
        verify(mPausedStateService).getCurrentState();
        verifyNoMoreInteractions(mPausedStateService);
    }

    @ParameterizedTest
    @ValueSource(ints = {0,1,10,12})
    @NullSource
    public void testPausedHistory(final Integer limit) throws Exception {
        PausedState ps1 = new PausedState();
        ps1.setPaused(false);
        ps1.setCreated(LocalDateTime.now(CLOCK));
        PausedState ps2 = new PausedState();
        ps2.setPaused(true);
        ps2.setCreated(LocalDateTime.now(CLOCK).minusDays(1));
        PausedState ps3 = new PausedState();
        ps3.setPaused(false);
        ps3.setCreated(LocalDateTime.now(CLOCK).minusDays(2));
        
        int expectedLimit = limit == null ? 10 : limit;
        when(mPausedStateService.getRecentEntries(expectedLimit)).thenReturn(List.of(ps1,ps2,ps3));

        setupUser("bob",false);

        String requestURL = "/admin/paused/history";
        if(limit != null){
            requestURL += "/" + limit;
        }
        mvc.perform(get(requestURL)
                        .header(HEADER_CLIENT_KEY,"clientKey123")
                        .header(HEADER_USER_ID, "bob")
                )
                .andDo(print())
                .andExpect(content().json(
                        """
[
  { "isPaused":false, "created":"2007-12-03T10:15:30"},
  { "isPaused":true,  "created":"2007-12-02T10:15:30"},
  { "isPaused":false, "created":"2007-12-01T10:15:30"}
]
"""));
        verify(mPausedStateService).getRecentEntries(expectedLimit);
        verifyNoMoreInteractions(mPausedStateService);
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    void setupUser(String username, boolean isAdmin){
        User bobUser = new User();
        bobUser.setID(username+"-user-id");
        when(mUsersService.getUser(username))
                .thenReturn(bobUser);
        when(mRolesAndPermissionService.getUserPermissions(bobUser.getID())).thenReturn(Collections.EMPTY_SET);

        when(mAdminService.isAdminUser(bobUser)).thenReturn(isAdmin);
        Client client = new Client();
        client.setIpAddress("127.0.0.1");

        when(mClientsService.getClientByApiKey("clientKey123")).thenReturn(client);
    }
    
    @Test
    public void testToggleStateAsAdminUser() throws Exception {
        PausedState pausedState = new PausedState();
        when(mPausedStateService.toggleState()).thenReturn(pausedState);
        
        setupUser("bob", true);

        MvcResult result = mvc.perform(post("/admin/paused/toggle")
                .header(HEADER_CLIENT_KEY,"clientKey123")
                .header(HEADER_USER_ID, "bob")
                .with(csrf())).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        verify(mPausedStateService).toggleState();
        verifyNoMoreInteractions(mPausedStateService);
    }
    @Test
    public void testToggleStateAsNonAdminUser() throws Exception {
        PausedState pausedState = new PausedState();
        when(mPausedStateService.toggleState()).thenReturn(pausedState);

        setupUser("bob", false);

        MvcResult result = mvc.perform(post("/admin/paused/toggle")
                .header(HEADER_CLIENT_KEY,"clientKey123")
                .header(HEADER_USER_ID, "bob")
                .with(csrf())).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

        verify(mPausedStateService, never()).toggleState();
        verifyNoMoreInteractions(mPausedStateService);
    }
}