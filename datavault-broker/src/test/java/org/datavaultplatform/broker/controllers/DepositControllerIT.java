package org.datavaultplatform.broker.controllers;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockRabbitConfig;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.retrieve.RetrieveStart;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.model.dao.RetrieveDAO;
import org.datavaultplatform.common.model.dao.UserDAO;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
        "logging.level.org.springframework.security=TRACE",
        "broker.security.enabled=true",
        "broker.scheduled.enabled=false",
        "broker.controllers.enabled=true",
        "broker.services.enabled=true",
        "broker.rabbit.enabled=false",
        "broker.ldap.enabled=false",
        "broker.initialise.enabled=false",
        "broker.email.enabled=false",
        "broker.database.enabled=true"})
@Import({MockRabbitConfig.class}) //cos spring security requires some services so we have to mock them
@AutoConfigureMockMvc
class DepositControllerIT extends BaseDatabaseTest {

    public static final String NON_RESTART_JOB_ID = "testNonRestartJobId";
    
    @MockBean
    EmailService emailService;
    
    @Autowired
    DepositsController controller;
    
    DepositsController controllerSpy;
    
    @Autowired
    DepositDAO depositDAO;

    @Autowired
    RetrieveDAO retrieveDAO;

    @Autowired
    VaultDAO vaultDAO;

    @Autowired
    UserDAO userDAO;

    User user1;
    
    Deposit deposit1;
    
    Deposit deposit2;
    
    Job retrieveJob;
    
    Vault vault;
    
    Retrieve retrieve1;
    @Autowired
    private UsersService usersService;
    @Autowired
    private EventService eventService;
    
    @Autowired
    JobsService jobsService;
    
    @Autowired
    DepositsService depositsService;
    
    @Captor
    ArgumentCaptor<Event> argLastEvent;

    RetrieveStart retrieveStart;
    
    @BeforeEach
    void setup() {
        
        vault = new Vault();
        vault.setDescription("test-vault");
        vault.setContact("test-contact");
        vault.setName("test-vault-name");
        vault.setReviewDate(new Date());
        vaultDAO.save(vault);
        
        user1 = new User();
        user1.setFirstname("first");
        user1.setLastname("last");
        user1.setID("user-1-id");
        user1.setEmail("test@test.com");
        userDAO.save(user1);
        
        deposit1 = new Deposit();
        deposit1.setHasPersonalData(false);
        deposit1.setName("deposit1");
        deposit1.setVault(vault);
        deposit1.setNonRestartJobId(NON_RESTART_JOB_ID);
        depositDAO.save(deposit1);
        
        retrieveJob = new Job(Job.TASK_CLASS_RETRIEVE);
        jobsService.addJob(deposit1, retrieveJob);
        assertThat(retrieveJob).isNotNull();

        deposit2 = new Deposit();
        deposit2.setHasPersonalData(false);
        deposit2.setName("deposit2");
        deposit2.setVault(vault);
        deposit2.setNonRestartJobId(NON_RESTART_JOB_ID);

        depositDAO.save(deposit2);
        
        retrieve1 = new Retrieve();
        retrieve1.setDeposit(deposit1);
        retrieve1.setNote("retrieve-one");
        retrieve1.setHasExternalRecipients(false);
        retrieveDAO.save(retrieve1);
        
        controllerSpy = spy(controller);

        retrieveStart = new RetrieveStart();
        retrieveStart.setDeposit(deposit1);
        retrieveStart.setRetrieveId(retrieve1.getID());
        retrieveStart.setJob(retrieveJob);
        
        eventService.addEvent(retrieveStart);
        
        Event byID = eventService.findById(retrieveStart.getID());
        assertThat(byID.getID()).isEqualTo(retrieveStart.getID());
        
        User found = userDAO.findById(user1.getID()).get();
        assertThat(found).isEqualTo(user1);

        User found2 = usersService.getUser(user1.getID());
        assertThat(found2).isEqualTo(user1);
        
        Event lastEvent = depositsService.getLastNotFailedRetrieveEvent(deposit1.getID(), retrieve1.getID());
        assertThat(lastEvent).isNotNull();
    }
    
    @Test
    @Transactional
    void testRestartRetrieveWhereDepositAndRetrieveMatches() throws Exception {
        
        doReturn(true).when(controllerSpy).runRetrieveDeposit(eq(user1), eq(deposit1), eq(retrieve1), argLastEvent.capture());
        
        boolean result = controllerSpy.retrieveDepositRestart(user1.getID(), deposit1.getID(), retrieve1.getID());
        assertThat(result).isTrue();

        Event actualLastEvent = argLastEvent.getValue();
        assertThat(actualLastEvent.getID()).isEqualTo(this.retrieveStart.getID());

        verify(controllerSpy).runRetrieveDeposit(user1, deposit1, retrieve1, actualLastEvent);
    }

    @Test
    @Transactional
    void testRestartRetrieveWhereDepositAndRetrieveDoNotMatch() throws Exception{

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            controllerSpy.retrieveDepositRestart(user1.getID(), deposit2.getID(), retrieve1.getID());
        });
        assertThat(ex).hasMessage("The depositId[%s] does not match retrieve's depositId[%s]".formatted(deposit2.getID(), deposit1.getID()));

        verify(controllerSpy, never()).runRetrieveDeposit(any(User.class), any(Deposit.class), any(Retrieve.class), any(Event.class));
    }
}
