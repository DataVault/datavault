package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: Stuart Lewis
 * Date: 27/09/2015
 */

@Controller
public class AdminVaultsController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/vaults", method = RequestMethod.GET)
    public String getVaultsListing(ModelMap model) {
        model.addAttribute("vaults", restService.getVaultsListingAll());
        return "admin/vaults/index";
    }
}


