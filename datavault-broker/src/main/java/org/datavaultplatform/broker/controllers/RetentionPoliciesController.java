package org.datavaultplatform.broker.controllers;

import java.util.List;

import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:29
 */

@RestController
public class RetentionPoliciesController {
    
    private RetentionPoliciesService retentionPoliciesService;
    private UsersService usersService;
    
    public void setRetentionPoliciesService(RetentionPoliciesService retentionPoliciesService) {
        this.retentionPoliciesService = retentionPoliciesService;
    }
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
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

        User user = usersService.getUser(userID);
        if (user == null) {
            throw new Exception("User '" + userID + "' does not exist");
        }

        if (!user.isAdmin()) {
            throw new Exception("Access denied");
        }

        retentionPoliciesService.updateRetentionPolicy(policy);
        return new ResponseEntity<>(policy, HttpStatus.OK);
    }
}
