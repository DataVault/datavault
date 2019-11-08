package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:29
 */

@RestController
public class RetentionPoliciesController {
    
    private RetentionPoliciesService retentionPoliciesService;
    private AdminService adminService;
    
    public void setRetentionPoliciesService(RetentionPoliciesService retentionPoliciesService) {
        this.retentionPoliciesService = retentionPoliciesService;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
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

    @RequestMapping(value = "/retentionpolicies/update", method = RequestMethod.POST)
    public ResponseEntity<Object> updateRetentionPolicy(@RequestHeader(value = "X-UserID", required = true) String userID,
                                              @RequestBody RetentionPolicy policy) throws Exception {

        User user = adminService.ensureAdminUser(userID);

        retentionPoliciesService.updateRetentionPolicy(policy);
        return new ResponseEntity<>(policy, HttpStatus.OK);
    }

    @RequestMapping(value = "/retentionpolicies/delete/{id}", method = RequestMethod.DELETE)
    public void deleteRetentionPolicy(@RequestHeader(value = "X-UserID", required = true) String userID,
                                                        @PathVariable("id") String policyID) throws Exception {

        User user = adminService.ensureAdminUser(userID);

        retentionPoliciesService.delete(policyID);
    }

    @RequestMapping(value = "/retentionpolicies/add", method = RequestMethod.PUT)
    public ResponseEntity<Object> createRetentionPolicy(@RequestHeader(value = "X-UserID", required = true) String userID,
                                              @RequestBody CreateRetentionPolicy createPolicy) throws Exception {

        User user = adminService.ensureAdminUser(userID);

        RetentionPolicy policy = new RetentionPolicy();

        if(createPolicy.getName() == null || createPolicy.getName().isEmpty()) throw new Exception("RetentionPolicy name missing.");
        policy.setName(createPolicy.getName());

        if(createPolicy.getDescription() != null) {
            policy.setDescription(createPolicy.getDescription());
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if(createPolicy.getDateGuidanceReviewed() != null && !createPolicy.getDateGuidanceReviewed().isEmpty()) {
            policy.setDataGuidanceReviewed(formatter.parse(createPolicy.getDateGuidanceReviewed()));
        }
        if(createPolicy.getEndDate() != null && !createPolicy.getEndDate().isEmpty()) {
            policy.setEndDate(formatter.parse(createPolicy.getEndDate()));
        }
        if(createPolicy.getInEffectDate() != null && !createPolicy.getInEffectDate().isEmpty()) {
            policy.setInEffectDate(formatter.parse(createPolicy.getInEffectDate()));
        }

        if(createPolicy.getUrl() != null && !createPolicy.getUrl().isEmpty()) {
            policy.setUrl(createPolicy.getUrl());
        }

        if(createPolicy.getSort() == null || createPolicy.getSort().isEmpty()) throw new Exception("RetentionPolicy sort missing.");
        policy.setSort(Integer.parseInt(createPolicy.getSort()));

        if(createPolicy.getEngine() == null || createPolicy.getEngine().isEmpty()) throw new Exception("RetentionPolicy engine missing.");
        policy.setEngine(createPolicy.getEngine());


        retentionPoliciesService.addRetentionPolicy(policy);

        return new ResponseEntity<>(policy, HttpStatus.OK);
    }
}
