package org.datavaultplatform.webapp.model;

import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.VaultReview;


@Slf4j
public class VaultReviewModel {

    private String vaultReviewId;
    private Date actionedDate;
    private Date newReviewDate;
    private String comment;

    private List<DepositReviewModel> depositReviewModels;

    public VaultReviewModel() {
    }

    public VaultReviewModel(VaultReview vaultReview) {
        vaultReviewId = vaultReview.getId();
        actionedDate = vaultReview.getActionedDate();
        newReviewDate = vaultReview.getNewReviewDate();
        comment = vaultReview.getComment();
    }

    public String getVaultReviewId() {
        return vaultReviewId;
    }

    public void setVaultReviewId(String vaultReviewId) {
        this.vaultReviewId = vaultReviewId;
    }

    public Date getActionedDate() {
        return actionedDate;
    }

    public void setActionedDate(Date actionedDate) {
        this.actionedDate = actionedDate;
    }

    public Date getNewReviewDate() {
        return newReviewDate;
    }

    public void setNewReviewDate(Date newReviewDate) {
        this.newReviewDate = newReviewDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<DepositReviewModel> getDepositReviewModels() {
        return depositReviewModels;
    }

    public void setDepositReviewModels(List<DepositReviewModel> depositReviewModels) {
        this.depositReviewModels = depositReviewModels;
    }
}

