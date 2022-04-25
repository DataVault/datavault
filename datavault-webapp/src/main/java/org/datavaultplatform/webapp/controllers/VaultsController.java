package org.datavaultplatform.webapp.controllers;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomStringUtils;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.request.TransferVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.EventInfo;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.webapp.exception.EntityNotFoundException;
import org.datavaultplatform.webapp.exception.ForbiddenException;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewHistoryModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.services.ForceLogoutService;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.datavaultplatform.webapp.services.ValidateService;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@ConditionalOnBean(RestService.class)
//@RequestMapping("/vaults")
public class VaultsController {

    private static final Logger logger = LoggerFactory.getLogger(VaultsController.class);

    private final RestService restService;
    private final UserLookupService userLookupService;
    private ValidateService validateService;
    private final ForceLogoutService logoutService;
    private final String system;
    private final String link;
    private final String welcome;

    @Autowired
    public VaultsController(RestService restService,
        UserLookupService userLookupService,
        ForceLogoutService logoutService,
        ValidateService validateService,
        @Value("${metadata.system}") String system,
        @Value("${metadata.link}") String link,
        @Value("${metadata.motd}") String welcome) {
        this.restService = restService;
        this.userLookupService = userLookupService;
        this.logoutService = logoutService;
        this.validateService = validateService;
        this.system = system;
        this.link = link;
        this.welcome = welcome;
    }

