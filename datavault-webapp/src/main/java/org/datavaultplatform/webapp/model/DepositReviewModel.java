package org.datavaultplatform.webapp.model;

import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.response.DepositInfo;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

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

    public String getDepositReviewId() {
        return depositReviewId;
    }

    public void setDepositReviewId(String depositReviewId) {
        this.depositReviewId = depositReviewId;
    }

    public boolean isToBeDeleted() {
        return toBeDeleted;
    }

    public void setToBeDeleted(boolean toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }

    public int getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(int deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDepositId() {
        return depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * This common code was put here to avoid duplication.
     * @param depositReview
     * @param depositInfo
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
