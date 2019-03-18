package org.datavaultplatform.webapp.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.services.LDAPService;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private LDAPService ldapService;
    private String system;
    private String link;

    public void setSystem(String system) {
        this.system = system;
    }
    public void setLink(String link) {
        this.link = link;
    }

	public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setLDAPService(LDAPService ldapService) {
        this.ldapService = ldapService;
    }

    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public String getVaultsListing(ModelMap model) {
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

        return "vaults/index";
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID) {
        VaultInfo vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);

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

    // Process the completed 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.POST)
    public String addVault(@ModelAttribute CreateVault vault, ModelMap model) {
        VaultInfo newVault = restService.addVault(vault);
        String vaultUrl = "/vaults/" + newVault.getID() + "/";
        return "redirect:" + vaultUrl;        
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/addDataManager", method = RequestMethod.POST)
    public RedirectView addDataManager(ModelMap model,
                                       @PathVariable("vaultid") String vaultID,
                                       @RequestParam("uun") String uun,
                                       final RedirectAttributes redirectAttrs) {
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

        User u = restService.getUser(uun);
        if( u == null ) {
            // Validate UUN
            HashMap<String, String> attributes;
            try {
                attributes = ldapService.getLdapUserInfo(uun);
            } catch (LdapException | CursorException | IOException e) {
                e.printStackTrace();
                redirectAttrs.addFlashAttribute("error",
                        "<i>'"+uun+"'</i> doesnt seem to be a valid UUN!");
                return new RedirectView(vaultUrl, true);
            }
            if(attributes.size() < 1){
                redirectAttrs.addFlashAttribute("error",
                        "<i>'"+uun+"'</i> doesnt seem to be a valid UUN!");
                return new RedirectView(vaultUrl, true);
            }

            String[] names = attributes.get("cn").split(" "); attributes.remove("cn");

            logger.info("Adding user " + uun + " - " + names[0] + " " + names[1]);
            u = new User();
            u.setFirstname(names[0]);
            u.setLastname(names[1]);
            u.setID(attributes.get("uid")); attributes.remove("uid");
            u.setEmail(attributes.get("mail")); attributes.remove("mail");
            u.setProperties(attributes);
            u.setAdmin(false);

            // Generate random password to make sure account is not easily accessible
            String password = RandomStringUtils.randomAscii(10);
            u.setPassword(password);

            restService.addUser(u);
        } else {
            logger.info("User "+uun+" already exist!");
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
                                final RedirectAttributes redirectAttrs) {
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
                                 @RequestParam("description") String description ) {
        VaultInfo vault = restService.updateVaultDescription(vaultID, description);
        String vaultUrl = "/vaults/" + vault.getID() + "/";
        return "redirect:" + vaultUrl;
    }

    @RequestMapping(value = "/vaults/autocompleteuun/{term}", method = RequestMethod.GET)
    @ResponseBody
    public String autocompleteUUN(@PathVariable("term") String term) {
        List<String> result = new ArrayList<>();
        try {
            result = ldapService.autocompleteUID(term);
        }catch(LdapException | CursorException | IOException ex) {
            ex.printStackTrace();
        }
        Gson gson = new Gson();
        String jsonArray = gson.toJson(result);
        return jsonArray;
    }
}


