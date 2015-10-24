package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: Stuart Lewis
 * Date: 24/10/2015
 */

@Controller
public class AdminGroupsController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/groups", method = RequestMethod.GET)
    public String getGroupsListing(ModelMap model) {
        model.addAttribute("groups", restService.getGroups());
        return "admin/groups/index";
    }
}


