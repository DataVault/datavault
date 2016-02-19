package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Policy;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Tom Higgins
 * Date: 27/05/2015
 * Time: 14:44
 */

@Controller
//@RequestMapping("/vaults")
public class VaultsController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getVaultsListing(ModelMap model) {
        model.addAttribute("vaults", restService.getVaultsListing());
        return "vaults/index";
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID) {
        VaultInfo vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);

        model.addAttribute(restService.getPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));
        model.addAttribute("deposits", restService.getDepositsListing(vaultID));

        return "vaults/vault";
    }

    // Return an empty 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.GET)
    public String createVault(ModelMap model) {

        // pass the view an empty Vault since the form expects it
        model.addAttribute("vault", new CreateVault());

        Dataset[] datasets = restService.getDatasets();
        model.addAttribute("datasets", datasets);
        
        Policy[] policies = restService.getPolicyListing();
        model.addAttribute("policies", policies);

        Group[] groups = restService.getGroups();
        model.addAttribute("groups", groups);

        return "vaults/create";
    }

    // Process the completed 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.POST)
    public String addVault(@ModelAttribute CreateVault vault, ModelMap model, @RequestParam String action) {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        VaultInfo newVault = restService.addVault(vault);
        String vaultUrl = "/vaults/" + newVault.getID() + "/";
        return "redirect:" + vaultUrl;        
    }
}


