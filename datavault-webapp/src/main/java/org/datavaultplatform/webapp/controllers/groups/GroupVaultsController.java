package org.datavaultplatform.webapp.controllers.groups;


import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Stuart Lewis
 * Date: 25/10/2015
 */

@Controller
public class GroupVaultsController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public String getGroupVaultsListing(ModelMap model) {
        // Which groups is this user an owner of
        Group[] groups = restService.getGroups();
        ArrayList<Group> members = new ArrayList<Group>();
        for (Group group : groups) {
            if (group.hasMember(SecurityContextHolder.getContext().getAuthentication().getName())) {
                members.add(group);
            }
        }

        // Get the vaults for each group
        ArrayList<Vault[]> vaults = new ArrayList<Vault[]>();
        for (Group group : members) {
            Vault[] v = restService.getVaultsListingForGroup(group.getID());
            vaults.add(v);
        }

        model.addAttribute("vaults", vaults);
        model.addAttribute("groups", members);
        return "groups/index";
    }
}


