package org.datavaultplatform.webapp.model;

import org.datavaultplatform.common.model.VaultReview;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class VaultReviewModel {

    private String newReviewDate;
    private String comment;

    public VaultReviewModel() {
    }

    public VaultReviewModel(VaultReview vaultReview) {
        newReviewDate = DateToString(vaultReview.getNewReviewDate());
        comment = vaultReview.getComment();
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
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
}

