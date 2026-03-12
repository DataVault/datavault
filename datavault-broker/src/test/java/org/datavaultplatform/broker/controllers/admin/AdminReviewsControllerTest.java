package org.datavaultplatform.broker.controllers.admin;

import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.response.ReviewInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
}