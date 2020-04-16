package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.dao.DepositReviewDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DepositsReviewService {

    private static final Logger logger = LoggerFactory.getLogger(DepositsReviewService.class);

    private DepositReviewDAO depositReviewDAO;

    public void setDepositReviewDAO(DepositReviewDAO depositReviewDAO) {
        this.depositReviewDAO = depositReviewDAO;
    }


    public void addDepositReview(DepositReview depositReview) {
        depositReviewDAO.save(depositReview);
    }

    public List<DepositReview> getDepositReviews() {
        return depositReviewDAO.list();
    }

    public DepositReview getDepositReview(String depositReviewID) {
        return depositReviewDAO.findById(depositReviewID);
    }

    public List<DepositReview> search(String query) {
        return this.depositReviewDAO.search(query);
    }
    
    public void updateDepositReview(DepositReview depositReview) {
        depositReviewDAO.update(depositReview);
    }
    


    public int count() { return depositReviewDAO.count(); }




}

