package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Stuart Lewis
 * Date: 27/09/2015
 */

@Controller
public class AdminDepositsController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/deposits", method = RequestMethod.GET)
    public String getDepositsListing(ModelMap model,
                                     @RequestParam(value = "query", required = false) String query,
                                     @RequestParam(value = "sort", required = false) String sort) throws Exception {
        if ((query == null) || ("".equals(query))) {
            if ((sort == null) || ("".equals(sort))) {
                model.addAttribute("deposits", restService.getDepositsListingAll());
            } else {
                model.addAttribute("deposits", restService.getDepositsListingAll(sort));
            }
            model.addAttribute("query", "");
        } else {
            if ((sort == null) || ("".equals(sort))) {
                model.addAttribute("deposits", restService.searchDeposits(query));
            } else {
                model.addAttribute("deposits", restService.searchDeposits(query, sort));
            }
            model.addAttribute("query", query);
        }

        return "admin/deposits/index";
    }
}


