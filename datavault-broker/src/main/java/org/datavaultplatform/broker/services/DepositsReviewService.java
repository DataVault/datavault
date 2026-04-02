package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.model.dao.DepositReviewDAO;
import org.datavaultplatform.common.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional
public class DepositsReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(DepositsReviewService.class);

    private final DepositReviewDAO depositReviewDAO;
    private final DepositDAO depositDAO;
    private final Clock clock;

    @Autowired
    public DepositsReviewService(DepositReviewDAO depositReviewDAO, DepositDAO depositDAO, Clock clock) {
        this.depositReviewDAO = depositReviewDAO;
        this.depositDAO = depositDAO;
        this.clock = clock;
    }

    public void saveDepositReview(DepositReview depositReview) {
        depositReview.setCreationTime(LocalDateTime.now(clock));
        depositReviewDAO.save(depositReview);
    }

    public List<DepositReview> addDepositReviews(Vault vault, VaultReview vaultReview) {
        List <DepositReview> result = new ArrayList<>();

        if (vault == null) {
            return result;
        }
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

    public List<DepositReview> refreshDepositReviews(Vault vault, VaultReview vaultReview) {
        Assert.notNull(vault, "The vault cannot be null");
        Assert.hasText(vault.getID(), "The vault must have an id");
        Assert.notNull(vaultReview, "The vault review cannot be null");
        Assert.isTrue(vaultReview.isReviewUnderway(), "The vault review must be underway");
        
        Set<String> existingDepositReviewIds = Utils.getSafeStream(vaultReview.getDepositReviews())
                .map(DepositReview::getDeposit)
                .filter(Objects::nonNull) //extra safe
                .map(Deposit::getID)
                .collect(java.util.stream.Collectors.toSet());

        List<Deposit> fromDbDeposits = depositDAO.getDepositsByVaultId(vault.getID());
        LOG.info("Vault[{}] : number of existing deposits reviews: {}", vault.getName(), existingDepositReviewIds.size());
        LOG.info("Vault[{}] : number of existing deposits: {}", vault.getName(), fromDbDeposits.size());
        List<DepositReview> result = fromDbDeposits.stream()
                .filter(Objects::nonNull)
                .filter(deposit -> !existingDepositReviewIds.contains(deposit.getID()))
                .map(deposit -> getDepositReview(vaultReview, deposit))
                .toList();
        LOG.info("Vault[{}] : number of new deposit reviews: {}", vault.getName(), result.size());

        return result;
    }
    
    private DepositReview getDepositReview(VaultReview vaultReview, Deposit deposit) {
        DepositReview depositReview = new DepositReview();
        depositReview.setDeposit(deposit);
        vaultReview.addDepositReview(depositReview);
        saveDepositReview(depositReview);
        return depositReview;
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

