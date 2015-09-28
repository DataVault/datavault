package org.datavaultplatform.webapp.controllers;


import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Restore;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:32
 */

@Controller
public class DepositsController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    // Return an 'create new deposit' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/create", method = RequestMethod.GET)
    public String createDeposit(@ModelAttribute Deposit deposit, ModelMap model, @PathVariable("vaultid") String vaultID) {
        // pass the view an empty Deposit since the form expects it
        model.addAttribute("deposit", new Deposit());
        model.addAttribute("vault", restService.getVault(vaultID));
        return "/deposits/create";
    }

    // Process the completed 'create new deposit' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/create", method = RequestMethod.POST)
    public String addDeposit(@ModelAttribute Deposit deposit, ModelMap model,
                             @PathVariable("vaultid") String vaultID, @RequestParam String action) {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/vaults/" + vaultID;
        }

        Deposit newDeposit = restService.addDeposit(vaultID, deposit);
        String depositUrl = "/vaults/" + vaultID + "/deposits/" + newDeposit.getID() + "/";
        return "redirect:" + depositUrl;
    }

    // View properties of a single deposit
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}", method = RequestMethod.GET)
    public String getDeposit(ModelMap model, @PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) {
        model.addAttribute("vault", restService.getVault(vaultID));
        model.addAttribute("deposit", restService.getDeposit(vaultID, depositID));
        model.addAttribute("manifest", restService.getDepositManifest(vaultID, depositID));
        model.addAttribute("events", restService.getDepositEvents(vaultID, depositID));
        return "deposits/deposit";
    }
    
    // View properties of a single deposit as a JSON object
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/json", method = RequestMethod.GET)
    public @ResponseBody Deposit getDepositJson(@PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) {
        return restService.getDeposit(vaultID, depositID);
    }
    
    // View jobs related to a single deposit as a JSON object
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/jobs", method = RequestMethod.GET)
    public @ResponseBody Job[] getDepositJobsJson(@PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) {
        return restService.getDepositJobs(vaultID, depositID);
    }
    
    // Return a 'restore deposit' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/restore", method = RequestMethod.GET)
    public String restoreDeposit(@ModelAttribute Restore restore, ModelMap model, @PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) {
        model.addAttribute("restore", new Restore());
        model.addAttribute("vault", restService.getVault(vaultID));
        model.addAttribute("deposit", restService.getDeposit(vaultID, depositID));
        return "deposits/restore";
    }
    
    // Process the completed 'restore deposit' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/restore", method = RequestMethod.POST)
    public String processRestore(@ModelAttribute Restore restore, ModelMap model, @PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) {
        
        Boolean result = restService.restoreDeposit(vaultID, depositID, restore);
        
        String depositUrl = "/vaults/" + vaultID + "/deposits/" + depositID + "/";
        return "redirect:" + depositUrl;
    }
}
