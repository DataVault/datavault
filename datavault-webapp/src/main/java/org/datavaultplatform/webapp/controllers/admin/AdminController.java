package org.datavaultplatform.webapp.controllers.admin;


import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.retentionpolicy.PolicyStatus;
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
    public String adminIndex(ModelMap model) {
        model.addAttribute("usercount", restService.getUsersCount());
        model.addAttribute("groupcount", restService.getGroupsCount());
        model.addAttribute("vaultcount", restService.getVaultsCount());
        model.addAttribute("depositcount", restService.getDepositsCount());
        model.addAttribute("restorecount", restService.getRestoresCount());
        Long vaultSize = restService.getVaultsSize();
        if (vaultSize == null) vaultSize = new Long(0);
        model.addAttribute("vaultsize", FileUtils.byteCountToDisplaySize(vaultSize));
        model.addAttribute("depositsinprogress", restService.getDepositsInProgressCount());
        model.addAttribute("restoresinprogress", restService.getRestoresInProgressCount());
        model.addAttribute("depositqueue", restService.getDepositsQueue());
        model.addAttribute("restorequeue", restService.getRestoresQueue());
        model.addAttribute("reviewcount", restService.getPolicyStatusCount(PolicyStatus.REVIEW));
        model.addAttribute("eventcount", restService.getEventCount());

        return "admin/index";
    }
}


