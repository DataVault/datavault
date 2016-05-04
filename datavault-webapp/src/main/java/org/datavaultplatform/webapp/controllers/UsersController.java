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



    // Return an 'add keys' page
    @RequestMapping(value = "/users/{userid}/keys", method = RequestMethod.GET)
    public String addKeys(ModelMap model, @PathVariable("userid") String userID) {

        model.addAttribute("keysExist", restService.keysExist(userID));
        model.addAttribute("user", restService.getUser(userID));
        return "users/addKeys";
    }

    // Process the completed 'add keys' page
    @RequestMapping(value = "/users/{userid}/keys", method = RequestMethod.POST)
    public String addKeys(@ModelAttribute User user, ModelMap model, @PathVariable("userid") String userID, @RequestParam String action) {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        model.addAttribute("publicKey", restService.addKeys(userID));
        model.addAttribute("user", restService.getUser(userID));

        return "users/listKeys";
    }





}
