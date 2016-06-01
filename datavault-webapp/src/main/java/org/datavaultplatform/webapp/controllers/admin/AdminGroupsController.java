package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

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

        // Get list of all users
        model.addAttribute("users", restService.getUsers());
        
        return "admin/groups/index";
    }
    
    // Return an empty 'create new group' page
    @RequestMapping(value = "/admin/groups/create", method = RequestMethod.GET)
    public String createGroup(ModelMap model) {

        // pass the view an empty Group since the form expects it
        model.addAttribute("group", new Group());
        
        return "admin/groups/create";
    }
    
    // Process the completed 'create new group' page
    @RequestMapping(value = "/admin/groups/create", method = RequestMethod.POST)
    public String addGroup(@ModelAttribute Group group, ModelMap model, @RequestParam String action) {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/admin/groups/";
        }

        Group newGroup = restService.addGroup(group);
        return "redirect:/admin/groups/";      
    }
    
    // Add a group owner
    @RequestMapping(value = "/admin/groups/{groupid}/{userid}", method = RequestMethod.PUT)
    public @ResponseBody void addGroupOwner(ModelMap model,
                                            @PathVariable("groupid") String groupId,
                                            @PathVariable("userid") String userId) {
        restService.addGroupOwner(groupId, userId);
    }
    
    // Remove a group owner
    @RequestMapping(value = "/admin/groups/{groupid}/{userid}", method = RequestMethod.DELETE)
    public @ResponseBody void removeGroupOwner(ModelMap model,
                                               @PathVariable("groupid") String groupId,
                                               @PathVariable("userid") String userId) {
        restService.removeGroupOwner(groupId, userId);
    }
}


