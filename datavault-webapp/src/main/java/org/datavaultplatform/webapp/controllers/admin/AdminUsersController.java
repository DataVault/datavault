package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Policy;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Stuart Lewis
 * Date: 20/09/2015
 */

@Controller
public class AdminUsersController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    public String getUsersListing(ModelMap model) {
        model.addAttribute("users", restService.getUsers());
        return "admin/users/index";
    }
}


