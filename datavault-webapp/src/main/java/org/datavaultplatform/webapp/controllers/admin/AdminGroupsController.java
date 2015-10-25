package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

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
        // Get the groups
        Group[] groups = restService.getGroups();
        model.addAttribute("groups", groups);

        // Get the vault count per group
        int[] vaultCounts = new int[groups.length];
        int counter = 0;
        for (Group group : groups) {
            vaultCounts[counter++] = restService.getGroupVaultCount(group.getID());
        }
        model.addAttribute("vaultCounts", vaultCounts);

        return "admin/groups/index";
    }
}


