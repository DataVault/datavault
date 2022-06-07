package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.DepositReviewDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DepositsReviewService {

    private static final Logger logger = LoggerFactory.getLogger(DepositsReviewService.class);

    private final DepositReviewDAO depositReviewDAO;

    @Autowired
    public DepositsReviewService(DepositReviewDAO depositReviewDAO) {
        this.depositReviewDAO = depositReviewDAO;
    }


    public void addDepositReview(DepositReview depositReview) {
        depositReview.setCreationTime(new Date());
        depositReviewDAO.save(depositReview);
    }

    public List<DepositReview> addDepositReviews(Vault vault, VaultReview vaultReview) {
        List <DepositReview> depositReviews = new ArrayList<>();

        for (Deposit deposit : vault.getDeposits()) {
            DepositReview depositReview = new DepositReview();

            depositReview.setVaultReview(vaultReview);
            depositReview.setDeposit(deposit);
            addDepositReview(depositReview);

            depositReviews.add(depositReview);
        }

        return depositReviews;
    }

    public List<DepositReview> getDepositReviews() {
        return depositReviewDAO.list();
    }

    public DepositReview getDepositReview(String depositReviewID) {
        return depositReviewDAO.findById(depositReviewID).orElse(null);
    }

    public List<DepositReview> search(String query) {
        return this.depositReviewDAO.search(query);
    }
    
    public void updateDepositReview(DepositReview depositReview) {
        depositReviewDAO.update(depositReview);
    }
    


    public long count() { return depositReviewDAO.count(); }




}

