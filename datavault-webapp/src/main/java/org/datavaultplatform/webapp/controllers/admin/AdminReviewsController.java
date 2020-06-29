package org.datavaultplatform.webapp.controllers.admin;


import org.apache.commons.lang.RandomStringUtils;
import org.datavaultplatform.common.model.*;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class AdminReviewsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminReviewsController.class);

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }


    @RequestMapping(value = "/admin/reviews", method = RequestMethod.GET)
    public String getVaultsForReview(ModelMap model) throws Exception {

        VaultsData vaultsData = restService.getVaultsForReview();
        List<VaultInfo> vaultsInfo = vaultsData.getData();

        model.addAttribute("vaults", vaultsInfo);

        return "admin/reviews/index";
    }

    // Return a review page
    @RequestMapping(value = "/admin/vaults/{vaultid}/reviews", method = RequestMethod.GET)
    public String showReview(ModelMap model, @PathVariable("vaultid") String vaultID, @RequestParam(value = "error", required = false) String error) throws Exception {

        if (error != null) {
            if (error.equals("reviewdate")) {
                model.addAttribute("error", "If some deposits are to be retained then a new Review Date must be entered");
            }
        }

        VaultInfo vault = restService.getVault(vaultID);
        model.addAttribute("vault", vault);

        List<RoleAssignment> roleAssignmentsForVault = restService.getRoleAssignmentsForVault(vaultID);

        List<RoleAssignment> dataManagers = roleAssignmentsForVault.stream()
                .filter(roleAssignment -> RoleUtils.isRoleOfName(roleAssignment, "Nominated Data Manager"))
                .collect(Collectors.toList());
        model.addAttribute("dataManagers", dataManagers);

        roleAssignmentsForVault.stream()
                .filter(RoleUtils::isDataOwner)
                .findFirst()
                .ifPresent(roleAssignment -> model.addAttribute("dataOwner", roleAssignment));

        model.addAttribute("createRetentionPolicy", restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));

        ReviewInfo reviewInfo = restService.getCurrentReview(vaultID);
        if (reviewInfo == null) {
            // There isn't a current review so create one.
            reviewInfo = restService.createCurrentReview(vaultID);
        }

        VaultReview currentReview = restService.getVaultReview(reviewInfo.getVaultReviewId());
        VaultReviewModel vaultReviewModel = new VaultReviewModel(currentReview);

        List<DepositReviewModel> depositReviewModels = new ArrayList<>();
        for (int i = 0; i < reviewInfo.getDepositIds().size(); i++) {
            DepositInfo depositInfo = restService.getDeposit(reviewInfo.getDepositIds().get(i));
            DepositReview depositReview = restService.getDepositReview(reviewInfo.getDepositReviewIds().get(i));
            DepositReviewModel drm = new DepositReviewModel();

            // Set DepositReview stuff
            drm.setDepositReviewId(depositReview.getId());
            drm.setToBeDeleted(depositReview.isToBeDeleted());
            drm.setComment(depositReview.getComment());

            // Set Deposit stuff
            drm.setDepositId(depositInfo.getID());
            drm.setName(depositInfo.getName());
            drm.setStatusName(depositInfo.getStatus().name());
            drm.setCreationTime(depositInfo.getCreationTime());

            depositReviewModels.add(drm);
        }

        vaultReviewModel.setDepositReviewModels(depositReviewModels);

        model.addAttribute("vaultReviewModel", vaultReviewModel);

        return "admin/reviews/create";
    }


    // Process the completed review page
    @RequestMapping(value = "/admin/vaults/{vaultid}/reviews/{reviewid}", method = RequestMethod.POST)
    public String processReview(@ModelAttribute VaultReviewModel vaultReviewModel, ModelMap model, RedirectAttributes redirectAttributes, @PathVariable("vaultid") String vaultID, @PathVariable("reviewid") String reviewID, @RequestParam String action) throws Exception {

        // Note - The ModelAttributes made available here are not the same objects as those passed to the View,
        // they only contain the values entered on screen. With that in mind, fetch the original objects again and
        // update them appropriately.

        if ("Cancel".equals(action)) {
            return "redirect:/admin/reviews";
        }


        if ("Submit".equals(action)) {
            // Throw back an error if a new review date has not been entered but some deposits are being retained.
            if (vaultReviewModel.getNewReviewDate().isEmpty()) {
                for (DepositReviewModel drm : vaultReviewModel.getDepositReviewModels()) {
                    if (drm.isToBeDeleted() == false) {
                        redirectAttributes.addAttribute("error", "reviewdate");
                        return "redirect:/admin/vaults/" + vaultID + "/reviews";
                    }
                }
            } else {
                // Update the review date in the Vault object.
                logger.info("Editing Review Date for Vault id " + vaultID + " with new Review Date " + vaultReviewModel.getNewReviewDate());
                restService.updateVaultReviewDate(vaultID, vaultReviewModel.getNewReviewDate());
            }
        }

        VaultReview originalReview = restService.getVaultReview(reviewID);
        originalReview.setNewReviewDate(vaultReviewModel.StringToDate(vaultReviewModel.getNewReviewDate()));
        originalReview.setComment(vaultReviewModel.getComment());

        if ("Submit".equals(action)) {
            originalReview.setActionedDate(new Date());
        }

        logger.info("Editing Vault Review id " + originalReview.getId());
        restService.editVaultReview(originalReview);

        for (DepositReviewModel drm : vaultReviewModel.getDepositReviewModels()) {
            DepositReview originalDepositReview = restService.getDepositReview(drm.getDepositReviewId());
            originalDepositReview.setToBeDeleted(drm.isToBeDeleted());
            originalDepositReview.setComment(drm.getComment());

            if ("Submit".equals(action)) {
                originalDepositReview.setActionedDate(new Date());

                if (drm.isToBeDeleted()) {
                    // Stand back everyone!
                    logger.info("Deleting deposit id " + drm.getDepositId());
                    restService.deleteDeposit(drm.getDepositId());
                }
            }

            logger.info("Editing Deposit Review id " + originalDepositReview.getId());
            restService.editDepositReview(originalDepositReview);
        }

        return "redirect:/admin/reviews";
    }

}


