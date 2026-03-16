package org.datavaultplatform.broker.services;

import lombok.SneakyThrows;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.event.roles.CreateRoleAssignment;
import org.datavaultplatform.common.event.vault.Create;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.datavaultplatform.common.request.CreateVault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.datavaultplatform.broker.services.VaultsService.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultsServiceTest {

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

        serviceSpy = Mockito.spy(new VaultsService(vaultDAO, rolesAndPermissionsService, retentionPoliciesService, dataCreatorsService, billingService, usersService, eventService, clientsService, emailService, clock));
    }


    @Test
    void testGetVaults() {
        List<Vault> vaults = new ArrayList<>();
        when(vaultDAO.list()).thenReturn(vaults);
        List<Vault> result = serviceSpy.getVaults();
        assertThat(result).isEqualTo(vaults);
    }

    @Test
    void testSearch() {
        List<Vault> searchResult = new ArrayList<>();

        when(vaultDAO.search("userId", "query", "sort", "order", "offset", "maxResult")).thenReturn(searchResult);

        List<Vault> result = serviceSpy.search("userId", "query", "sort", "order", "offset", "maxResult");

        assertThat(result).isEqualTo(searchResult);

        verify(vaultDAO).search("userId", "query", "sort", "order", "offset", "maxResult");

        verifyNoMoreInteractions(vaultDAO);
    }

    @Test
    void testCount() {
        when(vaultDAO.count("userId")).thenReturn(1234);
        int result = serviceSpy.count("userId");
        assertThat(result).isEqualTo(1234);
        verify(vaultDAO).count("userId");
        verifyNoMoreInteractions(vaultDAO);
    }

    @Test
    void testGetRetentionPolicyCount() {
        when(vaultDAO.getRetentionPolicyCount(123)).thenReturn(123456);
        int result = serviceSpy.getRetentionPolicyCount(123);
        assertThat(result).isEqualTo(123456);
        verify(vaultDAO).getRetentionPolicyCount(123);
        verifyNoMoreInteractions(vaultDAO);
    }

    @SuppressWarnings("CodeBlock2Expr")
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
        void testAddVault() {
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
            Client client = new Client();
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

    @SuppressWarnings("CodeBlock2Expr")
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
            assertThat(ex2).hasMessage("The vault group cannot be null");

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
            Map<String, Object> map = argMap.getValue();
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

            verify(serviceSpy).sendEmail(vault, "user-email", "A new vault you own has been created", "Owner", EmailTemplate.USER_VAULT_CREATE, "homePage", "helpPage");
        }

        @Test
        void testSendVaultDepositorsEmail() {
            Vault vault = new Vault();
            User user = new User();
            user.setEmail("user-email");

            doNothing().when(serviceSpy).sendEmail(any(Vault.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

            serviceSpy.sendVaultDepositorsEmail(vault, "homePage", "helpPage", user);

            verify(serviceSpy).sendEmail(vault, "user-email", "A new vault you have a role on has been created", "Depositor", EmailTemplate.USER_VAULT_CREATE, "homePage", "helpPage");
        }

        @Test
        void sendVaultNDMsEmail() {
            Vault vault = new Vault();
            User user = new User();
            user.setEmail("user-email");

            doNothing().when(serviceSpy).sendEmail(any(Vault.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

            serviceSpy.sendVaultNDMsEmail(vault, "homePage", "helpPage", user);

            verify(serviceSpy).sendEmail(vault, "user-email", "A new vault you have a role on has been created", "Nominated Data Manager", EmailTemplate.USER_VAULT_CREATE, "homePage", "helpPage");
        }

    }

    @Nested
    class OrphanVaultTests {

        @Test
        void testNullVault() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.orphanVault(null);
            });
            assertThat(ex).hasMessage("The vault cannot be null");
        }

        @Test
        void testNoMatchingRoleAssignmentsForVault() {
            Vault vault = new Vault() {
                @Override
                public String getID() {
                    return "vaultId1";
                }
            };
            RoleModel roleDataOwner = new RoleModel();
            roleDataOwner.setId(1234L);
            RoleAssignment roleAssignment1 = new RoleAssignment();
            roleAssignment1.setVaultId("vaultId2");

            when(rolesAndPermissionsService.getDataOwner()).thenReturn(roleDataOwner);
            when(rolesAndPermissionsService.getRoleAssignmentsForRole(1234L)).thenReturn(List.of(roleAssignment1));
            serviceSpy.orphanVault(vault);

            verify(rolesAndPermissionsService).getDataOwner();
            verify(rolesAndPermissionsService).getRoleAssignmentsForRole(1234L);
            verify(rolesAndPermissionsService, never()).deleteRoleAssignment(anyLong());
            verifyNoMoreInteractions(rolesAndPermissionsService);
        }

        @Test
        void testMatchingRoleAssignmentsForVault() {
            Vault vault = new Vault() {
                @Override
                public String getID() {
                    return "vaultId1";
                }
            };
            RoleModel roleDataOwner = new RoleModel();
            roleDataOwner.setId(1234L);
            RoleAssignment roleAssignment1 = new RoleAssignment();
            roleAssignment1.setId(1111L);
            roleAssignment1.setVaultId("vaultId2");

            RoleAssignment roleAssignment2 = new RoleAssignment();
            roleAssignment2.setId(2222L);
            roleAssignment2.setVaultId("vaultId1");

            RoleAssignment roleAssignment3 = new RoleAssignment();
            roleAssignment3.setId(3333L);
            roleAssignment3.setVaultId("vaultId1");

            when(rolesAndPermissionsService.getDataOwner()).thenReturn(roleDataOwner);
            when(rolesAndPermissionsService.getRoleAssignmentsForRole(1234L)).thenReturn(List.of(roleAssignment1, roleAssignment2, roleAssignment3));
            serviceSpy.orphanVault(vault);

            verify(rolesAndPermissionsService).getDataOwner();
            verify(rolesAndPermissionsService).getRoleAssignmentsForRole(1234L);

            // we only delete the first matching roleAssignment
            verify(rolesAndPermissionsService).deleteRoleAssignment(2222L);

            verifyNoMoreInteractions(rolesAndPermissionsService);
        }
    }

    @Nested
    class UpdateVaultTests {

        @Test
        void testUploadVault() {
            Vault vault = new Vault();
            serviceSpy.updateVault(vault);
            verify(vaultDAO).update(vault);
            verifyNoMoreInteractions(vaultDAO);
        }
    }

    @Nested
    class SaveOrUpdateVaultTests {

        @Test
        void testSaveOrUploadVault() {
            Vault vault = new Vault();
            serviceSpy.saveOrUpdateVault(vault);
            verify(vaultDAO).saveOrUpdateVault(vault);
            verifyNoMoreInteractions(vaultDAO);
        }
    }

    @Nested
    class GetVaultTests {

        @Test
        void testVaultDoesNotExist() {
            when(vaultDAO.findById("vaultId")).thenReturn(Optional.empty());
            Vault vault = serviceSpy.getVault("vaultId");
            assertThat(vault).isNull();
            verify(vaultDAO).findById("vaultId");
            verifyNoMoreInteractions(vaultDAO);
        }

        @Test
        void testVaultExists() {
            Vault vault = new Vault();
            when(vaultDAO.findById("vaultId")).thenReturn(Optional.of(vault));
            Vault result = serviceSpy.getVault("vaultId");
            assertThat(result).isEqualTo(vault);
            verify(vaultDAO).findById("vaultId");
            verifyNoMoreInteractions(vaultDAO);
        }
    }

    @Nested
    class CheckRetentionPolicyTests {

        @Test
        void testCheckRetentionPolicyVaultFound() {
            Vault vault = new Vault() {
                @Override
                public String getID() {
                    return "vaultId";
                }
            };

            try (MockedStatic<RetentionPoliciesService> mockStatic = Mockito.mockStatic(RetentionPoliciesService.class)) {

                //setup mocks
                doReturn(vault).when(serviceSpy).getVault("vaultId");
                when(vaultDAO.update(vault)).thenReturn(vault);

                // this is the method we are testing
                serviceSpy.checkRetentionPolicy("vaultId");

                // we just verify static invocation as updateRetentionPolicyExpiryDate does not return anything
                mockStatic.verify(() -> RetentionPoliciesService.updateRetentionPolicyExpiryDate(vault, clock));
                mockStatic.verifyNoMoreInteractions();

                //verify mocks
                verify(serviceSpy).getVault("vaultId");
                verify(vaultDAO).update(vault);
                verifyNoMoreInteractions(vaultDAO);
            }
        }
        
        @Test
        void testCheckRetentionPolicyVaultNotFound() {
            Vault vault = new Vault() {
                @Override
                public String getID() {
                    return "vaultId";
                }
            };

            try (MockedStatic<RetentionPoliciesService> mockStatic = Mockito.mockStatic(RetentionPoliciesService.class)) {

                //setup mocks
                doReturn(null).when(serviceSpy).getVault("vaultId");

                // this is the method we are testing
                serviceSpy.checkRetentionPolicy("vaultId");

                // we just verify static invocation as updateRetentionPolicyExpiryDate does not return anything
                mockStatic.verifyNoMoreInteractions();

                //verify mocks
                verify(serviceSpy).getVault("vaultId");
                verifyNoMoreInteractions(vaultDAO);
            }
        }
    }
    
    @Nested
    class GetUserVaultTests {
        
        @Test
        void getUserVaultWhereVaultNotFound() {

            doReturn(null).when(serviceSpy).getVault("vaultId");
            Exception ex = assertThrows(Exception.class, () -> {
                serviceSpy.getUserVault(new User(), "vaultId");
            });
            assertThat(ex).hasMessage("Vault 'vaultId' does not exist");
            verify(serviceSpy).getVault("vaultId");
            verifyNoMoreInteractions(vaultDAO);
        }
        
        @Test
        @SneakyThrows
        void getUserVaultWhereVaultFound(){
            Vault vault = new Vault(){
                @Override
                public String getID() {
                    return "vaultId";
                }
            };
            doReturn(vault).when(serviceSpy).getVault("vaultId");
            
            Vault result = serviceSpy.getUserVault(new User(), "vaultId");
            assertThat(result).isEqualTo(vault);
            verify(serviceSpy).getVault("vaultId");
            verifyNoMoreInteractions(vaultDAO);
        }
    }

    @Nested
    class GetTotalNumberOfVaultsTests {

        @Test
        void testGetTotalNumberOfVaultsWithoutQuery() {
            when(vaultDAO.getTotalNumberOfVaults("userId")).thenReturn(123);

            int result = serviceSpy.getTotalNumberOfVaults("userId");
            assertThat(result).isEqualTo(123);

            verify(vaultDAO).getTotalNumberOfVaults("userId");
            verifyNoMoreInteractions(vaultDAO);
        }

        @Test
        void testGetTotalNumberOfVaultsWithQuery() {
            when(vaultDAO.getTotalNumberOfVaults("userId", "query")).thenReturn(123);

            int result = serviceSpy.getTotalNumberOfVaults("userId", "query");
            assertThat(result).isEqualTo(123);

            verify(vaultDAO).getTotalNumberOfVaults("userId", "query");
            verifyNoMoreInteractions(vaultDAO);
        }
    }
    
    @Nested
    class GetAllProjectsSizeTests {
        
        @Test
        void testGetAllProjectsSizeReturnsNull() {
            when(vaultDAO.getAllProjectsSize()).thenReturn(null);
            Map<String, Long> result = serviceSpy.getAllProjectsSize();
            assertThat(result).isEmpty();
            verify(vaultDAO).getAllProjectsSize();
            verifyNoMoreInteractions(vaultDAO);
        }

        @SuppressWarnings("MismatchedReadAndWriteOfArray")
        @Test
        void testGetAllProjectsSizeReturnsListOfObjectArrays() {
            Object[] arr1 = {};
            Object[] arr2 = {"2"};
            Object[] arr3 = {"3", 3333L};
            Object[] arr4 = {"4", 4444L, "blah", null, "blah"};
            Object[] arr5 = {"5", null, null};
            Object[] arr6 = {"", 6666L, null};

            List<Object[]> listOfObjectArrays = Arrays.asList(null, arr1, arr2, arr3, arr4, arr5, arr6);
            when(vaultDAO.getAllProjectsSize()).thenReturn(listOfObjectArrays);
            Map<String, Long> result = serviceSpy.getAllProjectsSize();
            assertThat(result).hasSize(4);
            assertThat(result).containsEntry("3", 3333L);
            assertThat(result).containsEntry("4", 4444L);
            assertThat(result).containsEntry("5", null);
            assertThat(result).containsEntry("", 6666L);

            verify(vaultDAO).getAllProjectsSize();
            verifyNoMoreInteractions(vaultDAO);
        }
    }
    
    @Nested
    class TransferVaultTests {

        @Captor
        ArgumentCaptor<RoleAssignment> argRoleAssignment;
        @Captor
        ArgumentCaptor<Vault> argVault;

        final RoleModel dataOwnerRole = new RoleModel(){
            @Override
            public Long getId() {
                return 123L;
            }
        };

        final User newOwner = new User(){
            @Override
            public String getID() {
                return "newOwnerUserId";
            }
        };

        final Vault vault = new Vault(){
            @Override
            public String getID() {
                return "vaultId";
            }
        };
        
        @Test
        void testNullVaultArg(){
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.transferVault(null, newOwner, "transfer-reason");
            });
            assertThat(ex).hasMessage("The vault cannot be null");   
        }

        @Test
        void testNullNewOwnerArg() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.transferVault(vault, null, "transfer-reason");
            });
            assertThat(ex).hasMessage("The newOwner cannot be null");
        }
        
        @Test
        void testNoExistingRoleAssignment() {
            when(rolesAndPermissionsService.getDataOwner()).thenReturn(dataOwnerRole);
            when(rolesAndPermissionsService.getRoleAssignmentsForRole(dataOwnerRole.getId())).thenReturn(List.of());

            assertThat(vault.getUser()).isNull();
            
            serviceSpy.transferVault(vault, newOwner, "transfer-reason");

            verify(vaultDAO).update(argVault.capture());
            verify(rolesAndPermissionsService).getDataOwner();
            verify(rolesAndPermissionsService).getRoleAssignmentsForRole(dataOwnerRole.getId());
            verify(rolesAndPermissionsService, never()).deleteRoleAssignment(anyLong());
            verify(rolesAndPermissionsService).createRoleAssignment(argRoleAssignment.capture());
            
            Vault updatedVault = argVault.getValue();
            assertThat(updatedVault).isEqualTo(vault);
            assertThat(updatedVault.getUser()).isEqualTo(newOwner);

            RoleAssignment roleAssignment = argRoleAssignment.getValue();

            assertThat(roleAssignment.getVaultId()).isEqualTo(vault.getID());
            assertThat(roleAssignment.getUserId()).isEqualTo(newOwner.getID());
            assertThat(roleAssignment.getRole()).isEqualTo(dataOwnerRole);
            
            verifyNoMoreInteractions(retentionPoliciesService);

        }
 
        @Test
        void testExistingDataRoleAssignmentWillBeDeleted() {

            RoleAssignment roleAssignement1 = new RoleAssignment();
            roleAssignement1.setId(1111L);
            roleAssignement1.setVaultId("vaultId");

            RoleAssignment roleAssignment2 = new RoleAssignment();
            roleAssignment2.setId(2222L);
            roleAssignment2.setVaultId("vaultId");
            
            when(rolesAndPermissionsService.getDataOwner()).thenReturn(dataOwnerRole);
            when(rolesAndPermissionsService.getRoleAssignmentsForRole(dataOwnerRole.getId())).thenReturn(List.of(roleAssignement1, roleAssignment2));

            assertThat(vault.getUser()).isNull();

            serviceSpy.transferVault(vault, newOwner, "transfer-reason");

            verify(vaultDAO).update(argVault.capture());
            verify(rolesAndPermissionsService).getDataOwner();
            verify(rolesAndPermissionsService).getRoleAssignmentsForRole(dataOwnerRole.getId());
            verify(rolesAndPermissionsService).deleteRoleAssignment(1111L);
            verify(rolesAndPermissionsService).createRoleAssignment(argRoleAssignment.capture());

            Vault updatedVault = argVault.getValue();
            assertThat(updatedVault).isEqualTo(vault);
            assertThat(updatedVault.getUser()).isEqualTo(newOwner);

            RoleAssignment roleAssignment = argRoleAssignment.getValue();

            assertThat(roleAssignment.getVaultId()).isEqualTo(vault.getID());
            assertThat(roleAssignment.getUserId()).isEqualTo(newOwner.getID());
            assertThat(roleAssignment.getRole()).isEqualTo(dataOwnerRole);

            verifyNoMoreInteractions(retentionPoliciesService);
        }
    }
    
    @Nested
    class AddRepositorRolesTests {
        
        @Captor
        ArgumentCaptor<RoleAssignment> argRoleAssignment1;
        @Captor
        ArgumentCaptor<RoleAssignment> argRoleAssignment2;
        @Captor
        ArgumentCaptor<User> argUser;
        
        final Vault vault = new Vault(){
            @Override
            public String getID() {
                return "vaultId";
            }
        };
        
        final User user1 = new User();
        final User user2 = new User();

        final CreateVault createVault = new CreateVault();

        final RoleModel depositorRole = new RoleModel();
        
        @Test
        void testNullCreateVaultArg() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
               serviceSpy.addDepositorRoles(null, new Vault(), "clientKey", "homePage", "helpPage"); 
            });
            assertThat(ex).hasMessage("The create vault cannot be null");   
            
            verifyNoMoreInteractions(rolesAndPermissionsService);
        }

        @Test
        void testNullDepositorsLists() {

            createVault.setDepositors(null);

            serviceSpy.addDepositorRoles(createVault, new Vault(), "clientKey", "homePage", "helpPage");

            verify(rolesAndPermissionsService).getDepositor();
            verifyNoMoreInteractions(rolesAndPermissionsService);
        }

        @Test
        void testNullOrEmptyDepositorsLists() {
            
            createVault.setDepositors(Arrays.asList(null, ""));
            
            serviceSpy.addDepositorRoles(createVault, vault, "clientKey", "homePage", "helpPage");

            verify(rolesAndPermissionsService).getDepositor();
            verifyNoMoreInteractions(rolesAndPermissionsService);
        }

        
        @Test
        void testNonEmptyDepositors() {

            createVault.setVaultOwner("createVaultOwner");
            createVault.setVaultCreator("createVaultCreator");
            createVault.setDepositors(Arrays.asList("depositor1", null, "", "depositor2"));

            when(rolesAndPermissionsService.getDepositor()).thenReturn(depositorRole);
            when(usersService.getUser("depositor1")).thenReturn(user1);
            when(usersService.getUser("depositor2")).thenReturn(user2);

            doNothing().when(serviceSpy).addRoleEvent(any(), any(), any(), any());
            doNothing().when(serviceSpy).sendVaultDepositorsEmail(any(), any(), any(), any());

            serviceSpy.addDepositorRoles(createVault, vault, "clientKey", "homePage", "helpPage");

            verify(rolesAndPermissionsService).getDepositor();

            verify(rolesAndPermissionsService, times(2)).createRoleAssignment(argRoleAssignment1.capture());

            verify(serviceSpy, times(2)).addRoleEvent(argRoleAssignment2.capture(), eq("createVaultOwner"), eq("createVaultCreator"), eq("clientKey"));

            RoleAssignment ra1_1 = argRoleAssignment1.getAllValues().get(0);
            assertThat(ra1_1.getRole()).isEqualTo(depositorRole);
            assertThat(ra1_1.getUserId()).isEqualTo("depositor1");
            assertThat(ra1_1.getVaultId()).isEqualTo("vaultId");

            RoleAssignment ra1_2 = argRoleAssignment2.getAllValues().get(0);
            assertThat(ra1_1).isEqualTo(ra1_2);

            RoleAssignment ra2_1 = argRoleAssignment1.getAllValues().get(1);
            assertThat(ra2_1.getRole()).isEqualTo(depositorRole);
            assertThat(ra2_1.getUserId()).isEqualTo("depositor2");
            assertThat(ra2_1.getVaultId()).isEqualTo("vaultId");

            RoleAssignment ra2_2 = argRoleAssignment2.getAllValues().get(1);
            assertThat(ra2_1).isEqualTo(ra2_2);

            verify(serviceSpy, times(2)).sendVaultDepositorsEmail(eq(vault), eq("homePage"), eq("helpPage"), argUser.capture());
            assertThat(argUser.getAllValues().get(0)).isEqualTo(user1);
            assertThat(argUser.getAllValues().get(1)).isEqualTo(user2);

            verifyNoMoreInteractions(rolesAndPermissionsService);
        }
    }

    @Nested
    class AddOwnerRoleTests {

        final Vault vault = new Vault() {
            @Override
            public String getID() {
                return "vaultId";
            }
        };

        @Test
        void testArgs(){
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.addOwnerRole(null, vault, "clientKey");
            });
            assertThat(ex).hasMessage("The create vault cannot be null");
        }
        @Test
        void testNoVaultOwner(){
            CreateVault createVault = new CreateVault();
            createVault.setVaultOwner(null);
            createVault.setVaultCreator("vaultCreatorUserId");
           
            serviceSpy.addOwnerRole(createVault, vault, "clientKey");
            
            verify(rolesAndPermissionsService, never()).getDataOwner();
            verify(rolesAndPermissionsService, never()).createRoleAssignment(any());
        }

        @Captor
        ArgumentCaptor<RoleAssignment> argRoleAssignment1;

        @Captor
        ArgumentCaptor<RoleAssignment> argRoleAssignment2;
        
        @Test
        void testVaultOwner(){
            
            doNothing().when(serviceSpy).addRoleEvent(any(), any(), any(), any());
            
            RoleModel dataOwner = new RoleModel();
            CreateVault createVault = new CreateVault();
            createVault.setVaultOwner("vaultOwnerUserId");
            createVault.setVaultCreator("vaultCreatorUserId");
            
            when(rolesAndPermissionsService.getDataOwner()).thenReturn(dataOwner);

            serviceSpy.addOwnerRole(createVault, vault, "clientKey");

            verify(rolesAndPermissionsService).getDataOwner();
            verify(rolesAndPermissionsService).createRoleAssignment(argRoleAssignment1.capture());
            verify(serviceSpy).addRoleEvent(argRoleAssignment2.capture(), eq("vaultOwnerUserId"), eq("vaultCreatorUserId"), eq("clientKey"));
            
            RoleAssignment roleAssignment1 = argRoleAssignment1.getValue();
            assertThat(roleAssignment1.getRole()).isEqualTo(dataOwner);
            assertThat(roleAssignment1.getVaultId()).isEqualTo("vaultId");
            assertThat(roleAssignment1.getUserId()).isEqualTo("vaultOwnerUserId");

            RoleAssignment roleAssignment2 = argRoleAssignment2.getValue();
            assertThat(roleAssignment2).isEqualTo(roleAssignment1);
            
            verifyNoMoreInteractions(rolesAndPermissionsService);
        }
    }
    @Nested
    class ProcessDataCreatorParamsTest {
        
        @Spy
        Vault spyVault;
        
        @Test
        void testCreatorVaultArg() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
               serviceSpy.processDataCreatorParams(null, spyVault); 
            });
            assertThat(ex).hasMessage("The create vault cannot be null");
            verify(spyVault, never()).setDataCreator(any());
            verify(spyVault, never()).getDataCreators();
            verify(dataCreatorsService, never()).addCreators(any());
            verifyNoMoreInteractions(spyVault, dataCreatorsService);
        }
        
        @Test
        void testNoDataCreators() {
            CreateVault createVault = new CreateVault();
            serviceSpy.processDataCreatorParams(createVault, spyVault);

            verify(spyVault, never()).setDataCreator(any());
            verify(spyVault).getDataCreators();
            verify(dataCreatorsService, never()).addCreators(any());
            verifyNoMoreInteractions(spyVault, dataCreatorsService);
        }
 
        @Captor
        ArgumentCaptor<List<DataCreator>> argCreators1;

        @Captor
        ArgumentCaptor<List<DataCreator>> argCreators2;
        
        @Test
        void testSomeDataCreators() {
            
            CreateVault createVault = new CreateVault();
            createVault.setDataCreators(Arrays.asList("creator1", "creator2", null, ""));
            
            serviceSpy.processDataCreatorParams(createVault, spyVault);

            verify(spyVault).setDataCreator(argCreators1.capture());
            verify(spyVault).getDataCreators();
            verify(dataCreatorsService).addCreators(argCreators2.capture());
            verifyNoMoreInteractions(spyVault, dataCreatorsService);
            
            List<DataCreator> creators1 = argCreators1.getValue();
            List<DataCreator> creators2 = argCreators2.getValue();
            assertThat(creators1).hasSize(2);
            assertThat(creators1).isEqualTo(creators2);
            
            DataCreator dc1 = creators1.get(0);
            assertThat(dc1.getVault()).isEqualTo(spyVault);
            assertThat(dc1.getName()).isEqualTo("creator1");

            DataCreator dc2 = creators1.get(1);
            assertThat(dc2.getVault()).isEqualTo(spyVault);
            assertThat(dc2.getName()).isEqualTo("creator2");
        }
 
    }
    @Nested
    class AddNominatedDataManagerRolesTests {

        @Captor
        ArgumentCaptor<RoleAssignment> argRoleAssignment1;
        @Captor
        ArgumentCaptor<RoleAssignment> argRoleAssignment2;
        @Captor
        ArgumentCaptor<User> argUser;

        final Vault vault = new Vault(){
            @Override
            public String getID() {
                return "vaultId";
            }
        };

        final User user1 = new User();
        final User user2 = new User();

        final CreateVault createVault = new CreateVault();

        final RoleModel nominatedDataManagerRole = new RoleModel();

        @Test
        void testNullCreateVaultArg() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                serviceSpy.addNDMRoles(null, new Vault(), "clientKey", "homePage", "helpPage");
            });
            assertThat(ex).hasMessage("The create vault cannot be null");

            verifyNoMoreInteractions(rolesAndPermissionsService);
        }

        @Test
        void testNullNominatedDataManagersList() {

            createVault.setNominatedDataManagers(null);

            serviceSpy.addNDMRoles(createVault, new Vault(), "clientKey", "homePage", "helpPage");

            verify(rolesAndPermissionsService).getNominatedDataManager();
            verifyNoMoreInteractions(rolesAndPermissionsService);
        }

        @Test
        void testNullOrEmptyNominatedDataManagersList() {

            createVault.setNominatedDataManagers(Arrays.asList(null, ""));

            serviceSpy.addNDMRoles(createVault, vault, "clientKey", "homePage", "helpPage");

            verify(rolesAndPermissionsService).getNominatedDataManager();
            verifyNoMoreInteractions(rolesAndPermissionsService);
        }


        @Test
        void testNonEmptyNominatedDataMangers() {

            createVault.setVaultOwner("createVaultOwner");
            createVault.setVaultCreator("createVaultCreator");
            createVault.setNominatedDataManagers(Arrays.asList("ndm1", null, "", "ndm2"));

            when(rolesAndPermissionsService.getNominatedDataManager()).thenReturn(nominatedDataManagerRole);
            when(usersService.getUser("ndm1")).thenReturn(user1);
            when(usersService.getUser("ndm2")).thenReturn(user2);

            doNothing().when(serviceSpy).addRoleEvent(any(), any(), any(), any());
            doNothing().when(serviceSpy).sendVaultNDMsEmail(any(), any(), any(), any());

            serviceSpy.addNDMRoles(createVault, vault, "clientKey", "homePage", "helpPage");

            verify(rolesAndPermissionsService).getNominatedDataManager();

            verify(rolesAndPermissionsService, times(2)).createRoleAssignment(argRoleAssignment1.capture());

            verify(serviceSpy, times(2)).addRoleEvent(argRoleAssignment2.capture(), eq("createVaultOwner"), eq("createVaultCreator"), eq("clientKey"));

            RoleAssignment ra1_1 = argRoleAssignment1.getAllValues().get(0);
            assertThat(ra1_1.getRole()).isEqualTo(nominatedDataManagerRole);
            assertThat(ra1_1.getUserId()).isEqualTo("ndm1");
            assertThat(ra1_1.getVaultId()).isEqualTo("vaultId");

            RoleAssignment ra1_2 = argRoleAssignment2.getAllValues().get(0);
            assertThat(ra1_1).isEqualTo(ra1_2);

            RoleAssignment ra2_1 = argRoleAssignment1.getAllValues().get(1);
            assertThat(ra2_1.getRole()).isEqualTo(nominatedDataManagerRole);
            assertThat(ra2_1.getUserId()).isEqualTo("ndm2");
            assertThat(ra2_1.getVaultId()).isEqualTo("vaultId");

            RoleAssignment ra2_2 = argRoleAssignment2.getAllValues().get(1);
            assertThat(ra2_1).isEqualTo(ra2_2);

            verify(serviceSpy, times(2)).sendVaultNDMsEmail(eq(vault), eq("homePage"), eq("helpPage"), argUser.capture());
            assertThat(argUser.getAllValues().get(0)).isEqualTo(user1);
            assertThat(argUser.getAllValues().get(1)).isEqualTo(user2);

            verifyNoMoreInteractions(rolesAndPermissionsService);
        }
    }
    
    @Nested
    class AddBillingInfoTests {

        final Vault vault = new Vault() {
            @Override
            public String getID() {
                return "vaultId";
            }
        };

        @Test
        void testNullCreateVault(){
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
               serviceSpy.addBillingInfo(null, vault); 
            });
            verify(billingService, never()).saveOrUpdateVault(any());
        }
        
        @Captor
        ArgumentCaptor<BillingInfo> argBillingInfo;

        CreateVault createVault;

        @BeforeEach
        void setup() {
            createVault = new CreateVault();
            createVault.setGrantAuthoriser("grantAuthorizer");
            createVault.setPaymentDetails("paymentDetails");
            createVault.setBudgetAuthoriser("budgetAuthorizer");
            createVault.setSliceID("sliceId");
            createVault.setBudgetSchoolOrUnit("schoolId");
            createVault.setBudgetSubunit("subunitId");
            createVault.setProjectTitle("projectTitle");

        }
        
        @Test
        void testBillingTypeGrantFunding() {
            createVault.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING.toString());
            
            serviceSpy.addBillingInfo(createVault, vault);
            
            verify(billingService).saveOrUpdateVault(argBillingInfo.capture());
            
            BillingInfo info = argBillingInfo.getValue();
            checkCommon(info, PendingVault.Billing_Type.GRANT_FUNDING);
            assertThat(info.getContactName()).isEqualTo("grantAuthorizer");
        }

        @Test
        void testBillingTypeBudgetCode() {
            createVault.setBillingType(PendingVault.Billing_Type.BUDGET_CODE.toString());

            serviceSpy.addBillingInfo(createVault, vault);

            verify(billingService).saveOrUpdateVault(argBillingInfo.capture());

            BillingInfo info = argBillingInfo.getValue();
            checkCommon(info, PendingVault.Billing_Type.BUDGET_CODE);
            assertThat(info.getContactName()).isEqualTo("budgetAuthorizer");
        }

        @Test
        void testBillingTypeSlice() {
            createVault.setBillingType(PendingVault.Billing_Type.SLICE.toString());

            serviceSpy.addBillingInfo(createVault, vault);

            verify(billingService).saveOrUpdateVault(argBillingInfo.capture());

            BillingInfo info = argBillingInfo.getValue();
            checkCommon(info, PendingVault.Billing_Type.SLICE);
            assertThat(info.getSliceID()).isEqualTo("sliceId");
        }

        @Test
        void testBillingTypeWillPay() {
            createVault.setBillingType(PendingVault.Billing_Type.WILL_PAY.toString());

            serviceSpy.addBillingInfo(createVault, vault);

            verify(billingService).saveOrUpdateVault(argBillingInfo.capture());

            BillingInfo info = argBillingInfo.getValue();
            checkCommon(info, PendingVault.Billing_Type.WILL_PAY);
            assertThat(info.getContactName()).isEqualTo("budgetAuthorizer");
            assertThat(info.getSchool()).isEqualTo("schoolId");
            assertThat(info.getSubUnit()).isEqualTo("subunitId");
            assertThat(info.getProjectTitle()).isEqualTo("projectTitle");
        }

        void checkCommon(BillingInfo info, PendingVault.Billing_Type billingType) {
            assertThat(info.getBillingType()).isEqualTo(billingType);
            assertThat(info.getVault()).isEqualTo(vault);
            BigDecimal billedAmt = info.getAmountBilled();
            Assertions.assertNull(billedAmt);
            BigDecimal toBeBilledAmt = info.getAmountToBeBilled();
            Assertions.assertNull(toBeBilledAmt);
            assertThat(info.getPaymentDetails()).isEqualTo("paymentDetails");
        }
            
        
    }
} 
