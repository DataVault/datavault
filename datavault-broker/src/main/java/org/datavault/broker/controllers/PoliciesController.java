package org.datavault.broker.controllers;

import java.util.List;

import org.datavault.common.model.Policy;
import org.datavault.broker.services.PoliciesService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:29
 */

@RestController
public class PoliciesController {
    
    private PoliciesService policiesService;
    
    public void setPoliciesService(PoliciesService policiesService) {
        this.policiesService = policiesService;
    }
    
    @RequestMapping(value = "/policies", method = RequestMethod.GET)
    public List<Policy> getPolicies() {
        return policiesService.getPolicies();
    }
}