    @PreAuthorize("hasPermission(#vaultId, 'VAULT', 'CAN_TRANSFER_VAULT_OWNERSHIP') or hasPermission(#vaultId, 'GROUP_VAULT', 'TRANSFER_SCHOOL_VAULT_OWNERSHIP')")
    @PostMapping(value = "/vaults/{vaultid}/data-owner/update")
    public ResponseEntity transferOwnership(
            @PathVariable("vaultid") String vaultId,
            @Valid VaultTransferRequest request) {

        VaultInfo vault = restService.getVault(vaultId);
        if (vault == null) {
            throw new EntityNotFoundException(Vault.class, vaultId);
        }

        String vaultOwner = vault.getUserID();

        if (!request.isOrphaning()) {
            User newOwner = restService.getUser(request.user);
            if (newOwner == null) {
                return ResponseEntity.status(422).body("Could not find user with ID=" + request.user);
            }

            boolean hasVaultRole = restService.getRoleAssignmentsForUser(request.user)
                    .stream()
                    .anyMatch(role -> role.getVaultId() != null && role.getVaultId().equals(vaultId));

            String userId = vault.getUserID();
            // DAS 20210409 If the we are changing owner to a new user who isn't the same owner
            // and he has an existing role remove it
            if (hasVaultRole && (userId == null || ! userId.equals(request.user))) {
                // delete the users existing vault role
                restService.getRoleAssignmentsForUser(request.user).stream()
                        .filter(roleAssignment -> vault.getID().equals(roleAssignment.getVaultId()))
                        .findFirst()
                        .ifPresent(roleAssignment -> restService.deleteRoleAssignment(roleAssignment.getId()));
            }

            if (userId != null && userId.equals(request.user)) {
                return ResponseEntity.status(422).body("Cannot transfer ownership to the current owner");
            }

            // if currently orphaned can't give new role
            if (vaultOwner == null && request.assigningRole) {
                return ResponseEntity.status(422).body("Cannot assign role to the previous owner");
            }



            logoutService.logoutUser(newOwner.getID());
        } else {
            // if currently orphaned can't orphan
            if (vaultOwner == null) {
                return ResponseEntity.status(422).body("Cannot orphan an vault that is already orphaned");
            }
        }
        TransferVault transfer = new TransferVault();
        transfer.setUserId(request.user);
        transfer.setRoleId(request.role);
        transfer.setChangingRoles(request.assigningRole);
        transfer.setOrphaning(request.orphaning);

        restService.transferVault(vaultId, transfer);

        if (vaultOwner != null) {
            logoutService.logoutUser(vaultOwner);
        }

        if (request.assigningRole) {
            logoutService.logoutUser(request.user);
        }

        return ResponseEntity.ok().build();
    }


    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public String getVaultsListing(ModelMap model, Principal principal) {
        logger.debug("Getting the current vaults");
        VaultInfo currentVaults[] = restService.getVaultsListing();
        VaultInfo pendingVaults[] = restService.getPendingVaultsListing();
        //VaultInfo pendingVaults[] = null;
        // go to vault list or vault create if no current / pending vaults
        if ((currentVaults != null && currentVaults.length > 0)
                || (pendingVaults != null && pendingVaults.length > 0)) {
            logger.debug("Current vaults: " + currentVaults.length);
            model.addAttribute("vaults", currentVaults);
            model.addAttribute("pendingVaults", pendingVaults);

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

        //return this.createVault(model);
        return this.buildVault(model, principal);
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID, Principal principal) {
        VaultInfo vault = restService.getVault(vaultID);

        if (!canAccessVault(vault, principal)) {
            logger.info("getVault no permission.");
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

        List<RoleAssignment> roleAssignmentsForUser = restService.getRoleAssignmentsForUser(principal.getName());
        List<RoleModel> roles = restService.getVaultRoles();
        List<RoleModel> validRoles = RoleUtils.getAssignableRoles(roleAssignmentsForUser, roles);

        model.addAttribute("vault", vault);
        model.addAttribute("roles", validRoles);
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

        EventInfo[] roleEvents = restService.getVaultsRoleEvents(vaultID);
        model.addAttribute("roleEvents", roleEvents);

        // todo: Get all the review history

        ReviewInfo[] reviewInfos = restService.getReviewsListing(vaultID);
        List<VaultReviewModel> vaultReviewModels = new ArrayList<VaultReviewModel>();

        for (ReviewInfo reviewInfo : reviewInfos) {

            VaultReview currentReview = restService.getVaultReview(reviewInfo.getVaultReviewId());
            VaultReviewModel vaultReviewModel = new VaultReviewModel(currentReview);

            List<DepositReviewModel> depositReviewModels = new ArrayList<>();
            for (int i = 0; i < reviewInfo.getDepositIds().size(); i++) {
                DepositInfo depositInfo = restService.getDeposit(reviewInfo.getDepositIds().get(i));
                DepositReview depositReview = restService.getDepositReview(reviewInfo.getDepositReviewIds().get(i));
                DepositReviewModel drm = new DepositReviewModel();

                // Set DepositReview stuff
                drm.setDepositReviewId(depositReview.getId());
                drm.setDeleteStatus(depositReview.getDeleteStatus());
                drm.setComment(depositReview.getComment());

                // Set Deposit stuff
                drm.setDepositId(depositInfo.getID());
                drm.setName(depositInfo.getName());
                drm.setStatusName(depositInfo.getStatus().name());
                drm.setCreationTime(depositInfo.getCreationTime());

                depositReviewModels.add(drm);
            }

            vaultReviewModel.setDepositReviewModels(depositReviewModels);

            vaultReviewModels.add(vaultReviewModel);
        }

        VaultReviewHistoryModel vrhm = new VaultReviewHistoryModel();
        vrhm.setVaultReviewModels(vaultReviewModels);

        model.addAttribute("vrhm", vrhm);

        return "vaults/vault";
    }

    private boolean canAccessVault(VaultInfo vault, Principal principal) {
        return canAccessVault(vault, principal, false);
    }

    private boolean canAccessPendingVault(VaultInfo vault, Principal principal) {
        return canAccessVault(vault, principal, true);
    }

    private boolean canAccessVault(VaultInfo vault, Principal principal, Boolean pending) {
        List<RoleAssignment> roleAssignmentsForUser = restService.getRoleAssignmentsForUser(principal.getName());
        if (pending) {
            return roleAssignmentsForUser.stream().anyMatch(roleAssignment ->
                    RoleUtils.isISAdmin(roleAssignment)
                            || RoleUtils.isRoleInPendingVault(roleAssignment, vault.getID())
                            || (RoleUtils.isRoleInSchool(roleAssignment, vault.getGroupID()) && RoleUtils.hasPermission(roleAssignment, Permission.CAN_MANAGE_VAULTS)));
        } else {
            return roleAssignmentsForUser.stream().anyMatch(roleAssignment ->
                    RoleUtils.isISAdmin(roleAssignment)
                            || RoleUtils.isRoleInVault(roleAssignment, vault.getID())
                            || (RoleUtils.isRoleInSchool(roleAssignment, vault.getGroupID()) && RoleUtils.hasPermission(roleAssignment, Permission.CAN_MANAGE_VAULTS)));
        }
    }
    @RequestMapping(value = "/vaults/{vaultid}/{userid}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("vaultid") String vaultID, @PathVariable("userid") String userID) {
    	model.addAttribute("vaults", restService.getVaultsListingAll(userID));
    	        
        return "vaults/userVaults";
    }

    @RequestMapping(value = "/pendingVaults/{vaultid}", method = RequestMethod.GET)
    public String getPendingVault(ModelMap model, @PathVariable("vaultid") String vaultID, Principal principal) {
        VaultInfo vault = restService.getPendingVault(vaultID);
        logger.info("Passed in id: '" + vaultID);

        if (!canAccessPendingVault(vault, principal)) {
            throw new ForbiddenException();
        }

        CreateVault cv = vault.convertToCreate();
        model.addAttribute("vault", cv);
        RetentionPolicy[] policies = restService.getRetentionPolicyListing();
        model.addAttribute("policies", policies);

        Group[] groups = restService.getGroups();
        model.addAttribute("groups", groups);

        return "vaults/newCreatePrototype";
    }

    @RequestMapping(value = "/vaults/buildsteps", method = RequestMethod.GET)
    public String buildVault(ModelMap model, Principal principal) {

        // pass the view an empty Vault since the form expects it if nothing has been saved so far

        CreateVault vault = new CreateVault();
        vault.setIsOwner(true);
        vault.setLoggedInAs(principal.getName());
        model.addAttribute("vault", vault);
        String defaultReviewDate = validateService.getDefaultReviewDate();
        vault.setReviewDate(defaultReviewDate);

        RetentionPolicy[] policies = restService.getRetentionPolicyListing();
        model.addAttribute("policies", policies);

        Group[] groups = restService.getGroups();
        model.addAttribute("groups", groups);

        return "vaults/newCreatePrototype";
    }

   /* // Process the completed 'create new vault' page
    @RequestMapping(value = "/vaults/create", method = RequestMethod.POST)
    public String addVault(@ModelAttribute CreateVault vault, ModelMap model) {
        VaultInfo newVault = restService.addVault(vault);
        String vaultUrl = "/vaults/" + newVault.getID() + "/";
        return "redirect:" + vaultUrl;
    }*/
   @RequestMapping(value = "/vaults/confirmed", method = RequestMethod.GET)
    public String confirmPendingVault() {
       return "vaults/confirmed";

    }
    // Process the completed 'create new vault' page
    @RequestMapping(value = "/vaults/stepCreate", method = RequestMethod.POST)
    public String addVault(@ModelAttribute CreateVault vault, ModelMap model, @RequestParam String action, Principal principal) {



        // if the confirm button has been clicked save what we have if everything isn't already saved
        // and display the summary
        logger.info("Action is:'" + action + "'");
        logger.info("PendingID is:'" + vault.getPendingID() + "'");
        String buildUrl = "/vaults/buildsteps/";
        VaultInfo newVault = null;
        if ("Save".equals(action)) {
            // if the save button has been clicked just save what we have and go back to the same page of the form
            // already have something saved update if not new save
            logger.info("Save button clicked");
            String result = userLookupService.checkNewRolesUserExists(vault, buildUrl);
            if (result != null && ! result.isEmpty()) {
                return result;
            }

            if (vault.getPendingID() == null || vault.getPendingID().isEmpty()) {
                newVault = restService.addPendingVault(vault);
            } else {
                newVault = restService.updatePendingVault(vault);
            }
            //buildUrl = buildUrl + newVault.getID();
            String vaultUrl = "/pendingVaults/" + newVault.getID() + "/";
            return "redirect:" + vaultUrl;
        } else if ("Confirm".equals(action)) {
            // if the confirm button has been clicked save what we have if everything isn't already saved
            // and display the summary
            logger.info("Confirm button clicked");

            // confirm we have enough data entered to proceed
            List<String> validateResult = validateService.validate(vault, principal.getName());
            if (validateResult != null && !validateResult.isEmpty()) {
                // return to create vault page with the entered data and error message(s)
                // (just redirects to empty page without error atm
                logger.info("Validation Errors");
                for (String error : validateResult) {
                    logger.info(error);
                }
                model.addAttribute("errors", validateResult);
                model.addAttribute("vault", vault);
                RetentionPolicy[] policies = restService.getRetentionPolicyListing();
                model.addAttribute("policies", policies);

                Group[] groups = restService.getGroups();
                model.addAttribute("groups", groups);
                return "vaults/newCreatePrototype";
            }

            String userResult = userLookupService.checkNewRolesUserExists(vault, buildUrl);
            if (userResult != null && ! userResult.isEmpty()) {
                return userResult;
            }
            vault.setConfirmed(true);
            if (vault.getPendingID() == null || vault.getPendingID().isEmpty()) {
                newVault = restService.addPendingVault(vault);
            } else {
                newVault = restService.updatePendingVault(vault);
            }

            return "redirect:" + "/vaults/confirmed";
        } else if ("Validate".equals(action)) {
            // this will be the code that moves from pending vault to validated vault
            // when an admin gives the ok

            // add the new vault
            // remove all the pending stuff for the vault;
            //String vaultUrl = "/pendingVaults/" + newVault.getID() + "/";
            //VaultInfo newVault = restService.addVault(vault);
            return "";
        } else {
            logger.info("Invalid button clicked");
            return "redirect:" + buildUrl;
        }

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

    @RequestMapping(value = "/vaults/{vaultid}/updateVaultName", method = RequestMethod.POST)
    public String updateVaultName(ModelMap model,
                                         @PathVariable("vaultid") String vaultID,
                                         @RequestParam("name") String name) {
        VaultInfo vault = restService.updateVaultName(vaultID, name);
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
    
    @RequestMapping(value = "/vaults/isuun/{uun}", method = RequestMethod.GET)
    @ResponseBody
    public String isUUN(@PathVariable("uun") String uun) {
        boolean result = userLookupService.isUUN(uun);
        Gson gson = new Gson();
        return gson.toJson(result);
    }

//    private String checkUserList(List<String> list, String errorUrl) {
//        String retVal = "";
//
//        for (String li : list) {
//            String result = this.checkUser(li, errorUrl);
//            if (result != null && ! result.isEmpty()) {
//                return result;
//            }
//        }
//
//        return retVal;
//
//    }
//
//    private String checkUser(String user, String errorUrl) {
//        String retVal = "";
//        // exclude the empty dummy user
//        if (user != null && ! user.equals("")) {
//            try {
//                userLookupService.ensureUserExists(user);
//            } catch (InvalidUunException e) {
//                    /* @TODO: need to go back to the entered values plus an error message about the problem user.
//                    This will do none of that but will return to an empty from
//                    Would be good if we checked all of them before erroring too
//                    */
//                return errorUrl;
//            }
//        }
//        return retVal;
//    }
//
//    private String checkNewRolesUserExists(CreateVault vault, String buildUrl) {
//        String retVal = "";
//
//        // foreach depositor
//        List<String> deps = vault.getDepositors();
//        String depResult = this.checkUserList(deps, buildUrl);
//
//        if (depResult != null && ! depResult.isEmpty()) {
//            return "redirect:" + depResult;
//        }
//        // foreach ndm
//        List<String> ndms = vault.getNominatedDataManagers();
//        String ndmResult = this.checkUserList(ndms, buildUrl);
//
//        if (ndmResult != null && ! ndmResult.isEmpty()) {
//            return "redirect:" + ndmResult;
//        }
//
//        // foreach data creator
//        List<String> creators = vault.getDataCreators();
//        String creatorResult = this.checkUserList(creators, buildUrl);
//
//        if (creatorResult != null && ! creatorResult.isEmpty()) {
//            return "redirect:" + creatorResult;
//        }
//        // owner
//        String owner = vault.getVaultOwner();
//        String ownerResult = this.checkUser(owner, buildUrl);
//
//        if (ownerResult != null && !ownerResult.isEmpty()) {
//            return "redirect:" + ownerResult;
//        }
//
//        // contact
//        String contact = vault.getContactPerson();
//        String contactResult = this.checkUser(contact, buildUrl);
//
//        if (contactResult != null && !contactResult.isEmpty()) {
//            return "redirect:" + contactResult;
//        }
//
//        return retVal;
//    }

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

        @NotEmpty(message = "Please specify a transfer reason")
        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public boolean isAssigningRole() {
            return assigningRole;
        }

        @AssertTrue(message = "Please specify a user")
        public boolean isUserSelectionValid() {
            return orphaning || !Strings.isNullOrEmpty(user);
        }

        @AssertTrue(message = "Please specify a role")
        public boolean isRoleSelectionValid() {
            return !assigningRole || role != null;
        }

    }
}


