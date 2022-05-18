package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:29
 */

@RestController
public class RetentionPoliciesController {
    
    private final RetentionPoliciesService retentionPoliciesService;
    private final AdminService adminService;

    public RetentionPoliciesController(RetentionPoliciesService retentionPoliciesService,
        AdminService adminService) {
        this.retentionPoliciesService = retentionPoliciesService;
        this.adminService = adminService;
    }

    @GetMapping("/retentionpolicies")
    public List<RetentionPolicy> getPolicies(@RequestHeader(HEADER_USER_ID) String userID,
                                             @RequestHeader(HEADER_CLIENT_KEY) String clientKey) {
        return retentionPoliciesService.getRetentionPolicies();
    }



}
