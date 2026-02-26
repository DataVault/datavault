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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepositsReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(DepositsReviewService.class);

    private final DepositReviewDAO depositReviewDAO;
    private final Clock clock;

    @Autowired
    public DepositsReviewService(DepositReviewDAO depositReviewDAO, Clock clock) {
        this.depositReviewDAO = depositReviewDAO;
        this.clock = clock;
    }

    public void saveDepositReview(DepositReview depositReview) {
        depositReview.setCreationTime(LocalDateTime.now(clock));
        depositReviewDAO.save(depositReview);
    }

    public List<DepositReview> addDepositReviews(Vault vault, VaultReview vaultReview) {
        List <DepositReview> result = new ArrayList<>();

        for (Deposit deposit : vault.getDeposits()) {
            if (deposit == null) {
                continue;
            }
            DepositReview depositReview = new DepositReview();

            depositReview.setVaultReview(vaultReview);
            depositReview.setDeposit(deposit);
            saveDepositReview(depositReview);

            result.add(depositReview);
        }

        return result;
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

