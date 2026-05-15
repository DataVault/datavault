package org.datavaultplatform.webapp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.response.DepositInfo;

import java.time.LocalDateTime;
import java.util.Comparator;

// Used in VaultsController for displaying Vault/Deposit Reviews
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DepositReviewViewModel extends DepositReviewModel {

    public static final Comparator<DepositReviewViewModel> BY_DEPOSIT_CREATION_TIME =
            Comparator.nullsFirst(Comparator.comparing(
                    DepositReviewViewModel::getDepositCreationTime,
                    Comparator.nullsFirst(Comparator.naturalOrder())));

    public static final Comparator<DepositReviewViewModel> BY_CREATION_TIME =
            Comparator.nullsFirst(Comparator.comparing(
                    DepositReviewViewModel::getCreationTime,
                    Comparator.nullsFirst(Comparator.naturalOrder())));

    private LocalDateTime actionedDate;
    private LocalDateTime creationTime;

    /**
     * This common code was put here to avoid duplication.
     * @param depositReview the deposit review from where to get data from
     * @param depositInfo the deposit info from where to get data from
     */
    public DepositReviewViewModel(DepositReview depositReview, DepositInfo depositInfo) {
        super.updateFromDepositReviewAndDepositInfo(depositReview, depositInfo);
        this.setActionedDate(depositReview.getActionedDate());
        this.setCreationTime(depositReview.getCreationTime());
    }
}
