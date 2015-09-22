package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Policy;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Stuart Lewis
 * Date: 20/09/2015
 */

@Controller
public class UsersController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    public String getUsersListing(ModelMap model) {
        model.addAttribute("users", restService.getUsers());
        return "admin/users/index";
    }

    /**
    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID) {
        Vault vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);

        model.addAttribute(restService.getPolicy(vault.getPolicyID()));
        model.addAttribute("deposits", restService.getDepositsListing(vaultID));

        return "vaults/vault";
    }

    // Return an empty 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.GET)
    public String createVault(ModelMap model) {

        // pass the view an empty Vault since the form expects it
        model.addAttribute("vault", new Vault());

        Policy[] policies = restService.getPolicyListing();
        Map<String, String> policyMap = new HashMap<>();
        for (Policy policy : policies) {
            policyMap.put(policy.getID(), policy.getName());
        }
        model.addAttribute("policyMap", policyMap);

        return "vaults/create";
    }

    // Process the completed 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.POST)
    public String addVault(@ModelAttribute Vault vault, ModelMap model, @RequestParam String action) {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        Vault newVault = restService.addVault(vault);
        String vaultUrl = "/vaults/" + newVault.getID() + "/";
        return "redirect:" + vaultUrl;        
    }
    */
}


