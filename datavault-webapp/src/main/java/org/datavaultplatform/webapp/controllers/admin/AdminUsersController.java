package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Stuart Lewis
 * Date: 20/09/2015
 */

@Controller
@ConditionalOnBean(RestService.class)
public class AdminUsersController {

    private final RestService restService;

    @Autowired
    public AdminUsersController(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    public String getUsersListing(ModelMap model,
                                  @RequestParam(value = "query", required = false) String query) throws Exception {
        if ((query == null) || ("".equals(query))) {
            model.addAttribute("users", restService.getUsers());
            model.addAttribute("query", "");
        } else {
            model.addAttribute("users", restService.searchUsers(query));
            model.addAttribute("query", query);
        }

        return "admin/users/index";
    }

    // Return an empty 'create new user' page
    @RequestMapping(value = "/admin/users/create", method = RequestMethod.GET)
    public String createUser(ModelMap model) throws Exception {
        // pass the view an empty User since the form expects it
        model.addAttribute("user", new User());

        return "admin/users/create";
    }

    // Process the completed 'create new user' page
    @RequestMapping(value = "/admin/users/create", method = RequestMethod.POST)
    public String addUser(@ModelAttribute User user, ModelMap model, @RequestParam String action) throws Exception {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        if (restService.getUser(user.getID()) == null) {
            User newUser = restService.addUser(user);
            return "redirect:/admin/users";
        } else {
            // User already exists
            // todo : Return error message

            return "admin/users/create";
        }
    }

    // Return an 'edit user' page
    @RequestMapping(value = "/admin/users/edit/{userid}", method = RequestMethod.GET)
    public String editUser(ModelMap model, @PathVariable("userid") String userID) throws Exception {

        model.addAttribute("user", restService.getUser(userID));
        return "admin/users/edit";
    }

    // Process the completed 'edit user' page
    @RequestMapping(value = "/admin/users/edit/{userid}", method = RequestMethod.POST)
    public String editUser(@ModelAttribute User user, ModelMap model, @PathVariable("userid") String userID, @RequestParam String action) throws Exception {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        // todo : Is using the userID sensible? Should we use an alternative editUserRequest model? etc
        // todo: This should be considered hacky test code, no more.

        User existingUser = restService.getUser(userID);
        //existingUser.setName(user.getName());
        restService.editUser(existingUser);

        return "vaults/index";
    }
}


