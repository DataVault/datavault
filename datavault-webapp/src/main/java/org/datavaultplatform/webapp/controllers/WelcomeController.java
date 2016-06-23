package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: Robin Taylor
 * Date: 23/06/2016
 * Time: 14:20
 */
@Controller
public class WelcomeController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getVaultsListing(ModelMap model) {

        if (restService.getFileStoreListing().length == 0) {
            model.addAttribute("noFilestores", true);
            return "welcome";
        } else if (restService.getVaultsListing().length == 0) {
            model.addAttribute("noFilestores", false);
            model.addAttribute("noVaults", true);
            return "welcome";
        }

        return "redirect:/vaults";
    }
}
