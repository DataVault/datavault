package org.datavaultplatform.webapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.response.DepositInfo;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Comparator;

// Used in AdminReviewsController when editing the latest Vault/Deposit Review that is underway
@Data
@NoArgsConstructor
public class DepositReviewModel {

    public static final Comparator<DepositReviewModel> BY_DEPOSIT_CREATION_TIME =
            Comparator.nullsFirst(Comparator.comparing(
                    DepositReviewModel::getDepositCreationTime,
                    Comparator.nullsFirst(Comparator.naturalOrder())));

    // DepositReview Identifier
    private String depositReviewId;
    private int deleteStatus;
    private String comment;

    // Add in here any fields from the Deposit that we want to display

    private String depositId;
    private String depositName;
    private String depositStatusName;
    private LocalDateTime depositCreationTime;

    /**
     * This common code was put here to avoid duplication.
     * @param depositReview the deposit review from where to get data from
     * @param depositInfo the deposit info from where to get data from
     */
    public void updateFromDepositReviewAndDepositInfo(DepositReview depositReview, DepositInfo depositInfo) {
        Assert.notNull(depositReview, "The depositReview cannot be null");
        Assert.notNull(depositInfo, "The depositInfo cannot be null");
        Assert.notNull(depositReview.getId(), "The depositReview Id cannot be null");

        // Set DepositReview stuff
        this.setDepositReviewId(depositReview.getId());
        this.setDeleteStatus(depositReview.getDeleteStatus());
        this.setComment(depositReview.getComment());

        // Set Deposit stuff
        this.setDepositId(depositInfo.getID());
        this.setDepositName(depositInfo.getName());
        Deposit.Status status = depositInfo.getStatus();
        this.setDepositStatusName(status == null ? "" : status.name());
        this.setDepositCreationTime(depositInfo.getCreationTime());
    }
}
