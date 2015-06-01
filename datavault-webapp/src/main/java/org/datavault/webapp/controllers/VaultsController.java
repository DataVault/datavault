package org.datavault.webapp.controllers;


import org.datavault.common.model.Vault;
import org.datavault.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.Map;

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

    // Return an empty 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.GET)
    public String addVault(ModelMap model) {
        //model.addAttribute("vaults", restService.getVaultsListing());
        return "vaults/create";
    }

    // Process the completed 'create new vault' page
    //@RequestMapping(value = "/vaults", method = RequestMethod.POST)
    //public String addVault(ModelMap model) {
    //    model.addAttribute("vaults", restService.getVaultsListing());
    //    return "vaults";
   // }


}
