package org.datavaultplatform.webapp.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.VaultReview;

// Used in AdminReviewsController - we are only meant to edit the latest VaultReviews that has no actioned date.
@Slf4j
@Data
@NoArgsConstructor
public class VaultReviewModel {

    private String vaultReviewId;
    private LocalDate nextReviewDate;
    private String comment;

    private List<DepositReviewModel> depositReviewModels = new ArrayList<>();

    public VaultReviewModel(VaultReview vaultReview, LocalDate nextReviewDate) {
        if (vaultReview != null) {
            vaultReviewId = vaultReview.getId();
            comment = vaultReview.getComment();
        }
        this.nextReviewDate = nextReviewDate;
    }

    public void setDepositReviewModels(List<DepositReviewModel> depositReviewModels) {
        this.depositReviewModels = depositReviewModels;
        if (depositReviewModels == null) {
            this.depositReviewModels = new ArrayList<>();
        }
    }
}

