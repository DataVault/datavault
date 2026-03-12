package org.datavaultplatform.broker.controllers.admin;

import lombok.SneakyThrows;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.ReviewInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
            vr.setDepositReviews(Arrays.asList(dr1, dr2, dr3, dr4, dr5, dr6, dr7));

            ReviewInfo info = AdminReviewsController.getReviewInfo(vr);
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
            when(mVault.getMostRecentVaultReview()).thenReturn(Optional.of(vaultReview));

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

            verifyNoMoreInteractions(mClientsService, mEventService, mDepositsReviewService, mUsersService,
                    mVaultsReviewService, mVaultsService);
        }
    }
}