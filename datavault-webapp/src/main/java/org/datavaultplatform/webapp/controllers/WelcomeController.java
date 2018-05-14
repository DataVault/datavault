package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.Dataset;
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
    
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getVaultsListing(ModelMap model) {
        return "redirect:/vaults";
    }
    
//    @RequestMapping(value = "/", method = RequestMethod.GET)
//    public String getVaultsListing(ModelMap model) {
//
//        if (restService.getVaultsListing().length > 0) {
//            return "redirect:/vaults";
//        }
//
//        if (restService.getFileStoreListing().length > 0) {
//            model.addAttribute("filestoresExist", true);
//        } else {
//            model.addAttribute("filestoresExist", false);
//        }
//
//        if (restService.getDatasets().length > 0) {
//            model.addAttribute("datasetsExist", true);
//        } else {
//            model.addAttribute("datasetsExist", false);
//        }
//
//        model.put("system", system);
//        model.put("link", link);
//
//        return "welcome";
//
//    }
}
