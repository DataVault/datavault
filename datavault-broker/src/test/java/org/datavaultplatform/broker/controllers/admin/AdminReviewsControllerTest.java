package org.datavaultplatform.broker.controllers.admin;

import lombok.SneakyThrows;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReviewsControllerTest {

    AdminReviewsController controller;
    @Mock
    VaultsService mVaultsService;

    @Mock
    VaultsReviewService mVaultsReviewService;

    @Mock
    DepositsReviewService mDepositsReviewService;

    @Mock
    UsersService mUsersService;

    @Mock
    ClientsService mClientsService;

    @Mock
    EventService mEventService;
    
    static final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setup() {
        this.controller = new AdminReviewsController(mVaultsService, mVaultsReviewService, mDepositsReviewService, mUsersService, mClientsService, mEventService);
    }

    @Nested
    class GetVaultReviewTests {

        @Test
        void testNullArg() {

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> AdminReviewsController.getReviewInfo(null));
            assertThat(ex).hasMessage("The vaultReview cannot be null");
        }

        @Test
        void testEmptyVaultReview() {
            ReviewInfo info = AdminReviewsController.getReviewInfo(new VaultReview());
            assertThat(info.getVaultReviewId()).isNull();
            assertThat(info.getDepositIds()).isEmpty();
            assertThat(info.getDepositReviewIds()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"1", "2", "3"})
        @NullSource
        void testNonEmptyVaultReview(String vaultReviewId) {

            DepositReview dr1 = null;
            DepositReview dr2 = new DepositReview();
            dr2.setDeposit(new Deposit());
            DepositReview dr3 = new DepositReview();
            DepositReview dr4 = createDepositReview(null, null);
            DepositReview dr5 = createDepositReview("depositId-5", null);
            DepositReview dr6 = createDepositReview(null, "depositReviewId-6");
            DepositReview dr7 = createDepositReview("depositId-7", "depositReviewId-7");

            VaultReview vr = new VaultReview();
            vr.setId(vaultReviewId);
            //noinspection ConstantValue
            vr.setDepositReviews(Arrays.asList(dr1, dr2, dr3, dr4, dr5, dr6, dr7));

            ReviewInfo info = AdminReviewsController.getReviewInfo(vr);

            assertThat(info.getVaultReviewId()).isEqualTo(vaultReviewId);

            assertThat(info).isNotNull();
            List<String> depositIds = info.getDepositIds();
            List<String> depositReviewIds = info.getDepositReviewIds();

            assertThat(depositIds)
                    .hasSize(6)
                    .hasSameSizeAs(depositReviewIds)
                    .isEqualTo(Arrays.asList(null, null, null, "depositId-5", null, "depositId-7"));

            assertThat(depositReviewIds)
                    .isEqualTo(Arrays.asList(null, null, null, null, "depositReviewId-6", "depositReviewId-7"));
        }

        private DepositReview createDepositReview(String depositId, String depositReviewId) {
            DepositReview dr = new DepositReview();
            dr.setId(depositReviewId);
            Deposit d = new Deposit() {
                @Override
                public String getID() {
                    return depositId;
                }
            };
            dr.setDeposit(d);
            return dr;
        }
    }

    @Nested
    class GetVaultsForReviewTests {

        @Test
        void testNoVaultsForReview() {

            List<Vault> vaults = List.of();
            when(mVaultsService.getVaults()).thenReturn(vaults);

            when(mVaultsReviewService.getVaultsForReview(vaults)).thenReturn(null);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
                controller.getVaultsForReview("userId");
            });
            assertThat(ex).hasMessage("The vaultsForReview should not be null");

            verify(mVaultsService).getVaults();
            verify(mVaultsReviewService).getVaultsForReview(vaults);

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }

        @Test
        void testVaultsForReview() {


            List<Vault> vaults = List.of();
            when(mVaultsService.getVaults()).thenReturn(vaults);

            Vault mVault1 = mock(Vault.class);
            Vault mVault2 = mock(Vault.class);

            VaultInfo vaultInfo1 = new VaultInfo();
            VaultInfo vaultInfo2 = new VaultInfo();
            when(mVault1.convertToResponse()).thenReturn(vaultInfo1);
            when(mVault2.convertToResponse()).thenReturn(vaultInfo2);

            List<Vault> vaultsForReview = Arrays.asList(mVault1, null, mVault2);
            when(mVaultsReviewService.getVaultsForReview(vaults)).thenReturn(vaultsForReview);

            VaultsData result = controller.getVaultsForReview("userId");
            assertThat(result.getData()).isEqualTo(List.of(vaultInfo1, vaultInfo2));

            verify(mVaultsService).getVaults();
            verify(mVaultsReviewService).getVaultsForReview(vaults);

            verify(mVault1).convertToResponse();
            verify(mVault2).convertToResponse();

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }
    }

    @Nested
    class GetCurrentReviewTest {

        @Test
        @SneakyThrows
        void testNullUserId() {
            doThrow(IllegalArgumentException.class).when(mUsersService).getUser(null);

            assertThrows(IllegalArgumentException.class, () -> {
                controller.getCurrentReview(null, "vaultId");
            });

            verify(mUsersService).getUser(null);

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }

        @Test
        @SneakyThrows
        void testUserIdNotFound() {
            doReturn(null).when(mUsersService).getUser("userId");

            ReviewInfo reviewInfo = controller.getCurrentReview("userId", "vaultId");
            assertThat(reviewInfo).isNull();

            verify(mUsersService).getUser("userId");

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }

        @Test
        @SneakyThrows
        void testUserVaultNotFound() {
            User user1 = new User();
            doReturn(user1).when(mUsersService).getUser("userId");

            when(mVaultsService.getUserVault(user1, "vaultId")).thenThrow(new Exception("VAULT NOT FOUND"));

            Exception ex = assertThrows(Exception.class, () -> controller.getCurrentReview("userId", "vaultId"));
            assertThat(ex).hasMessage("VAULT NOT FOUND");

            verify(mUsersService).getUser("userId");
            verify(mVaultsService).getUserVault(user1, "vaultId");

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }

        @Test
        @SneakyThrows
        void testVaultHasNoMostRecentVaultReview() {
            User user1 = new User();
            doReturn(user1).when(mUsersService).getUser("userId");

            Vault vault = new Vault();
            when(mVaultsService.getUserVault(user1, "vaultId")).thenReturn(vault);

            ReviewInfo reviewInfo = controller.getCurrentReview("userId", "vaultId");
            assertThat(reviewInfo).isNull();

            verify(mUsersService).getUser("userId");
            verify(mVaultsService).getUserVault(user1, "vaultId");

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }

        @Test
        @SneakyThrows
        void testVaultHasMostRecentVaultReview() {
            User user1 = new User();
            doReturn(user1).when(mUsersService).getUser("userId");

            Vault mVault = mock(Vault.class);

            VaultReview vaultReview = new VaultReview();
            when(mVault.findLatestVaultReviewIfStillUnderway()).thenReturn(Optional.of(vaultReview));

            when(mVaultsService.getUserVault(user1, "vaultId")).thenReturn(mVault);

            ReviewInfo reviewInfo1 = new ReviewInfo();

            /*
                we mock the part where getReviewInfo static method is called
                we've got a separate test for that static method
             */
            try (MockedStatic<AdminReviewsController> mockStatic = Mockito.mockStatic(AdminReviewsController.class)) {
                mockStatic.when(() -> AdminReviewsController.getReviewInfo(vaultReview))
                        .thenReturn(reviewInfo1);

                // When you call the static method here, it returns the mock value
                ReviewInfo result = controller.getCurrentReview("userId", "vaultId");
                assertThat(result).isEqualTo(reviewInfo1);

                mockStatic.verify(() -> {
                    AdminReviewsController.getReviewInfo(vaultReview);
                });
            }

            verify(mUsersService).getUser("userId");
            verify(mVaultsService).getUserVault(user1, "vaultId");
            verify(mVault).findLatestVaultReviewIfStillUnderway();

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService, mVault);
        }
    }
    
    @Nested
    class CreateCurrentReviewTests {
        
        @Test
        @SneakyThrows
        void testCreateCurrentReview() {
         
            User user = new User();
            Vault vault = new Vault();
            when(mUsersService.getUser("userId")).thenReturn(user);
            when(mVaultsService.getUserVault(user, "vaultId")).thenReturn(vault);
            
            VaultReview vaultReview = new VaultReview();
            when(mVaultsReviewService.createVaultReview(vault)).thenReturn(vaultReview);

            ReviewInfo reviewInfo1 = new ReviewInfo();
            try (MockedStatic<AdminReviewsController> mockStatic = Mockito.mockStatic(AdminReviewsController.class)) {
                mockStatic.when(() -> AdminReviewsController.getReviewInfo(vaultReview))
                        .thenReturn(reviewInfo1);

                // When you call the static method here, it returns the mock value
                ReviewInfo result = controller.createCurrentReview("userId", "vaultId");
                assertThat(result).isEqualTo(reviewInfo1);

                mockStatic.verify(() -> {
                    AdminReviewsController.getReviewInfo(vaultReview);
                });
            }

            verify(mUsersService).getUser("userId");
            verify(mVaultsService).getUserVault(user, "vaultId");
            verify(mVaultsReviewService).createVaultReview(vault);

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }
    }
    
    @Nested
    class EditVaultReviewTests {
        
        VaultReview vaultReview;
        
        @BeforeEach
        void setup() {
            vaultReview = new VaultReview();
            doNothing().when(mVaultsReviewService).updateVaultReview(vaultReview);
        }

        @Test
        void testVaultReviewWithNoActionedDate() {
            vaultReview.setActionedDate(null);
            
            VaultReview result = controller.editVaultReview("userId", "clientKey", vaultReview);
            assertThat(result).isEqualTo(vaultReview);
            
            verify(mVaultsReviewService).updateVaultReview(vaultReview);

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }
        
        @Test
        void testVaultReviewWithActionedDateButNullVault(){
            vaultReview.setActionedDate(NOW);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
                controller.editVaultReview("userId", "clientKey", vaultReview);
            });
            assertThat(ex).hasMessage("The VaultReview cannot have null Vault");

            verify(mVaultsReviewService).updateVaultReview(vaultReview);

            
            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);

        }
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"client1", "client2"})
        void testVaultReviewWithActionedDateAndVault(String clientName){
            ArgumentCaptor<Event> argEvent = ArgumentCaptor.forClass(Event.class);
            Vault vault = new Vault();
            vaultReview.setActionedDate(NOW);
            vaultReview.setVault(vault);
            
            User user = new User();
            when(mUsersService.getUser("userId")).thenReturn(user);
            Client client;
            if (clientName == null) {
                client = null;
            } else {
                client = new Client();
                client.setName(clientName);
            }
            when(mClientsService.getClientByApiKey("clientKey")).thenReturn(client);

            VaultReview result = controller.editVaultReview("userId", "clientKey", vaultReview);
            
            assertThat(result).isEqualTo(vaultReview);

            verify(mVaultsReviewService).updateVaultReview(vaultReview);
            verify(mUsersService).getUser("userId");
            verify(mClientsService).getClientByApiKey("clientKey");

            verify(mEventService).addEvent(argEvent.capture());
            
            Event event = argEvent.getValue();
            assertThat(event.getAgent()).isEqualTo(clientName);
            assertThat(event.getAgentType()).isEqualTo(Agent.AgentType.BROKER);
            assertThat(event.getUser()).isEqualTo(user);
            assertThat(event.getVault()).isEqualTo(vault);

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }
    }
    
    @Nested
    class EditDepositReviewTests {
        
        @Test
        void testEditVaultReview() {
            DepositReview depositReview = new DepositReview();

            DepositReview result = controller.editDepositReview("userId", depositReview);
            assertThat(result).isEqualTo(depositReview);

            verify(mDepositsReviewService).updateDepositReview(depositReview);

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }
    }
}