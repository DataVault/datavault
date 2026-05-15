package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
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
public class AdminRetentionPoliciesController implements AdminRetentionPoliciesControllerApi {

    private final Logger logger = LoggerFactory.getLogger(AdminRetentionPoliciesController.class);

    private final RestService restService;

    public AdminRetentionPoliciesController(
        RestService restService) {
        this.restService = restService;
    }

    @Override
    @GetMapping(value = "/admin/retentionpolicies", produces = MediaType.TEXT_HTML_VALUE)
    public String getRetentionPoliciesListing(ModelMap model) {

        model.addAttribute("policies", restService.getRetentionPolicyListing());

        return "admin/retentionpolicies/index";
    }

    @Override
    @DeleteMapping(value = "/admin/retentionpolicies/delete/{policyId}")
    public ResponseEntity<Void> deleteRetentionPoliciesListing(ModelMap model, @PathVariable String policyId) throws Exception {

        // todo : Check if it is being used and if so then error.

        restService.deleteRetentionPolicy(policyId);

        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping(value = "/admin/retentionpolicies/add", produces = MediaType.TEXT_HTML_VALUE)
    public String addRetentionPolicyPage(ModelMap model) {
        // pass the view an empty RetentionPolicy since the form expects it
        model.addAttribute("retentionPolicy", new CreateRetentionPolicy());

        return "admin/retentionpolicies/add";
    }

    // Process the completed 'add new retention policy' page
    @Override
    @PostMapping(value = "/admin/retentionpolicies/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
    @Override
    @GetMapping(value = "/admin/retentionpolicies/edit/{retentionPolicyId}", produces = MediaType.TEXT_HTML_VALUE)
    public String editRetentionPolicyPage(ModelMap model, @PathVariable String retentionPolicyId) throws Exception {

        logger.info("Getting RetentionPolicy with id = " +  retentionPolicyId);

        CreateRetentionPolicy crp = restService.getRetentionPolicy(retentionPolicyId);
        model.addAttribute("retentionPolicy", crp);

        return "/admin/retentionpolicies/edit";
    }

    // Process the completed 'edit retention policy' page
    @Override
    @PostMapping(value = "/admin/retentionpolicies/edit/{retentionPolicyId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String editRetentionPolicy(@ModelAttribute CreateRetentionPolicy createRetentionPolicy, ModelMap model, @PathVariable String retentionPolicyId, @RequestParam String action) throws Exception {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }
        String formId = String.valueOf(createRetentionPolicy.getId());
        if (!formId.equals(retentionPolicyId)) {
            String msg = String.format("retentionPolicyId mismatch URL[%s] form[%s]", retentionPolicyId, formId);
            throw new IllegalArgumentException(msg);
        }

        logger.info("Editing RetentionPolicy with id and name = " + createRetentionPolicy.getId() + " " + createRetentionPolicy.getName());

        restService.editRetentionPolicy(createRetentionPolicy);

        return "redirect:/admin/retentionpolicies";
    }

}