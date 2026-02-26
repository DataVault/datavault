package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.*;

import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@ConditionalOnBean(RestService.class)
public class AdminReviewsController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminReviewsController.class);
    public static final String ACTION_CANCEL = "Cancel";
    public static final String ACTION_SUBMIT = "Submit";
    public static final String ACTION_SAVE = "Save";

    private final RestService restService;
    private final Clock clock;

    @Autowired
    public AdminReviewsController(RestService restService, Clock clock) {
        this.restService = restService;
        this.clock = clock;
    }

    @GetMapping("/admin/reviews")
    public String getVaultsForReview(ModelMap model) {

        VaultsData vaultsData = restService.getVaultsForReview();
        List<VaultInfo> vaultsInfo = vaultsData.getData();

        model.addAttribute("vaults", vaultsInfo);

        return "admin/reviews/index";
    }

    // Return a review page
    @GetMapping("/admin/vaults/{vaultid}/reviews")
    public String showReview(ModelMap model, 
                             @PathVariable("vaultid") String vaultID, 
                             @RequestParam(value = "error", required = false) String error) {

        if (error != null) {
            if (error.equals("reviewdate")) {
                model.addAttribute("error", "If some deposits are to be retained then a new Review Date must be entered");
            }
        }

        VaultInfo vault = restService.getVault(vaultID);
        model.addAttribute("vault", vault);

        List<RoleAssignment> roleAssignmentsForVault = restService.getRoleAssignmentsForVault(vaultID);

        List<RoleAssignment> dataManagers = roleAssignmentsForVault.stream()
                .filter(Objects::nonNull)
                .filter(roleAssignment -> RoleUtils.isRoleOfName(roleAssignment, "Nominated Data Manager"))
                .collect(Collectors.toList());
        model.addAttribute("dataManagers", dataManagers);

        roleAssignmentsForVault.stream()
                .filter(Objects::nonNull)
                .filter(RoleUtils::isDataOwner)
                .findFirst()
                .ifPresent(roleAssignment -> model.addAttribute("dataOwner", roleAssignment));

        model.addAttribute("createRetentionPolicy", restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));

        ReviewInfo reviewInfo = restService.getCurrentReview(vaultID);
        if (reviewInfo == null) {
            // There isn't a current review, so create one.
            reviewInfo = restService.createCurrentReview(vaultID);
        }
        VaultReview currentReview = restService.getVaultReview(reviewInfo.getVaultReviewId());
        VaultReviewModel vaultReviewModel = new VaultReviewModel(currentReview);

        int sizeDepositReviewIds = reviewInfo.getDepositReviewIds().size();
        int sizeDepositIds = reviewInfo.getDepositIds().size();
        
        Assert.isTrue(sizeDepositIds >= sizeDepositReviewIds, "size of depositIds not >= to size of depositReviewIds");

        List<DepositReviewModel> depositReviewModels = new ArrayList<>();
        for (int i = 0; i < reviewInfo.getDepositReviewIds().size(); i++) {
            String depositReviewId = reviewInfo.getDepositReviewIds().get(i);
            String depositId = reviewInfo.getDepositIds().get(i);
            DepositInfo depositInfo = restService.getDeposit(depositId);
            DepositReview depositReview = restService.getDepositReview(depositReviewId);
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

            depositReviewModels.sort(Comparator.comparing(DepositReviewModel::getCreationTime));
            depositReviewModels.add(drm);
        }

        vaultReviewModel.setDepositReviewModels(depositReviewModels);

        model.addAttribute("createRetentionPolicy", new CreateRetentionPolicy());
        model.addAttribute("vaultReviewModel", vaultReviewModel);

        return "admin/reviews/create";
    }



    // Process the completed review page
    @PostMapping("/admin/vaults/{vaultid}/reviews/{reviewid}")
    public String processReview(@ModelAttribute VaultReviewModel vaultReviewModel,
                                ModelMap model,
                                RedirectAttributes redirectAttributes,
                                @PathVariable("vaultid") String vaultID,
                                @PathVariable("reviewid") String reviewID,
                                @RequestParam String action) {

        // ??? the valid actions Submit, Cancel and Save
        
        // Note - The ModelAttributes made available here are not the same objects as those passed to the View,
        // they only contain the values entered on screen. With that in mind, fetch the original objects again and
        // update them appropriately.

        if (ACTION_CANCEL.equals(action)) {
            return "redirect:/admin/reviews";
        }

        if (ACTION_SUBMIT.equals(action)) {
            // Throw back an error if a new review date has not been entered but some deposits are being retained.
            if (vaultReviewModel.getNewReviewDate() == null) {
                if (vaultReviewModel.getDepositReviewModels() != null) {
                    for (DepositReviewModel drm : vaultReviewModel.getDepositReviewModels()) {
                        if (drm == null) {
                            continue;
                        }
                        if (drm.getDeleteStatus() == DepositReviewDeleteStatus.RETAIN) {
                            redirectAttributes.addAttribute("error", "reviewdate");
                            return "redirect:/admin/vaults/" + vaultID + "/reviews";
                        }
                    }
                }
            }
        }

        LocalDateTime now = LocalDateTime.now(clock);
        
        // Get the old stuff and update it
        VaultReview originalVaultReview = restService.getVaultReview(reviewID);
        originalVaultReview.setNewReviewDate(vaultReviewModel.getNewReviewDate());
        originalVaultReview.setComment(vaultReviewModel.getComment());

        if (ACTION_SUBMIT.equals(action)) {
            originalVaultReview.setActionedDate(now);

            // Save the old review date in the VaultReview
            VaultInfo vault = restService.getVault(vaultID);
            originalVaultReview.setOldReviewDate(vault.getReviewDate());

            // And update the review date in the Vault if a new one has been entered
            if (vaultReviewModel.getNewReviewDate() != null) {
                // Update the review date in the Vault object.
                LOG.info("Editing Review Date for Vault id " + vaultID + " with new Review Date " + vaultReviewModel.getNewReviewDate());
                restService.updateVaultReviewDate(vaultID, vaultReviewModel.getNewReviewDate());
            }
        }

        LOG.info("Editing Vault Review id " + originalVaultReview.getId());
        restService.editVaultReview(originalVaultReview);

        if (vaultReviewModel.getDepositReviewModels() != null) {
            for (DepositReviewModel drm : vaultReviewModel.getDepositReviewModels()) {
                DepositReview originalDepositReview = restService.getDepositReview(drm.getDepositReviewId());
                originalDepositReview.setDeleteStatus(drm.getDeleteStatus());
                originalDepositReview.setComment(drm.getComment());

                if (ACTION_SUBMIT.equals(action)) {
                    if (drm.getDeleteStatus() == DepositReviewDeleteStatus.NOW) {
                        // Stand back everyone!
                        LOG.info("Deleting deposit id " + drm.getDepositId());
                        originalDepositReview.setActionedDate(now);
                        restService.deleteDeposit(drm.getDepositId());
                    } else if (drm.getDeleteStatus() == DepositReviewDeleteStatus.RETAIN) {
                        LOG.info("Retaining deposit id " + drm.getDepositId());
                        originalDepositReview.setActionedDate(now);
                    } // Otherwise it has been flagged to be deleted later.
                }

                LOG.info("Editing Deposit Review id " + originalDepositReview.getId());
                restService.editDepositReview(originalDepositReview);
            }
        }

        return "redirect:/admin/reviews";
    }

}


