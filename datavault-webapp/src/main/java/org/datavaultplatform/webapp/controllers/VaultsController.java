package org.datavaultplatform.webapp.controllers;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.commons.lang.RandomStringUtils;
import org.datavaultplatform.common.model.DataManager;
import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.request.TransferVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @PostMapping(value = "/vaults/{vaultid}/data-owner/update")
    public ResponseEntity transferOwnership(
            @PathVariable("vaultid") String vaultId,
            @Valid VaultTransferRequest request) {

        if (!request.isOrphaning()) {
            User newOwner = restService.getUser(request.user);
            if (newOwner == null) {
                return ResponseEntity.status(422).body("No such user)");
            }

            boolean hasVaultRole = restService.getRoleAssignmentsForUser(request.user)
                    .stream()
                    .anyMatch(role -> {
                        Vault vault = role.getVault();
                        return vault != null && vault.getID().equals(vaultId);
                    });

            if (hasVaultRole) {
                return ResponseEntity.status(422).body("User already has a role in this vault");
            }
        }

        TransferVault transfer = new TransferVault();
        transfer.setUserId(request.user);
        transfer.setRoleId(request.role);
        transfer.setChangingRoles(request.assigningRole);
        transfer.setOrphaning(request.orphaning);

        restService.transferVault(vaultId, transfer);

        return ResponseEntity.ok().build();
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

    private static class VaultTransferRequest {
        private Long role;
        private String user;
        private boolean assigningRole;
        private boolean orphaning;
        private String reason;

        public void setOrphaning(boolean orphaning) {
            this.orphaning = orphaning;
        }

        public boolean isOrphaning() {
            return orphaning;
        }

        public void setAssigningRole(boolean assigningRole) {
            this.assigningRole = assigningRole;
        }

        public void setRole(Long role) {
            this.role = role;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public Long getRole() {
            return role;
        }

        public String getUser() {
            return user;
        }

        @NotEmpty(message = "Must provide a transfer reason.")
        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public boolean isAssigningRole() {
            return assigningRole;
        }

        @AssertTrue(message = "Must select a user.")
        public boolean isUserSelectionValid() {
            return !orphaning && !Strings.isNullOrEmpty(user);
        }

        @AssertTrue(message = "Must select a role when assigning a new role.")
        public boolean isRoleSelectionValid() {
            return !assigningRole || role != null;
        }
    }
}


