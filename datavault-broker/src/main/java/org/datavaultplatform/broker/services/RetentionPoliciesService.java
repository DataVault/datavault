package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.RetentionPolicyDAO;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

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

        retentionPolicy.setDataGuidanceReviewed(createRetentionPolicy.getDateGuidanceReviewed());

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
        crp.setDateGuidanceReviewed(rp.getDataGuidanceReviewed());

        return crp;

    }

    /**
    public int run(Vault v) {
        RetentionPolicy rp = v.getRetentionPolicy();

        if (rp.getMinRetentionPeriod() > 0) {
            Date now = new Date();
            Date check = getReviewDate(v);

            // Is it time for review?
            if (check.before(now)) {
                v.setRetentionPolicyStatus(RetentionPolicyStatus.REVIEW);
                return RetentionPolicyStatus.REVIEW;
            }
        }

        v.setRetentionPolicyStatus(RetentionPolicyStatus.OK);
        return RetentionPolicyStatus.OK;

    }

    public Date getReviewDate(Vault v) {

        logger.info("Calculating Expiry Date for vault " + v.getName());

        if (v.getGrantEndDate() == null) {
            logger.info("No grant date entered for vault " + v.getName());
            return v.getCreationTime();
        }

        Date check = v.getGrantEndDate();

        logger.info("Start date is " + check);

        RetentionPolicy rp = v.getRetentionPolicy();

        if (rp.isExtendUponRetrieval()) {
            // At the time of writing this means its EPSRC

            // Get all the retrieve events
            ArrayList<Retrieve> retrieves = new ArrayList<Retrieve>();
            for (Deposit d : v.getDeposits()) {
                retrieves.addAll(d.getRetrieves());
            }

            // Have their been any retrieves?
            if (!retrieves.isEmpty()) {
                check = retrieves.get(0).getTimestamp();
                for (Retrieve r : retrieves) {
                    if (r.getTimestamp().after(check)) {
                        check = r.getTimestamp();
                    }
                }
            }
        }

        Calendar c = Calendar.getInstance();
        c.setTime(check);
        c.add(Calendar.YEAR, rp.getMinRetentionPeriod());
        check = c.getTime();

        logger.info("Expiry date is " + check);

        return check;
    }
     **/

    public static void setRetention(Vault v) {

        RetentionPolicy rp = v.getRetentionPolicy();

        Date check;
        if (v.getGrantEndDate() == null) {
            check = v.getCreationTime();
        } else {
            check = v.getGrantEndDate();

            if (rp.getMinRetentionPeriod() > 0 && rp.isExtendUponRetrieval()) {
                // At the time of writing this means its EPSRC

                // Get all the retrieve events
                ArrayList<Retrieve> retrieves = new ArrayList<>();
                for (Deposit d : v.getDeposits()) {
                    retrieves.addAll(d.getRetrieves());
                }

                // Have their been any retrieves?
                if (!retrieves.isEmpty()) {
                    check = retrieves.get(0).getTimestamp();
                    for (Retrieve r : retrieves) {
                        if (r.getTimestamp().after(check)) {
                            check = r.getTimestamp();
                        }
                    }
                }
            }

            // Add on the minimum retention period (a number of years)
            Calendar c = Calendar.getInstance();
            c.setTime(check);
            c.add(Calendar.YEAR, rp.getMinRetentionPeriod());
            check = c.getTime();

        }

        v.setRetentionPolicyExpiry(check);

        // Is it time for review?
        Date now = new Date();
        if (check.before(now)) {
            v.setRetentionPolicyStatus(RetentionPolicyStatus.REVIEW);
        } else {
            v.setRetentionPolicyStatus(RetentionPolicyStatus.OK);
        }

        // Record when we checked it
        v.setRetentionPolicyLastChecked(new Date());

    }
}

