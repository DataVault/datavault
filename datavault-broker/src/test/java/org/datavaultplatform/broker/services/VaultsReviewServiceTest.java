package org.datavaultplatform.broker.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.junit.Test;

public class VaultsReviewServiceTest {

    private VaultsReviewService vaultsReviewService = new VaultsReviewService(null);

    @Test
    public void testIsVaultForReview() {
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
    public void testDueForReviewEmail() {
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
        Calendar c = Calendar.getInstance();

        c.setTime(new Date());
        c.add(Calendar.MONTH, reviewDateOffset);
        Date reviewDate = c.getTime();

        c.setTime(new Date());
        c.add(Calendar.MONTH, actionedDateOffset);
        Date actionedDate = c.getTime();

        VaultReview vaultReview = new VaultReview();
        vaultReview.setActionedDate(actionedDate);
        List<VaultReview> vaultReviews = Collections.singletonList(vaultReview);

        Vault vault = new Vault();
        vault.setReviewDate(reviewDate);
        vault.setVaultReviews(vaultReviews);

        return vaultsReviewService.isVaultForReview(vault);
    }

    private boolean dueForReviewEmail(int reviewDateOffset, int actionedDateOffset, boolean actioned) {
        Calendar c = Calendar.getInstance();

        c.setTime(new Date());
        c.add(Calendar.MONTH, reviewDateOffset);
        Date reviewDate = c.getTime();

        c.setTime(new Date());
        c.add(Calendar.MONTH, actionedDateOffset);
        Date actionedDate = c.getTime();

        VaultReview vaultReview = new VaultReview();
        if (actioned) {
            vaultReview.setActionedDate(actionedDate);
        }
        List<VaultReview> vaultReviews = Collections.singletonList(vaultReview);

        Vault vault = new Vault();
        vault.setReviewDate(reviewDate);
        vault.setVaultReviews(vaultReviews);

        return vaultsReviewService.dueForReviewEmail(vault);
    }

}
