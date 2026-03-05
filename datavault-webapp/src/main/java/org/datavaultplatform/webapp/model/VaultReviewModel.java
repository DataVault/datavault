package org.datavaultplatform.webapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private List<DepositReviewModel> depositReviewModels = new ArrayList<>();

    public VaultReviewModel(VaultReview vaultReview, LocalDate nextReviewDate) {
        if (vaultReview != null) {
            vaultReviewId = vaultReview.getId();
            actionedDate = vaultReview.getActionedDate();
            comment = vaultReview.getComment();
        }
        this.nextReviewDate = nextReviewDate;
    }

    public void setDepositReviewModels(List<DepositReviewModel> depositReviewModels) {
        this.depositReviewModels = Objects.requireNonNullElseGet(depositReviewModels, ArrayList::new);
    }
}

