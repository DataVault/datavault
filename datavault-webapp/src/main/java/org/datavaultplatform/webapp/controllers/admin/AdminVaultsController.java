package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Stuart Lewis
 * Date: 27/09/2015
 */

@Controller
public class AdminVaultsController {

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
        if (order == null) theOrder = "asc";

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

    @RequestMapping(value = "/admin/vaults/{vaultid}", method = RequestMethod.GET)
    public String showVault(ModelMap model, @PathVariable("vaultid") String vaultID) {
        Vault vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);

        model.addAttribute(restService.getPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));
        model.addAttribute("deposits", restService.getDepositsListing(vaultID));

        return "admin/vaults/vault";
    }

    @RequestMapping(value = "/admin/vaults/{vaultid}/checkpolicy", method = RequestMethod.POST)
    public String checkPolicy(ModelMap model, @PathVariable("vaultid") String vaultID) throws Exception {
        restService.checkVaultPolicy(vaultID);

        return "redirect:/admin/vaults/" + vaultID;
    }
}


