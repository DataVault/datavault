package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Stuart Lewis
 * Date: 20/09/2015
 */

@Controller
@ConditionalOnBean(RestService.class)
public class AdminUsersController implements AdminUsersControllerApi {

    private final RestService restService;

    @Autowired
    public AdminUsersController(RestService restService) {
        this.restService = restService;
    }

    @Override
    @GetMapping(value = "/admin/users", produces = MediaType.TEXT_HTML_VALUE)
    public String getUsersListing(ModelMap model,
                                  @RequestParam(value = "query", required = false) String query) throws Exception {
        if ((query == null) || (query.isEmpty())) {
            model.addAttribute("users", restService.getUsers());
            model.addAttribute("query", "");
        } else {
            model.addAttribute("users", restService.searchUsers(query));
            model.addAttribute("query", query);
        }

        return "admin/users/index";
    }

    // Return an empty 'create new user' page
    @Override
    @GetMapping(value = "/admin/users/create", produces = MediaType.TEXT_HTML_VALUE)
    public String createUserPage(ModelMap model) throws Exception {
        // pass the view an empty User since the form expects it
        model.addAttribute("user", new User());

        return "admin/users/create";
    }

    // Process the completed 'create new user' page
    @Override
    @PostMapping(value = "/admin/users/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
    @Override
    @GetMapping(value = "/admin/users/edit/{userId}", produces = MediaType.TEXT_HTML_VALUE)
    public String editUser(ModelMap model, @PathVariable String userId) throws Exception {

        model.addAttribute("user", restService.getUser(userId));
        return "admin/users/edit";
    }

    // Process the completed 'edit user' page
    @Override
    @PostMapping(value = "/admin/users/edit/{userId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String editUser(@ModelAttribute User user, ModelMap model, @PathVariable String userId, @RequestParam String action) throws Exception {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        // todo : Is using the userID sensible? Should we use an alternative editUserRequest model? etc
        // todo: This should be considered hacky test code, no more.

        User existingUser = restService.getUser(userId);
        //existingUser.setName(user.getName());
        restService.editUser(existingUser);

        return "vaults/index";
    }
}


