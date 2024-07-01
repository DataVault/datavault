package org.datavaultplatform.broker.controllers;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockRabbitConfig;
import org.datavaultplatform.broker.config.MockServicesConfig;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.PausedRetrieveState;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


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
@Import({MockServicesConfig.class, MockRabbitConfig.class}) //because spring security requires some services, so we have to mock them
@AutoConfigureMockMvc
class PausedRetrieveStateControllerIT {

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
    PausedRetrieveStateService mPausedRetrieveStateService;

    @Autowired
    ClientsService mClientsService;

    @BeforeEach
    void setup() {
        assertThat(mockingDetails(mUsersService).isMock()).isTrue();
        assertThat(mockingDetails(mAdminService).isMock()).isTrue();
        assertThat(mockingDetails(mPausedRetrieveStateService).isMock()).isTrue();
        assertThat(mockingDetails(mRolesAndPermissionService).isMock()).isTrue();
        assertThat(mockingDetails(mClientsService).isMock()).isTrue();
    }

    @Test
    public void testGetState() throws Exception {
        PausedRetrieveState PausedRetrieveState = new PausedRetrieveState();
        PausedRetrieveState.setPaused(false);
        PausedRetrieveState.setCreated(LocalDateTime.now(CLOCK));
        when(mPausedRetrieveStateService.getCurrentState()).thenReturn(PausedRetrieveState);

        setupUser("bob",false);
        mvc.perform(get("/admin/paused/retrieve/state")
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
        verify(mPausedRetrieveStateService).getCurrentState();
        verifyNoMoreInteractions(mPausedRetrieveStateService);
    }

    @ParameterizedTest
    @ValueSource(ints = {0,1,10,12})
    @NullSource
    public void testPausedHistory(final Integer limit) throws Exception {
        PausedRetrieveState ps1 = new PausedRetrieveState();
        ps1.setPaused(false);
        ps1.setCreated(LocalDateTime.now(CLOCK));
        PausedRetrieveState ps2 = new PausedRetrieveState();
        ps2.setPaused(true);
        ps2.setCreated(LocalDateTime.now(CLOCK).minusDays(1));
        PausedRetrieveState ps3 = new PausedRetrieveState();
        ps3.setPaused(false);
        ps3.setCreated(LocalDateTime.now(CLOCK).minusDays(2));
        
        int expectedLimit = limit == null ? 10 : limit;
        when(mPausedRetrieveStateService.getRecentEntries(expectedLimit)).thenReturn(List.of(ps1,ps2,ps3));

        setupUser("bob",false);

        String requestURL = "/admin/paused/retrieve/history";
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
        verify(mPausedRetrieveStateService).getRecentEntries(expectedLimit);
        verifyNoMoreInteractions(mPausedRetrieveStateService);
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
        PausedRetrieveState PausedRetrieveState = new PausedRetrieveState();
        when(mPausedRetrieveStateService.toggleState()).thenReturn(PausedRetrieveState);
        
        setupUser("bob", true);

        MvcResult result = mvc.perform(post("/admin/paused/retrieve/toggle")
                .header(HEADER_CLIENT_KEY,"clientKey123")
                .header(HEADER_USER_ID, "bob")
                .with(csrf())).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        verify(mPausedRetrieveStateService).toggleState();
        verifyNoMoreInteractions(mPausedRetrieveStateService);
    }
    @Test
    public void testToggleStateAsNonAdminUser() throws Exception {
        PausedRetrieveState PausedRetrieveState = new PausedRetrieveState();
        when(mPausedRetrieveStateService.toggleState()).thenReturn(PausedRetrieveState);

        setupUser("bob", false);

        MvcResult result = mvc.perform(post("/admin/paused/retrieve/toggle")
                .header(HEADER_CLIENT_KEY,"clientKey123")
                .header(HEADER_USER_ID, "bob")
                .with(csrf())).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

        verify(mPausedRetrieveStateService, never()).toggleState();
        verifyNoMoreInteractions(mPausedRetrieveStateService);
    }
}