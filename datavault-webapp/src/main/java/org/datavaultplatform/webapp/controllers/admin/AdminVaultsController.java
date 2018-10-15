package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;

/**
 * User: Stuart Lewis
 * Date: 27/09/2015
 */

@Controller
public class AdminVaultsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminVaultsController.class);

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/vaults", method = RequestMethod.GET)
    public String searchVaults(ModelMap model,
                               @RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "sort", required = false) String sort,
                               @RequestParam(value = "order", required = false) String order) {
        String theSort = sort;
        String theOrder = order;
        if (sort == null) theSort = "creationTime";
        if (order == null) theOrder = "desc";

        if ((query == null) || ("".equals(query))) {
            model.addAttribute("vaults", restService.getVaultsListingAll(theSort, theOrder));
            model.addAttribute("query", "");
        } else {
            model.addAttribute("vaults", restService.searchVaults(query, theSort, theOrder));
            model.addAttribute("query", query);
        }

        // Pass the sort and order
        if (sort == null) sort = "";
        model.addAttribute("sort", sort);
        model.addAttribute("orderid", "asc");
        model.addAttribute("ordername", "asc");
        model.addAttribute("orderdescription", "asc");
        model.addAttribute("orderuser", "asc");
        model.addAttribute("ordervaultsize", "asc");
        model.addAttribute("orderpolicy", "asc");
        model.addAttribute("ordercreationtime", "asc");
        if ("asc".equals(order)) {
            if ("id".equals(sort)) model.addAttribute("orderid", "dec");
            if ("name".equals(sort)) model.addAttribute("ordername", "dec");
            if ("description".equals(sort)) model.addAttribute("orderdescription", "dec");
            if ("user".equals(sort)) model.addAttribute("orderuser", "dec");
            if ("vaultSize".equals(sort)) model.addAttribute("ordervaultsize", "dec");
            if ("policy".equals(sort)) model.addAttribute("orderpolicy", "dec");
            if ("creationTime".equals(sort)) model.addAttribute("ordercreationtime", "dec");
        }

        return "admin/vaults/index";
    }

    @RequestMapping(value = "/admin/vaults/csv", method = RequestMethod.GET)
    public void exportVaults(HttpServletResponse response,
                               @RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "sort", required = false) String sort,
                               @RequestParam(value = "order", required = false) String order) {
        String theSort = sort;
        String theOrder = order;
        if (sort == null) theSort = "creationTime";
        if (order == null) theOrder = "desc";

        VaultInfo[] vaults = null;

        if ((query == null) || ("".equals(query))) {
            vaults = restService.getVaultsListingAll(theSort, theOrder);
        } else {
            vaults =  restService.searchVaults(query, theSort, theOrder);
        }

        response.setContentType("text/csv");

        // creates mock data
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=\"vaults.csv\"";
        response.setHeader(headerKey, headerValue);

        String[] header = { "Vault ID", "Vault name", "Vault description", "Vault Size", "User ID", "User Name", "Policy ID", "Creation Time" };

        String[] fieldMapping = { "id", "name", "description", "userID", "userName", "sizeStr", "policyID", "CreationTime" };

        try {
            // uses the Super CSV API to generate CSV data from the model data
            ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

            csvWriter.writeHeader(header);

            for (VaultInfo aVault : vaults) {
                csvWriter.write(aVault, fieldMapping);
            }

            csvWriter.close();

        } catch (Exception e){
            logger.error("IOException: "+e);
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/admin/vaults/{vaultid}", method = RequestMethod.GET)
    public String showVault(ModelMap model, @PathVariable("vaultid") String vaultID) {
        VaultInfo vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);

        model.addAttribute(restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));
        model.addAttribute("deposits", restService.getDepositsListing(vaultID));

        return "admin/vaults/vault";
    }

    @RequestMapping(value = "/admin/vaults/{vaultid}/checkretentionpolicy", method = RequestMethod.POST)
    public String checkPolicy(ModelMap model, @PathVariable("vaultid") String vaultID) throws Exception {
        restService.checkVaultRetentionPolicy(vaultID);

        return "redirect:/admin/vaults/" + vaultID;
    }
}


