package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Object> updateGroup(@RequestHeader(value = "X-UserID", required = true) String userID,
                                              @RequestBody RetentionPolicy policy) throws Exception {

        User user = adminService.ensureAdminUser(userID);

        retentionPoliciesService.updateRetentionPolicy(policy);
        return new ResponseEntity<>(policy, HttpStatus.OK);
    }
}
