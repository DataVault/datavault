package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.event.roles.CreateRoleAssignment;
import org.datavaultplatform.common.event.vault.Create;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.datavaultplatform.broker.services.VaultsService.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultServiceTest {

    
    @Mock
    VaultDAO vaultDAO;

    @Mock
    RolesAndPermissionsService rolesAndPermissionsService;

    @Mock
    RetentionPoliciesService retentionPoliciesService;

    @Mock
    DataCreatorsService dataCreatorsService;

    @Mock
    BillingService billingService;
    
    @Mock
    UsersService usersService;
    
    @Mock
    EventService eventService;
    
    @Mock
    ClientsService clientsService;
    
    @Mock
    EmailService emailService;
    
    VaultsService serviceSpy;
    
    Clock clock;
    LocalDateTime timestamp;
    LocalDate today;
    @BeforeEach
    void setup() {

        clock = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneOffset.UTC);
        timestamp = LocalDateTime.now(clock);
        today = LocalDate.now(clock);
        
        serviceSpy = Mockito.spy(new VaultsService(vaultDAO, rolesAndPermissionsService,
                retentionPoliciesService, dataCreatorsService,
                billingService, usersService, eventService,
                clientsService, emailService, clock));
    }
    
    
    @Test
    void testGetVaults() {
        List<Vault> vaults = new ArrayList<>();
        when(vaultDAO.list()).thenReturn(vaults);
        List<Vault> result = serviceSpy.getVaults();
        assertThat(result).isEqualTo(vaults);
    }
    
    @Nested
    class AddVaultTests {

        @Test
        void testAddVaultArgs() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.addVault(null);
            });
            assertThat(ex).hasMessage("The vault cannot be null");
        }
        
        @Test
        void testAddVault(){
            Vault vault = new Vault();
            serviceSpy.addVault(vault);
            verify(vaultDAO).save(vault);
            assertThat(vault.getCreationTime()).isEqualTo(timestamp);
        }
    }

    @Nested
    class AddVaultEventTests {

        @Test
        void testAddVaultEventArgs() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.addVaultEvent(null, "clientKey", "userId");
            });
            assertThat(ex).hasMessage("The vault cannot be null");
        }

        @Test
        void testAddVaultEventUnknownClientKey() {
            Vault vault = new Vault();
            User user = new User();
            ArgumentCaptor<Create> argCreateVaultEvent = ArgumentCaptor.forClass(Create.class);
            when(usersService.getUser("userId")).thenReturn(user);
            when(clientsService.getClientByApiKey("clientKey")).thenReturn(null);
            
            serviceSpy.addVaultEvent(vault, "clientKey", "userId");

            verify(usersService).getUser("userId");
            verify(clientsService).getClientByApiKey("clientKey");
            verify(eventService).addEvent(argCreateVaultEvent.capture());

            Create event = argCreateVaultEvent.getValue();
            assertThat(event.getVault()).isEqualTo(vault);
            assertThat(event.getUser()).isEqualTo(user);
            assertThat(event.getAgentType()).isEqualTo(Agent.AgentType.BROKER);
            assertThat(event.getAgent()).isEmpty();
        }
        
        @Test
        void testAddVaultEventKnownClientKey() {
            Vault vault = new Vault();
            User user = new User();
            ArgumentCaptor<Create> argCreateVaultEvent = ArgumentCaptor.forClass(Create.class);
            when(usersService.getUser("userId")).thenReturn(user);
            Client client =new Client();
            client.setName("clientName");
            
            when(clientsService.getClientByApiKey("clientKey")).thenReturn(client);

            serviceSpy.addVaultEvent(vault, "clientKey", "userId");

            verify(usersService).getUser("userId");
            verify(clientsService).getClientByApiKey("clientKey");
            verify(eventService).addEvent(argCreateVaultEvent.capture());

            Create event = argCreateVaultEvent.getValue();
            assertThat(event.getVault()).isEqualTo(vault);
            assertThat(event.getUser()).isEqualTo(user);
            assertThat(event.getAgentType()).isEqualTo(Agent.AgentType.BROKER);
            assertThat(event.getAgent()).isEqualTo("clientName");
        }
    }
    @Nested
    class AddRoleEventTests {

        @Test
        void testAddRoleEventArgs() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.addRoleEvent(null, "assigneeId", "clientKey", "userId");
            });
            assertThat(ex).hasMessage("The role assignment cannot be null");
        }

        @Test
        void testAddRoleEventUnknownClientKey() {
            RoleModel role = new RoleModel();
            
            RoleAssignment ra = new RoleAssignment();
            ra.setVaultId("vaultId");
            ra.setRole(role);
            
            User creatorUser = new User();
            User assigneeUser = new User();
            Vault vault = new Vault();
 
            ArgumentCaptor<CreateRoleAssignment> argEvent = ArgumentCaptor.forClass(CreateRoleAssignment.class);
 
            when(usersService.getUser("creatorId")).thenReturn(creatorUser);
            when(usersService.getUser("assigneeId")).thenReturn(assigneeUser);
            when(clientsService.getClientByApiKey("clientKey")).thenReturn(null);

            doReturn(vault).when(serviceSpy).getVault("vaultId");

            serviceSpy.addRoleEvent(ra, "assigneeId", "creatorId", "clientKey");

            verify(usersService).getUser("creatorId");
            verify(usersService).getUser("assigneeId");
            verify(clientsService).getClientByApiKey("clientKey");
            verify(eventService).addEvent(argEvent.capture());
            verify(serviceSpy).getVault("vaultId");
            
            CreateRoleAssignment event = argEvent.getValue();

            assertThat(event.getVault()).isEqualTo(vault);
            assertThat(event.getUser()).isEqualTo(creatorUser);

            assertThat(event.getAgentType()).isEqualTo(Agent.AgentType.BROKER);
            assertThat(event.getAgent()).isEmpty();
            
            assertThat(event.getAssignee()).isEqualTo(assigneeUser);
            assertThat(event.getRole()).isEqualTo(role);
        }

        @Test
        void testAddRoleEventKnownClientKey() {
            RoleModel role = new RoleModel();

            RoleAssignment ra = new RoleAssignment();
            ra.setVaultId("vaultId");
            ra.setRole(role);

            User creatorUser = new User();
            User assigneeUser = new User();
            Vault vault = new Vault();

            ArgumentCaptor<CreateRoleAssignment> argEvent = ArgumentCaptor.forClass(CreateRoleAssignment.class);

            when(usersService.getUser("creatorId")).thenReturn(creatorUser);
            when(usersService.getUser("assigneeId")).thenReturn(assigneeUser);
            Client client = new Client();
            client.setName("clientName");
            when(clientsService.getClientByApiKey("clientKey")).thenReturn(client);

            doReturn(vault).when(serviceSpy).getVault("vaultId");

            serviceSpy.addRoleEvent(ra, "assigneeId", "creatorId", "clientKey");

            verify(usersService).getUser("creatorId");
            verify(usersService).getUser("assigneeId");
            verify(clientsService).getClientByApiKey("clientKey");
            verify(eventService).addEvent(argEvent.capture());
            verify(serviceSpy).getVault("vaultId");

            CreateRoleAssignment event = argEvent.getValue();

            assertThat(event.getVault()).isEqualTo(vault);
            assertThat(event.getUser()).isEqualTo(creatorUser);

            assertThat(event.getAgentType()).isEqualTo(Agent.AgentType.BROKER);
            assertThat(event.getAgent()).isEqualTo("clientName");

            assertThat(event.getAssignee()).isEqualTo(assigneeUser);
            assertThat(event.getRole()).isEqualTo(role);
        }
    }
    
    @Nested
    class SendEmailTests {
        
        @Test
        void testSendEmailBadArgs() {
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.sendEmail(null, "email", "subject", "role", "template", "homePage", "homePage");
            });
            assertThat(ex1).hasMessage("The vault cannot be null");

            Vault vault = new Vault();
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.sendEmail(vault, "email", "subject", "role", "template", "homePage", "homePage");
            });
            assertThat(ex2).hasMessage("The vault group annot be null");

        }
        
        @Test
        void testSendEmail() {
            
            Vault vault = new Vault() {
                @Override
                public String getID() {
                    return "vaultId";
                }
            };
            vault.setName("vaultName");
            vault.setReviewDate(today);
            Group group = new Group();
            group.setName("groupName");
            vault.setGroup(group);
            serviceSpy.sendEmail(vault, "email", "subject", "role", "template", "homePage", "helpPage");
            ArgumentCaptor<Map<String, Object>> argMap = ArgumentCaptor.captor(); 
            verify(emailService).sendTemplateMail(eq("email"), eq("subject"), eq("template"), argMap.capture());
            Map<String,Object> map = argMap.getValue();
            assertThat(map).hasSize(7);
            assertThat(map).containsEntry(EMAIL_HOME_PAGE, "homePage");
            assertThat(map).containsEntry(EMAIL_HELP_PAGE, "helpPage");
            assertThat(map).containsEntry(EMAIL_VAULT_NAME, "vaultName");
            assertThat(map).containsEntry(EMAIL_GROUP_NAME, "groupName");

            assertThat(map).containsEntry(EMAIL_VAULT_ID, "vaultId");
            assertThat(map).containsEntry(EMAIL_VAULT_REVIEW_DATE, today);
            assertThat(map).containsEntry(EMAIL_ROLE_NAME, "role");


        }
        
        @Test
        void testSendVaultOwnerEmail() {
            Vault vault = new Vault();
            User user = new User();
            user.setEmail("user-email");

            doNothing().when(serviceSpy).sendEmail(any(Vault.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

            serviceSpy.sendVaultOwnerEmail(vault, "homePage", "helpPage", user);
            
            verify(serviceSpy).sendEmail(
                    vault,
                    "user-email", 
                    "A new vault you own has been created", 
                    "Owner",
                    EmailTemplate.USER_VAULT_CREATE,
                    "homePage",
                    "helpPage");
        }

        @Test
        void testSendVaultDepositorsEmail() {
            Vault vault = new Vault();
            User user = new User();
            user.setEmail("user-email");

            doNothing().when(serviceSpy).sendEmail(any(Vault.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

            serviceSpy.sendVaultDepositorsEmail(vault, "homePage", "helpPage", user);

            verify(serviceSpy).sendEmail(
                    vault,
                    "user-email",
                    "A new vault you have a role on has been created",
                    "Depositor",
                    EmailTemplate.USER_VAULT_CREATE,
                    "homePage",
                    "helpPage");
        }
        @Test
        void sendVaultNDMsEmail() {
            Vault vault = new Vault();
            User user = new User();
            user.setEmail("user-email");

            doNothing().when(serviceSpy).sendEmail(any(Vault.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

            serviceSpy.sendVaultNDMsEmail(vault, "homePage", "helpPage", user);

            verify(serviceSpy).sendEmail(
                    vault,
                    "user-email",
                    "A new vault you have a role on has been created",
                    "Nominated Data Manager",
                    EmailTemplate.USER_VAULT_CREATE,
                    "homePage",
                    "helpPage");
        }
        
    }
}
