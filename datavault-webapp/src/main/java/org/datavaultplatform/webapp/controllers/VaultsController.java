package org.datavaultplatform.webapp.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
//@RequestMapping("/vaults")
public class VaultsController {

    private static final Logger logger = LoggerFactory.getLogger(VaultsController.class);

    private RestService restService;
    private UserLookupService userLookupService;
    private String system;
    private String link;
    private String welcome;

    public void setSystem(String system) throws Exception {
        this.system = system;
    }
    public void setLink(String link) throws Exception {
        this.link = link;
    }

	public void setRestService(RestService restService) throws Exception {
        this.restService = restService;
    }

    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    public void setUserLookupService(UserLookupService userLookupService) {
        this.userLookupService = userLookupService;
    }

    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public String getVaultsListing(ModelMap model) throws Exception {
        model.addAttribute("vaults", restService.getVaultsListing());
        
        // pass the view an empty Vault since the form expects it
        model.addAttribute("vault", new CreateVault());

        Dataset[] datasets = restService.getDatasets();
        model.addAttribute("datasets", datasets);
        
        RetentionPolicy[] policies = restService.getRetentionPolicyListing();
        model.addAttribute("policies", policies);

        Group[] groups = restService.getGroups();
        model.addAttribute("groups", groups);
        
        model.put("system", system);
        model.put("link", link);

        model.addAttribute("welcome", welcome);

        return "vaults/index";
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID) throws Exception {
        VaultInfo vault = restService.getVault(vaultID);

        List<RoleAssignment> roleAssignmentsForVault = restService.getRoleAssignmentsForVault(vaultID);
        model.addAttribute("vault", vault);
        model.addAttribute("roles", restService.getVaultRoles());
        model.addAttribute("roleAssignments", roleAssignmentsForVault);
        model.addAttribute(restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));
        
        DepositInfo[] deposits = restService.getDepositsListing(vaultID);
        model.addAttribute("deposits", deposits);
        
        Map<String, Retrieve[]> depositRetrievals = new HashMap<String, Retrieve[]>();
        for (DepositInfo deposit : deposits) {
            Retrieve[] retrievals = restService.getDepositRetrieves(deposit.getID());
            depositRetrievals.put(deposit.getName(), retrievals);
        }
        model.addAttribute("retrievals", depositRetrievals);
        
        DataManager[] dataManagers = restService.getDataManagers(vaultID);
        List<User> dataManagerUsers = new ArrayList<>();
        for(DataManager dm : dataManagers){
            User u = restService.getUser(dm.getUUN());
            if(u == null) {
                u = new User();
                u.setID(dm.getUUN());
                u.setAdmin(false);

                // Generate random password to make sure account is not easily accessible
                String password = RandomStringUtils.randomAscii(10);
                u.setPassword(password);

                restService.addUser(u);
            }
            dataManagerUsers.add(u);
        }
        model.addAttribute("dataManagers", dataManagerUsers);
        
        return "vaults/vault";
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/{userid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID,@PathVariable("userid") String userID) throws Exception {
    	model.addAttribute("vaults", restService.getVaultsListingAll(userID));
    	        
        return "vaults/userVaults";
    }

    // Process the completed 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.POST)
    public String addVault(@ModelAttribute CreateVault vault, ModelMap model) throws Exception {
        VaultInfo newVault = restService.addVault(vault);
        String vaultUrl = "/vaults/" + newVault.getID() + "/";
        return "redirect:" + vaultUrl;        
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/addDataManager", method = RequestMethod.POST)
    public RedirectView addDataManager(ModelMap model,
                                       @PathVariable("vaultid") String vaultID,
                                       @RequestParam("uun") String uun,
                                       final RedirectAttributes redirectAttrs) throws Exception {
        logger.debug("Adding "+uun+" as DM");

        String vaultUrl = "/vaults/" + vaultID + "/";

        DataManager[] dataManagers = restService.getDataManagers(vaultID);
        logger.debug("Check if already DM");
        for( DataManager dm : dataManagers ){
            logger.debug("DM: "+dm.getUUN());
            if(dm.getUUN().equals(uun)){
                logger.info("User " + uun + " is already a Data Manager");
                redirectAttrs.addFlashAttribute("warning", "<i>"+uun+"</i> is already a Data Manager of this Vault!");
                return new RedirectView(vaultUrl, true);
            }
        }

        try {
            userLookupService.ensureUserExists(uun);
        } catch (InvalidUunException e) {
            redirectAttrs.addFlashAttribute("error", "<i>'" + uun + "'</i> doesnt seem to be a valid UUN!");
            return new RedirectView(vaultUrl, true);
        }

        restService.addDataManager(vaultID, uun);

        redirectAttrs.addFlashAttribute("success",
                "<strong>'"+uun+"'</strong> added as Data Manager of this Vault!");
        return new RedirectView(vaultUrl, true);
    }
    
    
    @RequestMapping(value = "/vaults/{vaultid}/deleteDataManager", method = RequestMethod.POST)
    public RedirectView deleteDataManager(ModelMap model,
                                @PathVariable("vaultid") String vaultID,
                                @RequestParam("uun") String uun,
                                final RedirectAttributes redirectAttrs) throws Exception {
        logger.info("Get Data Manager with id: "+uun);
        DataManager dataManager = restService.getDataManager(vaultID, uun);

        logger.info("Deleting Data Manager...");
        restService.deleteDataManager(vaultID, dataManager.getID());

        String vaultUrl = "/vaults/" + vaultID + "/";
        redirectAttrs.addFlashAttribute("success",
                "<strong>'"+uun+"'</strong> is no longer a Data Manager of this Vault!");
        return new RedirectView(vaultUrl, true);
    }


    @RequestMapping(value = "/vaults/{vaultid}/updateVaultDescription", method = RequestMethod.POST)
    public String updateVaultDescription(ModelMap model,
                                 @PathVariable("vaultid") String vaultID,
                                 @RequestParam("description") String description ) throws Exception {
        VaultInfo vault = restService.updateVaultDescription(vaultID, description);
        String vaultUrl = "/vaults/" + vault.getID() + "/";
        return "redirect:" + vaultUrl;
    }

    @RequestMapping(value = "/vaults/autocompleteuun/{term}", method = RequestMethod.GET)
    @ResponseBody
    public String autocompleteUUN(@PathVariable("term") String term) {
        List<String> result = userLookupService.getSuggestedUuns(term);
        Gson gson = new Gson();
        return gson.toJson(result);
    }
}


