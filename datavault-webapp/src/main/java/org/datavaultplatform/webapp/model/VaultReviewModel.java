package org.datavaultplatform.webapp.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.VaultReview;


@Slf4j
public class VaultReviewModel {

    private String vaultReviewId;
    private Date actionedDate;
    private String newReviewDate;
    private String comment;

    private List<DepositReviewModel> depositReviewModels;

    public VaultReviewModel() {
    }

    public VaultReviewModel(VaultReview vaultReview) {
        vaultReviewId = vaultReview.getId();
        actionedDate = vaultReview.getActionedDate();
        newReviewDate = DateToString(vaultReview.getNewReviewDate());
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

    public String getNewReviewDate() {
        return newReviewDate;
    }

    public void setNewReviewDate(String newReviewDate) {
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


    private String DateToString(Date date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        } else {
            return "";
        }

    }

    public Date StringToDate(String string) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(string);
        } catch (ParseException ex) {
            log.error("problem parsing date from {}", string, ex);
        }

        return date;
    }
}

