package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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

        // pass the view an empty Retention Policy since the form expects it
        model.addAttribute("retentionPolicy", new CreateRetentionPolicy());

        return "admin/retentionpolicies/index";
    }

    @RequestMapping(value = "/admin/retentionpolicies/create", method = RequestMethod.POST)
    public RedirectView addRetentionPoliciesListing(@ModelAttribute CreateRetentionPolicy createRetentionPolicy, ModelMap model) throws Exception {
        String defaultEngine = "org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy";

        restService.addRetentionPolicy(createRetentionPolicy);

        return new RedirectView("/admin/retentionpolicies", true);
    }
    @RequestMapping(value = "/admin/retentionpolicies/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteRetentionPoliciesListing(ModelMap model, @PathVariable("id") String policyId) throws Exception {

        restService.deleteRetentionPolicy(policyId);

        return ResponseEntity.ok().build();
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