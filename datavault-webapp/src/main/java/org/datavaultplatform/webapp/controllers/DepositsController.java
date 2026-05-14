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
import org.springframework.http.MediaType;
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
public class DepositsController implements DepositsControllerApi {

    private static final String HAS_PERMISSION_TO_RETRIEVE = "(hasPermission(#vaultId, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultId, 'GROUP_VAULT', 'CAN_RETRIEVE_DATA'))            and (hasRole('IS_ADMIN') or (!@permissionsService.retrievesPaused()))";
    private static final String HAS_PERMISSION_TO_DEPOSIT  = "(hasPermission(#vaultId, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultId, 'GROUP_VAULT', 'MANAGE_SCHOOL_VAULT_DEPOSITS')) and (hasRole('IS_ADMIN') or (!@permissionsService.depositsPaused()))";
    private final RestService restService;

    @Autowired
    public DepositsController(RestService restService) {
        this.restService = restService;
    }

    // Return an 'create new deposit' page
    @Override
    @GetMapping(value = "/vaults/{vaultId}/deposits/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasPermission(#vaultId, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultId, 'GROUP_VAULT', 'MANAGE_SCHOOL_VAULT_DEPOSITS')")
    public String createDeposit(ModelMap model, @PathVariable String vaultId) throws Exception {
        
        // pass the view an empty Deposit since the form expects it
        CreateDeposit deposit = new CreateDeposit();
        
        // Maybe get this from an API call in future. For now generate a random ID.
        deposit.setFileUploadHandle(UUID.randomUUID().toString());
        
        model.addAttribute("deposit", deposit);
        model.addAttribute("vault", restService.getVault(vaultId));
        return "deposits/create";
    }

    // Process the completed 'create new deposit' page
    // templates/deposits/create.html
    @Override
    @UsesDepositsPaused
    @PostMapping(value = "/vaults/{vaultId}/deposits/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize(HAS_PERMISSION_TO_DEPOSIT)
    public String createAndAddDeposit(@ModelAttribute CreateDeposit deposit,
                                      @PathVariable String vaultId) {

        // Set the Vault ID for the new deposit
        deposit.setVaultID(vaultId);
        
        // Remove personal statement if has personalstatement is false
        if("No".equalsIgnoreCase(deposit.getHasPersonalData())) {
            deposit.setPersonalDataStatement("");
        }
        
        DepositInfo newDeposit = restService.addDeposit(deposit);
        return  getDepositRedirectUrl(vaultId, newDeposit.getID());
    }

    // View properties of a single deposit
    @Override
    @GetMapping(value = "/vaults/{vaultId}/deposits/{depositId}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasPermission(#vaultId, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultId, 'GROUP_VAULT', 'CAN_RETRIEVE_DATA') or hasPermission(#vaultId,'GROUP_VAULT','CAN_MANAGE_DEPOSITS')")
    public String getDeposit(ModelMap model,
                             @PathVariable String vaultId,
                             @PathVariable String depositId) throws Exception {
        model.addAttribute("vault", restService.getVault(vaultId));
        model.addAttribute("deposit", restService.getDeposit(depositId));
        model.addAttribute("events", restService.getDepositEvents(depositId));
        model.addAttribute("retrieves", restService.getDepositRetrieves(depositId));
        
        return "deposits/deposit";
    }
    
    // View properties of a single deposit as a JSON object
    @Override
    @GetMapping(value = "/vaults/{vaultId}/deposits/{depositId}/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DepositInfo getDepositJson(
            @PathVariable String vaultId,
            @PathVariable String depositId) throws Exception {
        return restService.getDeposit(depositId);
    }
    
    // View jobs related to a single deposit as a JSON object
    @Override
    @GetMapping(value = "/vaults/{vaultId}/deposits/{depositId}/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Job[] getDepositJobsJson(@PathVariable String vaultId, @PathVariable String depositId) throws Exception {
        return restService.getDepositJobs(depositId);
    }
    
    // Return a 'retrieve deposit' page
    @Override
    @GetMapping(value = "/vaults/{vaultId}/deposits/{depositId}/retrieve", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasPermission(#vaultId, 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission(#vaultId, 'GROUP_VAULT', 'CAN_RETRIEVE_DATA')")
    public String retrieveDeposit(
            @ModelAttribute Retrieve retrieve,
            ModelMap model,
            @PathVariable String vaultId,
            @PathVariable String depositId) throws Exception {
        model.addAttribute("retrieve", new Retrieve());
        model.addAttribute("vault", restService.getVault(vaultId));
        model.addAttribute("deposit", restService.getDeposit(depositId));
        return "deposits/retrieve";
    }
    
    // Process the completed 'retrieve deposit' page
    // templates/deposits/retrieve.html
    @Override
    @UsesRetrievesPaused
    @PostMapping(value = "/vaults/{vaultId}/deposits/{depositId}/retrieve", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize(HAS_PERMISSION_TO_RETRIEVE)
    public String processRetrieve(@ModelAttribute Retrieve retrieve,
                                  @PathVariable String vaultId,
                                  @PathVariable String depositId
    ) {

        Boolean result = restService.retrieveDeposit(depositId, retrieve);
        if (Boolean.TRUE != result) {
            log.warn("Failed to retrieve deposit depositID[{}]", depositId);
        }

        return getDepositRedirectUrl(vaultId, depositId);
    }


    // Process the completed 'restart retrieve' page
    @Override
    @GetMapping(value = "/vaults/{vaultId}/deposits/{depositId}/retrieves/{retrieveId}/restart")
    public String restartRetrieve(ModelMap model,
                                  @PathVariable String vaultId,
                                  @PathVariable String depositId,
                                  @PathVariable String retrieveId) throws Exception {

        restService.restartRetrieve(depositId, retrieveId);

        return getDepositRedirectUrl(vaultId, depositId);
    }

    // Process the completed 'restart deposit' page
    @Override
    @GetMapping(value = "/vaults/{vaultId}/deposits/{depositId}/restart")
    public String restartDeposit(ModelMap model,
                                 @PathVariable String vaultId,
                                 @PathVariable String depositId) throws Exception {

        restService.restartDeposit(depositId);

        return getDepositRedirectUrl(vaultId, depositId);
    }

    public String getDepositRedirectUrl(String vaultId, String depositId) {
        String depositUrl = "/vaults/" + vaultId + "/deposits/" + depositId + "/";
        return "redirect:" + depositUrl;
    }
}
