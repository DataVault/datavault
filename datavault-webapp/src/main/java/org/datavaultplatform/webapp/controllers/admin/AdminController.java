package org.datavaultplatform.webapp.controllers.admin;


import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.model.Policy;
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
public class AdminController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String adminIndex(ModelMap model) {
        model.addAttribute("usercount", restService.getUsersCount());
        model.addAttribute("vaultcount", restService.getVaultsCount());
        model.addAttribute("depositcount", restService.getDepositsCount());
        Long vaultSize = restService.getVaultsSize();
        if (vaultSize == null) vaultSize = new Long(0);
        model.addAttribute("vaultsize", FileUtils.byteCountToDisplaySize(vaultSize));

        return "admin/index";
    }
}


