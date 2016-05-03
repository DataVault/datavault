package org.datavaultplatform.broker.controllers;

import java.util.List;

import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:29
 */

@RestController
public class RetentionPoliciesController {
    
    private RetentionPoliciesService retentionPoliciesService;
    
    public void setRetentionPoliciesService(RetentionPoliciesService retentionPoliciesService) {
        this.retentionPoliciesService = retentionPoliciesService;
    }
    
    @RequestMapping(value = "/retentionpolicies", method = RequestMethod.GET)
    public List<RetentionPolicy> getPolicies(@RequestHeader(value = "X-UserID", required = true) String userID,
                                             @RequestHeader(value = "X-Client-Key", required = true) String clientKey) {
        return retentionPoliciesService.getRetentionPolicies();
    }
    
    @RequestMapping(value = "/retentionpolicies/{policyid}", method = RequestMethod.GET)
    public RetentionPolicy getRetentionPolicy(@RequestHeader(value = "X-UserID", required = true) String userID,
                                              @RequestHeader(value = "X-Client-Key", required = true) String clientKey,
                                              @PathVariable("policyid") String policyID) {
        return retentionPoliciesService.getPolicy(policyID);
    }
}
