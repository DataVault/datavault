package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Robin Taylor
 * Date: 27/11/2015
 * Time: 11:00
 */
@Controller
public class UsersController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    // Return an 'edit user' page
    @RequestMapping(value = "/users/edit/{userid}", method = RequestMethod.GET)
    public String editUser(ModelMap model, @PathVariable("userid") String userID) {

        model.addAttribute("user", restService.getUser(userID));
        return "users/edit";
    }

    // Process the completed 'create new vault' page
    @RequestMapping(value = "/users/edit/{userid}", method = RequestMethod.POST)
    public String editUser(@ModelAttribute User user, ModelMap model, @RequestParam String action) {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        //User newVault = restService..addVault(vault);

        return "vaults/index";
    }


}
