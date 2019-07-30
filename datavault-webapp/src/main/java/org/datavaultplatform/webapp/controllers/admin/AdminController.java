package org.datavaultplatform.webapp.controllers.admin;


import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;


/**
 * User: Stuart Lewis
 * Date: 20/09/2015
 */

@Controller
public class AdminController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String adminIndex(ModelMap model) throws Exception{
        model.addAttribute("usercount", restService.getUsersCount());
        model.addAttribute("groupcount", restService.getGroupsCount());
        model.addAttribute("vaultcount", restService.getVaultsCount());
        model.addAttribute("depositcount", restService.getDepositsCount());
        model.addAttribute("retrievecount", restService.getRetrievesCount());
        Long vaultSize = restService.getVaultsSize();
        if (vaultSize == null) vaultSize = new Long(0);
        model.addAttribute("vaultsize", FileUtils.byteCountToDisplaySize(vaultSize));
        model.addAttribute("depositsinprogress", restService.getDepositsInProgressCount());
        model.addAttribute("retrievesinprogress", restService.getRetrievesInProgressCount());
        model.addAttribute("depositqueue", restService.getDepositsQueue());
        model.addAttribute("retrievequeue", restService.getRetrievesQueue());
        model.addAttribute("rolecount", getManagableRolesCount());
        model.addAttribute("eventcount", restService.getEventCount());
        model.addAttribute("policycount", restService.getRetentionPolicyListing().length);
        model.addAttribute("archivestorescount", restService.getArchiveStores().length);

        return "admin/index";
    }

    private int getManagableRolesCount() {
        return restService.getViewableRoles().size() + restService.getEditableRoles().size();
    }
}


