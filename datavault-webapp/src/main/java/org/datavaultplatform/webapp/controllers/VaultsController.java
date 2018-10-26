package org.datavaultplatform.webapp.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
//@RequestMapping("/vaults")
public class VaultsController {

    private RestService restService;
    private String system;
    private String link;

    public void setSystem(String system) {
        this.system = system;
    }
    public void setLink(String link) {
        this.link = link;
    }

	public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public String getVaultsListing(ModelMap model) {
        model.addAttribute("vaults", restService.getVaultsListing());
        
        // pass the view an empty Vault since the form expects it
        model.addAttribute("vault", new CreateVault());

        Dataset[] datasets = restService.getDatasets();
        model.addAttribute("datasets", datasets);
        
        RetentionPolicy[] policies = restService.getRetentionPolicyListing();
        model.addAttribute("policies", policies);

        Group[] groups = restService.getGroups();
        model.addAttribute("groups", groups);
        
        model.put("system", system);
        model.put("link", link);

        return "vaults/index";
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID) {
        VaultInfo vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);

        model.addAttribute(restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));
        
        DepositInfo[] deposits = restService.getDepositsListing(vaultID);
        model.addAttribute("deposits", deposits);
        
        Map<String, Retrieve[]> depositRetrievals = new HashMap<String, Retrieve[]>();
        for (DepositInfo deposit : deposits) {
            Retrieve[] retrievals = restService.getDepositRetrieves(deposit.getID());
            depositRetrievals.put(deposit.getName(), retrievals);
        }
        model.addAttribute("retrievals", depositRetrievals);
        
        DataManager[] dataManagers = restService.getDataManagers(vaultID);
        model.addAttribute("dataManagers", dataManagers);
        
        return "vaults/vault";
    }

    // Process the completed 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.POST)
    public String addVault(@ModelAttribute CreateVault vault, ModelMap model) {
        VaultInfo newVault = restService.addVault(vault);
        String vaultUrl = "/vaults/" + newVault.getID() + "/";
        return "redirect:" + vaultUrl;        
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/addDataManager", method = RequestMethod.POST)
    public String addDataManager(ModelMap model,
                                @PathVariable("vaultid") String vaultID,
                                @RequestParam("dataManagerUUN") String dataManagerUUN ) {
        VaultInfo vault = restService.addDataManager(vaultID, dataManagerUUN);
        String vaultUrl = "/vaults/" + vault.getID() + "/";
        return "redirect:" + vaultUrl;        
    }
    
    
    @RequestMapping(value = "/vaults/{vaultid}/deleteDataManager", method = RequestMethod.POST)
    public String deleteDataManager(ModelMap model,
                                @PathVariable("vaultid") String vaultID,
                                @RequestParam("dataManagerID") String dataManagerID ) {
        VaultInfo vault = restService.deleteDataManager(vaultID, dataManagerID);
        String vaultUrl = "/vaults/" + vault.getID() + "/";
        return "redirect:" + vaultUrl;        
    }


    @RequestMapping(value = "/vaults/{vaultid}/updateVaultDescription", method = RequestMethod.POST)
    public String updateVaultDescription(ModelMap model,
                                 @PathVariable("vaultid") String vaultID,
                                 @RequestParam("description") String description ) {
        VaultInfo vault = restService.updateVaultDescription(vaultID, description);
        String vaultUrl = "/vaults/" + vault.getID() + "/";
        return "redirect:" + vaultUrl;
    }
}


