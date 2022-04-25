package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.webapp.services.RestService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: Stuart Lewis
 * Date: 6/10/2015
 */

@Controller
@ConditionalOnBean(RestService.class)
public class AdminRetrievesController {

    private final RestService restService;

    public AdminRetrievesController(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/retrieves", method = RequestMethod.GET)
    public String getRetrievesListing(ModelMap model) throws Exception {
        model.addAttribute("retrieves", restService.getRetrievesListingAll());
        return "admin/retrieves/index";
    }
}


