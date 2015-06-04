package org.datavault.webapp.controllers;


import org.datavault.common.model.Deposit;
import org.datavault.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
        model.addAttribute("vaultID", vaultID);
        return "/deposits/create";
    }

    // Process the completed 'create new deposit' page
    @RequestMapping(value = "/vaults/{vaultid}/deposits/create", method = RequestMethod.POST)
    public String addDeposit(@ModelAttribute Deposit deposit, ModelMap model, @PathVariable("vaultid") String vaultID) {
        restService.addDeposit(vaultID, deposit);
// todo : do something with the returned deposit

        model.addAttribute("vault", restService.getVault(vaultID));
        return "vaults/vault";

    }

    /*
    @RequestMapping(value = "/deposits", method = RequestMethod.GET)
    public String createDeposit(ModelMap model) {
        model.addAttribute("files", restService.getFilesListing());
        return "files";
    }


    @RequestMapping(value = "/deposits", method = RequestMethod.GET)
    public String getFilesListing(ModelMap model) {
        model.addAttribute("files", restService.getFilesListing());
        return "files";
    }
*/

}
