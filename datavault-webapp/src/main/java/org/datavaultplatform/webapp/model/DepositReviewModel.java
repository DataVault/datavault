package org.datavaultplatform.webapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.response.DepositInfo;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DepositReviewModel {

    // DepositReview Identifier
    private String depositReviewId;
    /*  DEPRECATED */
    @Deprecated
    private boolean toBeDeleted;
    private int deleteStatus;
    private String comment;

    ////// Add in here any fields from the Deposit that we want to display

    private String depositId;
    private String name;
    private String statusName;
    private LocalDateTime creationTime;

    /**
     * This common code was put here to avoid duplication.
     * @param depositReview the deposit review from where to get data from
     * @param depositInfo the deposit info from where to get data from
     */
    public void updateFromDepositReviewAndDepositInfo(DepositReview depositReview, DepositInfo depositInfo) {
        Assert.notNull(depositReview, "The depositReview cannot be null");
        Assert.notNull(depositInfo, "The depositInfo cannot be null");
        // Set DepositReview stuff
        this.setDepositReviewId(depositReview.getId());
        this.setDeleteStatus(depositReview.getDeleteStatus());
        this.setComment(depositReview.getComment());

        // Set Deposit stuff
        this.setDepositId(depositInfo.getID());
        this.setName(depositInfo.getName());
        this.setStatusName(depositInfo.getStatus().name());
        this.setCreationTime(depositInfo.getCreationTime());
    }
}
