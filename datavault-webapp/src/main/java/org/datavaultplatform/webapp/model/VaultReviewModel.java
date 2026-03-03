package org.datavaultplatform.webapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.VaultReview;


@Slf4j
@Data
@NoArgsConstructor
public class VaultReviewModel {

    private String vaultReviewId;
    private LocalDateTime actionedDate;
    private LocalDate nextReviewDate;
    private String comment;

    private List<DepositReviewModel> depositReviewModels;

    public VaultReviewModel(VaultReview vaultReview, LocalDate nextReviewDate) {
        vaultReviewId = vaultReview.getId();
        actionedDate = vaultReview.getActionedDate();
        this.nextReviewDate = nextReviewDate;
        comment = vaultReview.getComment();
    }
}

