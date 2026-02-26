package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.*;
import java.util.Date;
import java.util.List;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.VaultReviewDAO;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VaultsReviewServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2007-12-03T12:00:00.00Z"), ZoneOffset.UTC);
    
    VaultsReviewService vaultsReviewService;

    @Mock
    VaultReviewDAO mVaultReviewDAO;

    @Mock
    DepositsReviewService mDepositReviewService;

    @BeforeEach
    public void setup() {
        vaultsReviewService = new VaultsReviewService(mVaultReviewDAO, mDepositReviewService, CLOCK);
    }

    @Test
    void testIsVaultForReview() {
        System.out.println("Test if vault due for review");

        // todo : needs expanded to be a comprehensive list of tests

        // Review date is today and Actioned date is 9 months ago
        assertTrue(isVaultForReview(0, -9));

        // Review date is 1 month in the past and Actioned date today
        assertFalse(isVaultForReview(-1, 0));

        // Review date is 1 month in the future and Actioned date is 9 months ago
        assertTrue(isVaultForReview(+1, -9));

        // Review date is 12 months in the future and Actioned date is 9 months ago
        assertFalse(isVaultForReview(+12, -9));
    }

    @Test
    void testDueForReviewEmail() {
        System.out.println("Test if vault due for a review email");

        // todo : needs expanded to be a comprehensive list of tests

        // Review date is today and Actioned date is 9 months ago
        assertTrue(dueForReviewEmail(0, -9, true));

        // Review date is 1 month in the past and Actioned date today
        assertFalse(dueForReviewEmail(-1, 0, true));

        // Review date is 1 month in the past and Actioned date not set yet
        assertFalse(dueForReviewEmail(-1, 0, false));

        // Review date is 2 month in the past and Actioned date 1 month ago
        assertFalse(dueForReviewEmail(-2, -1, false));

        // Review date is 1 month in the future and Actioned date is 9 months ago
        assertTrue(dueForReviewEmail(+1, -9, true));

        // Review date is 12 months in the future and Actioned date is 9 months ago
        assertFalse(dueForReviewEmail(+12, -9, true));
    }


    private boolean isVaultForReview(int reviewDateOffset, int actionedDateOffset) {
        LocalDate today = LocalDate.now(CLOCK);

        LocalDate reviewLocalDate = DateTimeUtils.getDateAdjustedByMonths(today, reviewDateOffset);
        Date reviewDate = DateTimeUtils.toDateAtNoon(reviewLocalDate);

        LocalDate actionedLocalDate = DateTimeUtils.getDateAdjustedByMonths(today, actionedDateOffset);
        LocalDateTime actionedLTD = DateTimeUtils.toLocalDateTimeAtNoon(actionedLocalDate);

        VaultReview vaultReview = new VaultReview();
        vaultReview.setActionedDate(actionedLTD);
        List<VaultReview> vaultReviews = List.of(vaultReview);

        Vault vault = new Vault();
        vault.setReviewDate(DateTimeUtils.toLocalDate(reviewDate));
        vault.setVaultReviews(vaultReviews);

        return vaultsReviewService.isVaultForReview(vault);
    }

    private boolean dueForReviewEmail(int reviewDateOffset, int actionedDateOffset, boolean actioned) {
        LocalDate today = LocalDate.now(CLOCK);

        LocalDate reviewLocalDate = DateTimeUtils.getDateAdjustedByMonths(today, reviewDateOffset);
        Date reviewDate = DateTimeUtils.toDateAtNoon(reviewLocalDate);

        LocalDate actionedLocalDate = DateTimeUtils.getDateAdjustedByMonths(today, actionedDateOffset);
        LocalDateTime actionedDate = DateTimeUtils.toLocalDateTimeAtNoon(actionedLocalDate);

        VaultReview vaultReview = new VaultReview();
        if (actioned) {
            vaultReview.setActionedDate(actionedDate);
        }
        List<VaultReview> vaultReviews = List.of(vaultReview);

        Vault vault = new Vault();
        vault.setReviewDate(DateTimeUtils.toLocalDate(reviewDate));
        vault.setVaultReviews(vaultReviews);

        return vaultsReviewService.dueForReviewEmail(vault);
    }

}
