package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.User;
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
    public String getRetentionPoliciesListing(ModelMap model) {
        model.addAttribute("policies", restService.getRetentionPolicyListing());

        return "admin/retentionpolicies/index";
    }
}


