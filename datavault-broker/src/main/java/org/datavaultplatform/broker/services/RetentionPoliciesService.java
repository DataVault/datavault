package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.RetentionPolicyDAO;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class RetentionPoliciesService {

    private final Logger logger = LoggerFactory.getLogger(RetentionPoliciesService.class);

    private final RetentionPolicyDAO retentionPolicyDAO;

    @Autowired
    public RetentionPoliciesService(RetentionPolicyDAO retentionPolicyDAO) {
        this.retentionPolicyDAO = retentionPolicyDAO;
    }

    public List<RetentionPolicy> getRetentionPolicies() {
        return retentionPolicyDAO.list();
    }
    
    public void addRetentionPolicy(RetentionPolicy retentionPolicy) {
        retentionPolicyDAO.save(retentionPolicy);
    }
    
    public void updateRetentionPolicy(RetentionPolicy retentionPolicy) {
        retentionPolicyDAO.update(retentionPolicy);
    }
    
    public RetentionPolicy getPolicy(String policyID) {
        return retentionPolicyDAO.findById(Integer.parseInt(policyID)).orElse(null);
    }

    public void delete(String policyID) {
        retentionPolicyDAO.deleteById(Integer.parseInt(policyID));
    }

    public RetentionPolicy buildRetentionPolicy(CreateRetentionPolicy createRetentionPolicy) {

        logger.info("Build RetentionPolicy from CreateRetentionPolicy");

        RetentionPolicy retentionPolicy = new RetentionPolicy();

        retentionPolicy.setId(createRetentionPolicy.getId());
        retentionPolicy.setName(createRetentionPolicy.getName());
        retentionPolicy.setDescription(createRetentionPolicy.getDescription());
        retentionPolicy.setUrl(createRetentionPolicy.getUrl());
        retentionPolicy.setMinRetentionPeriod(createRetentionPolicy.getMinRetentionPeriod());
        retentionPolicy.setExtendUponRetrieval(createRetentionPolicy.isExtendUponRetrieval());

        // Engine is deprecated so set it to blank until we remove it from the database
        retentionPolicy.setEngine("");
        // Sort is deprecated so set it to zero until we remove it from the database
        retentionPolicy.setSort(0);
        // MinDataRetentionPeriod is deprecated so et it to blank until we remove it from the database
        retentionPolicy.setMinDataRetentionPeriod("");

        retentionPolicy.setInEffectDate(createRetentionPolicy.getInEffectDate());

        retentionPolicy.setEndDate(createRetentionPolicy.getEndDate());

        retentionPolicy.setDataGuidanceReviewed(createRetentionPolicy.getDataGuidanceReviewed());

        return retentionPolicy;
    }

    public CreateRetentionPolicy buildCreateRetentionPolicy(RetentionPolicy rp) {

        logger.info("Build CreateRetentionPolicy from RetentionPolicy");

        CreateRetentionPolicy crp = new CreateRetentionPolicy();

        crp.setId(rp.getID());
        crp.setName(rp.getName());
        crp.setDescription(rp.getDescription());
        crp.setUrl(rp.getUrl());
        crp.setMinRetentionPeriod(rp.getMinRetentionPeriod());
        crp.setExtendUponRetrieval(rp.isExtendUponRetrieval());

        crp.setInEffectDate(rp.getInEffectDate());
        crp.setEndDate(rp.getEndDate());
        crp.setDataGuidanceReviewed(rp.getDataGuidanceReviewed());

        return crp;

    }

    /**
     * Called from VaultsService and 
     */
    public static void updateRetentionPolicyExpiryDate(Vault vault, Clock clock) {
        
        final LocalDateTime retentionPolicyExpiryLocalDateTime;
        
        RetentionPolicy retentionPolicy = vault.getRetentionPolicy();
        if (vault.getGrantEndDate() == null) {
            retentionPolicyExpiryLocalDateTime = vault.getCreationTime();
        } else {
            LocalDate baseRetentionPolicyExpiryLocalDate = getBaseRetentionPolicyExpiryDate(vault, retentionPolicy);
            int retentionPolicyMinPeriod = retentionPolicy.getMinRetentionPeriod();
            // Add on the minimum retention period (a number of years)
            LocalDate retentionPolicyExpiryLocalDate = DateTimeUtils.getLocalDateAdjustedByYears(baseRetentionPolicyExpiryLocalDate, retentionPolicyMinPeriod);
            retentionPolicyExpiryLocalDateTime = DateTimeUtils.toLocalDateTimeAtNoon(retentionPolicyExpiryLocalDate);
        }

        vault.setRetentionPolicyExpiry(retentionPolicyExpiryLocalDateTime);

        int retentionPolicyStatus = getRetentionPolicyStatus(clock, retentionPolicyExpiryLocalDateTime);
        vault.setRetentionPolicyStatus(retentionPolicyStatus);
        
        vault.setRetentionPolicyLastChecked(LocalDateTime.now(clock));
    }
    
    private static int getRetentionPolicyStatus(Clock clock, LocalDateTime retentionPolicyExpiryDate) {
        // Is it time for review? - to 'date arithmetic' using LocalDate - not Date.
        LocalDate today = LocalDate.now(clock);
        if (DateTimeUtils.toLocalDate(retentionPolicyExpiryDate).isBefore(today)) {
            return RetentionPolicyStatus.REVIEW;
        } else {
            return RetentionPolicyStatus.OK;
        }
    }

    private static LocalDate getBaseRetentionPolicyExpiryDate(Vault vault, RetentionPolicy retentionPolicy) {
        Assert.notNull(vault, "The vault cannot be null");
        Assert.notNull(retentionPolicy, "The retention policy cannot be null");

        LocalDate grantEndDate = vault.getGrantEndDate();

        final LocalDate result;
        if (retentionPolicy.getMinRetentionPeriod() > 0 && retentionPolicy.isExtendUponRetrieval()) {
            // At the time of writing this means its EPSRC

            // Get all the retrieve events
            // find the latest Timestamp and use that for the base
            List<Retrieve> allRetrieves = getAllRetrieves(vault);

            if (allRetrieves.isEmpty()) {
                result = grantEndDate;
            } else {
                // we know that allRetrieves has min length 1
                // Have there been any retrieves?
                // if so - set the retentionPeriodExpiryDate to be the max timestamp
                // NOTE: We do not consider the status of these retrieves
                LocalDateTime maxTimestamp = getMaxTimestamp(allRetrieves).orElseThrow();
                result = maxTimestamp.toLocalDate();
            }
        } else {
            result = grantEndDate;
        }
        return result;
    }

    private static Optional<LocalDateTime> getMaxTimestamp(List<Retrieve> retrieves) {
        Assert.isTrue(!retrieves.isEmpty(), "the retrieves cannot be empty");
        return retrieves
                .stream()
                .map(Retrieve::getTimestamp)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
    }

    private static List<Retrieve> getAllRetrieves(Vault vault) {
        List<Deposit> deposits = vault.getDeposits();
        if (deposits == null) {
            return List.of();
        }
        return deposits.stream()
                .filter(Objects::nonNull)   // Ignore null Deposits
                .map(Deposit::getRetrieves)// we now have stream of List<Retrieve>
                .filter(Objects::nonNull)   // filter out null List<Retrieve>
                .flatMap(List::stream)      // Flatten Stream<List<Retrieve>> into Stream<Retrieve>
                .filter(Objects::nonNull)   // Filter out null Retrieve elements from the stream
                .toList();
    }
}
