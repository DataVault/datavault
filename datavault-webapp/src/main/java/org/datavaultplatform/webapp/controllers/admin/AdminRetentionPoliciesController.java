package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Stuart Lewis
 * Date: 20/09/2015
 */

@Controller
public class AdminRetentionPoliciesController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/retentionpolicies", method = RequestMethod.GET)
    public String getRetentionPoliciesListing(ModelMap model) throws Exception {
        model.addAttribute("policies", restService.getRetentionPolicyListing());

        return "admin/retentionpolicies/index";
    }

    // Process the 'update policy name' Ajax request
    @RequestMapping(value = "/admin/retentionpolicies/{id}/update", method = RequestMethod.POST)
    public @ResponseBody
    void updateRetentionPolicy(ModelMap model,
                                      @PathVariable("id") String policyId,
                                      @RequestParam(name = "name", required = false) String name,
                                      @RequestParam(name = "description", required = false) String description) throws Exception {
        System.out.println("Calling updateRetentionPolicyName");
        RetentionPolicy policy = restService.getRetentionPolicy(policyId);
        if(name != null) {
            System.out.println("Update Name");
            policy.setName(name);
        }
        if(description != null){
            System.out.println("Update Description");
            policy.setDescription(description);
        }
        restService.updateRententionPolicy(policy);
    }
}