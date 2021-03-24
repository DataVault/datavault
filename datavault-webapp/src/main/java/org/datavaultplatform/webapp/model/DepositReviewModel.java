package org.datavaultplatform.webapp.model;

import java.util.Date;

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
    private Date creationTime;

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

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
