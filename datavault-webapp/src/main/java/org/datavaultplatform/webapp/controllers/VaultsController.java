package org.datavaultplatform.webapp.controllers;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.commons.lang.RandomStringUtils;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.request.TransferVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.webapp.exception.EntityNotFoundException;
import org.datavaultplatform.webapp.exception.ForbiddenException;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void setSystem(String system) {
        this.system = system;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    public void setUserLookupService(UserLookupService userLookupService) {
        this.userLookupService = userLookupService;
    }

    @PreAuthorize("hasPermission(#vaultId, 'VAULT', 'CAN_TRANSFER_VAULT_OWNERSHIP') or hasPermission(#vaultId, 'GROUP_VAULT', 'TRANSFER_SCHOOL_VAULT_OWNERSHIP')")
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
                    .anyMatch(role -> role.getVaultId() != null && role.getVaultId().equals(vaultId));

            if (hasVaultRole) {
                return ResponseEntity.status(422).body("User already has a role in this vault");
            }

            VaultInfo vault = restService.getVault(vaultId);
            if (vault == null) {
                throw new EntityNotFoundException(Vault.class, vaultId);
            }

            String userId = vault.getUserID();
            if (userId != null && userId.equals(request.user)) {
                return ResponseEntity.status(422).body("Cannot transfer ownership to the current owner");
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

        model.addAttribute("welcome", welcome);

        return "vaults/index";
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID, Principal principal) {
        VaultInfo vault = restService.getVault(vaultID);

        if (!canAccessVault(vault, principal)) {
            throw new ForbiddenException();
        }

        List<RoleAssignment> roleAssignmentsForVault = restService.getRoleAssignmentsForVault(vaultID);
        List<RoleAssignment> vaultUsers = roleAssignmentsForVault.stream()
                .filter(roleAssignment -> !RoleUtils.isDataOwner(roleAssignment))
                .collect(Collectors.toList());
        roleAssignmentsForVault.stream()
                .filter(RoleUtils::isDataOwner)
                .findFirst()
                .ifPresent(roleAssignment -> model.addAttribute("dataOwner", roleAssignment));

        model.addAttribute("vault", vault);
        model.addAttribute("roles", restService.getVaultRoles());
        model.addAttribute("roleAssignments", vaultUsers);
        model.addAttribute(restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));
        
        DepositInfo[] deposits = restService.getDepositsListing(vaultID);
        model.addAttribute("deposits", deposits);

        Map<String, Retrieve[]> depositRetrievals = new HashMap<>();
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

    private boolean canAccessVault(VaultInfo vault, Principal principal) {
        List<RoleAssignment> roleAssignmentsForUser = restService.getRoleAssignmentsForUser(principal.getName());
        return roleAssignmentsForUser.stream().anyMatch(roleAssignment ->
                RoleUtils.isISAdmin(roleAssignment)
                        || RoleUtils.isRoleInVault(roleAssignment, vault.getID())
                        || (RoleUtils.isRoleInSchool(roleAssignment, vault.getGroupID()) && RoleUtils.hasPermission(roleAssignment, Permission.CAN_MANAGE_VAULTS)));
    }

    @RequestMapping(value = "/vaults/{vaultid}/{userid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID, @PathVariable("userid") String userID) {
    	model.addAttribute("vaults", restService.getVaultsListingAll(userID));
    	        
        return "vaults/userVaults";
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
                                         @RequestParam("description") String description) {
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
            return orphaning || !Strings.isNullOrEmpty(user);
        }

        @AssertTrue(message = "Must select a role when assigning a new role.")
        public boolean isRoleSelectionValid() {
            return !assigningRole || role != null;
        }
    }
}


