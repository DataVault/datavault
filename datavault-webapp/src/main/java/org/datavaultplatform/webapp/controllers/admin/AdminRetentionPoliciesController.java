package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Stuart Lewis
 * Date: 20/09/2015
 */

@Controller
@ConditionalOnBean(RestService.class)
public class AdminRetentionPoliciesController {

    private final Logger logger = LoggerFactory.getLogger(AdminRetentionPoliciesController.class);

    private final RestService restService;

    public AdminRetentionPoliciesController(
        RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/retentionpolicies", method = RequestMethod.GET)
    public String getRetentionPoliciesListing(ModelMap model) throws Exception {

        model.addAttribute("policies", restService.getRetentionPolicyListing());

        return "admin/retentionpolicies/index";
    }

    @RequestMapping(value = "/admin/retentionpolicies/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteRetentionPoliciesListing(ModelMap model, @PathVariable("id") String policyId) throws Exception {

        // todo : Check if it is being used and if so then error.

        restService.deleteRetentionPolicy(policyId);

        return ResponseEntity.ok().build();
    }

    // Return an empty 'add new retention policy' page
    @RequestMapping(value = "/admin/retentionpolicies/add", method = RequestMethod.GET)
    public String addRetentionPolicy(ModelMap model) {
        // pass the view an empty RetentionPolicy since the form expects it
        model.addAttribute("retentionPolicy", new CreateRetentionPolicy());

        return "admin/retentionpolicies/add";
    }

    // Process the completed 'add new retention policy' page
    @RequestMapping(value = "/admin/retentionpolicies/add", method = RequestMethod.POST)
    public String addRetentionPolicy(@ModelAttribute CreateRetentionPolicy createRetentionPolicy, ModelMap model, @RequestParam String action) throws Exception {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        logger.info("Adding new RetentionPolicy with name = " + createRetentionPolicy.getName());

        restService.addRetentionPolicy(createRetentionPolicy);

        return "redirect:/admin/retentionpolicies";
    }

    // Return an 'edit retention policy' page
    @RequestMapping(value = "/admin/retentionpolicies/edit/{retentionpolicyid}", method = RequestMethod.GET)
    public String editRetentionPolicy(ModelMap model, @PathVariable("retentionpolicyid") String retentionPolicyId) throws Exception {

        logger.info("Getting RetentionPolicy with id = " +  retentionPolicyId);

        CreateRetentionPolicy crp = restService.getRetentionPolicy(retentionPolicyId);
        model.addAttribute("retentionPolicy", crp);

        return "/admin/retentionpolicies/edit";
    }

    // Process the completed 'edit retention policy' page
    @RequestMapping(value = "/admin/retentionpolicies/edit/{retentionpolicyid}", method = RequestMethod.POST)
    public String editRetentionPolicy(@ModelAttribute CreateRetentionPolicy createRetentionPolicy, ModelMap model, @PathVariable("retentionpolicyid") String retentionPolicyId, @RequestParam String action) throws Exception {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        logger.info("Editing RetentionPolicy with id and name = " + createRetentionPolicy.getId() + " " + createRetentionPolicy.getName());

        restService.editRetentionPolicy(createRetentionPolicy);

        return "redirect:/admin/retentionpolicies";
    }

}