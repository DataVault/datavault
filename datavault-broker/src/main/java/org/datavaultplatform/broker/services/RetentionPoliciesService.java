package org.datavaultplatform.broker.services;

import org.datavaultplatform.broker.controllers.admin.AdminRetentionPoliciesController;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.dao.RetentionPolicyDAO;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RetentionPoliciesService {

    private final Logger logger = LoggerFactory.getLogger(RetentionPoliciesService.class);

    private RetentionPolicyDAO retentionPolicyDAO;
    
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
        return retentionPolicyDAO.findById(policyID);
    }
    
    public void setRetentionPolicyDAO(RetentionPolicyDAO retentionPolicyDAO) {
        this.retentionPolicyDAO = retentionPolicyDAO;
    }

    public void delete(String policyID) {
        retentionPolicyDAO.delete(policyID);
    }

    public RetentionPolicy buildRetentionPolicy(CreateRetentionPolicy createRetentionPolicy) {

        logger.info("Build RetentionPolicy from CreateRetentionPolicy");

        RetentionPolicy retentionPolicy = new RetentionPolicy();

        logger.info("createRetentionPolicy.getId() = " + createRetentionPolicy.getId());

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

        if (createRetentionPolicy.getInEffectDate().isEmpty()) {
            retentionPolicy.setInEffectDate(null);
        } else {
            retentionPolicy.setInEffectDate(stringToDate(createRetentionPolicy.getInEffectDate()));
        }

        if (createRetentionPolicy.getEndDate().isEmpty()) {
            retentionPolicy.setEndDate(null);
        } else {
            retentionPolicy.setEndDate(stringToDate(createRetentionPolicy.getEndDate()));
        }

        if (createRetentionPolicy.getDateGuidanceReviewed().isEmpty()) {
            retentionPolicy.setDataGuidanceReviewed(null);
        } else {
            retentionPolicy.setDataGuidanceReviewed(stringToDate(createRetentionPolicy.getDateGuidanceReviewed()));
        }

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

        crp.setInEffectDate(dateToString(rp.getInEffectDate()));
        crp.setEndDate(dateToString(rp.getEndDate()));
        crp.setDateGuidanceReviewed(dateToString(rp.getDataGuidanceReviewed()));

        return crp;

    }


    private int stringToInt(String string) {
        int i;
        try {i = Integer.parseInt(string);
        } catch (Exception e) {
            return 0;
        }

        return i;
    }


    private String dateToString(Date date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        } else {
            return "";
        }

    }

    private Date stringToDate(String string) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
}

