package org.datavaultplatform.webapp.controllers;


import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.webapp.security.UsesDepositsPaused;
import org.datavaultplatform.webapp.security.UsesRetrievesPaused;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:32
 */

@Slf4j
@Controller
@ConditionalOnBean(RestService.class)
public class DepositsController {

    private static final String HAS_PERMISSION_TO_RETRIEVE = "(hasPermission(#vaultID, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultID, 'GROUP_VAULT', 'CAN_RETRIEVE_DATA'))            and (hasRole('IS_ADMIN') or (!@permissionsService.retrievesPaused()))";
    private static final String HAS_PERMISSION_TO_DEPOSIT  = "(hasPermission(#vaultID, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultID, 'GROUP_VAULT', 'MANAGE_SCHOOL_VAULT_DEPOSITS')) and (hasRole('IS_ADMIN') or (!@permissionsService.depositsPaused()))";
    private final RestService restService;

    @Autowired
    public DepositsController(RestService restService) {
        this.restService = restService;
    }

    // Return an 'create new deposit' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/create", method = RequestMethod.GET)
    @PreAuthorize("hasPermission(#vaultID, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultID, 'GROUP_VAULT', 'MANAGE_SCHOOL_VAULT_DEPOSITS')")
    public String createDeposit(ModelMap model, @PathVariable("vaultid") String vaultID) throws Exception {
        
        // pass the view an empty Deposit since the form expects it
        CreateDeposit deposit = new CreateDeposit();
        
        // Maybe get this from an API call in future. For now generate a random ID.
        deposit.setFileUploadHandle(UUID.randomUUID().toString());
        
        model.addAttribute("deposit", deposit);
        model.addAttribute("vault", restService.getVault(vaultID));
        return "deposits/create";
    }

    // Process the completed 'create new deposit' page
    // templates/deposits/create.html
    @UsesDepositsPaused
    @PostMapping("/vaults/{vaultid}/deposits/create")
    @PreAuthorize(HAS_PERMISSION_TO_DEPOSIT)
    public String createAndAddDeposit(@ModelAttribute CreateDeposit deposit,
                                      @PathVariable("vaultid") String vaultID) {
        
        // Set the Vault ID for the new deposit
        deposit.setVaultID(vaultID);
        
        // Remove personal statement if has personalstatement is false
        if("No".equalsIgnoreCase(deposit.getHasPersonalData())) {
            deposit.setPersonalDataStatement("");
        }
        
        DepositInfo newDeposit = restService.addDeposit(deposit);
        String depositUrl = "/vaults/" + vaultID + "/deposits/" + newDeposit.getID() + "/";
        return "redirect:" + depositUrl;
    }

    // View properties of a single deposit
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}", method = RequestMethod.GET)
    @PreAuthorize("hasPermission(#vaultID, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultID, 'GROUP_VAULT', 'CAN_RETRIEVE_DATA') or hasPermission(#vaultID,'GROUP_VAULT','CAN_MANAGE_DEPOSITS')")
    public String getDeposit(ModelMap model, @PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) throws Exception {
        model.addAttribute("vault", restService.getVault(vaultID));
        model.addAttribute("deposit", restService.getDeposit(depositID));
        model.addAttribute("events", restService.getDepositEvents(depositID));
        model.addAttribute("retrieves", restService.getDepositRetrieves(depositID));
        
        return "deposits/deposit";
    }
    
    // View properties of a single deposit as a JSON object
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/json", method = RequestMethod.GET)
    public @ResponseBody DepositInfo getDepositJson(@PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) throws Exception {
        return restService.getDeposit(depositID);
    }
    
    // View jobs related to a single deposit as a JSON object
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/jobs", method = RequestMethod.GET)
    public @ResponseBody Job[] getDepositJobsJson(@PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) throws Exception {
        return restService.getDepositJobs(depositID);
    }
    
    // Return a 'retrieve deposit' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/retrieve", method = RequestMethod.GET)
    @PreAuthorize("hasPermission(#vaultID, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultID, 'GROUP_VAULT', 'CAN_RETRIEVE_DATA')")
    public String retrieveDeposit(@ModelAttribute Retrieve retrieve, ModelMap model, @PathVariable("vaultid") String vaultID, @PathVariable("depositid") String depositID) throws Exception {
        model.addAttribute("retrieve", new Retrieve());
        model.addAttribute("vault", restService.getVault(vaultID));
        model.addAttribute("deposit", restService.getDeposit(depositID));
        return "deposits/retrieve";
    }
    
    // Process the completed 'retrieve deposit' page
    // templates/deposits/retrieve.html
    @UsesRetrievesPaused
    @PostMapping("/vaults/{vaultid}/deposits/{depositid}/retrieve")
    @PreAuthorize(HAS_PERMISSION_TO_RETRIEVE)
    public String processRetrieve(@ModelAttribute Retrieve retrieve,
                                 @PathVariable("vaultid") String vaultID,
                                 @PathVariable("depositid") String depositID
    ) {

        Boolean result = restService.retrieveDeposit(depositID, retrieve);
        if (Boolean.TRUE != result) {
            log.warn("Failed to retrieve deposit depositID[{}]", depositID);
        }

        String depositUrl = "/vaults/" + vaultID + "/deposits/" + depositID + "/";
        return "redirect:" + depositUrl;
    }


    // Process the completed 'restart retrieve' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositId}/retrieves/{retrieveId}/restart", method = RequestMethod.GET)
    public String restartRetrieve(ModelMap model,
                                 @PathVariable("vaultid") String vaultID, @PathVariable("depositId") String depositID,
                                 @PathVariable("retrieveId") String retrieveID) throws Exception {

        restService.restartRetrieve(depositID, retrieveID);

        String depositUrl = "/vaults/" + vaultID + "/deposits/" + depositID + "/";
        return "redirect:" + depositUrl;
    }

    // Process the completed 'restart deposit' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositId}/restart", method = RequestMethod.GET)
    public String restartDeposit(ModelMap model,
                                  @PathVariable("vaultid") String vaultID, @PathVariable("depositId") String depositID) throws Exception {

        restService.restartDeposit(depositID);

        String depositUrl = "/vaults/" + vaultID + "/deposits/" + depositID + "/";
        return "redirect:" + depositUrl;
    }
}
