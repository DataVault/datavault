package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(RestService.class)
public class WelcomeController {

    private final RestService restService;

    private final String system;
    private final String link;

    @Autowired
    public WelcomeController(RestService restService,
        @Value("${metadata.system}") String system,
        @Value("${metadata.link}") String link) {
        this.restService = restService;
        this.system = system;
        this.link = link;
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
