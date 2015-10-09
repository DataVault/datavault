package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String searchVaults(ModelMap model,
                               @RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "sort", required = false) String sort) {
        if ((query == null) || ("".equals(query))) {
            if ((sort == null) || ("".equals(sort))) {
                model.addAttribute("vaults", restService.getVaultsListingAll());
            } else {
                model.addAttribute("vaults", restService.getVaultsListingAll(sort));
            }
            model.addAttribute("query", "");
        } else {
            if ((sort == null) || ("".equals(sort))) {
                model.addAttribute("vaults", restService.searchVaults(query));
            } else {
                model.addAttribute("vaults", restService.searchVaults(query, sort));
            }
            model.addAttribute("query", query);
        }
        return "admin/vaults/index";
    }
}


