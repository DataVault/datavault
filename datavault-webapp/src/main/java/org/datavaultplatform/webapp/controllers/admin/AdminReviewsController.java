package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;

import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.DepositReviewModelList;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
    public String showReview(ModelMap model, @PathVariable("vaultid") String vaultID) throws Exception {

        VaultInfo vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);
        model.addAttribute(restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));

        // In fact this creates a current Review if none exists.
        ReviewInfo reviewInfo = restService.getCurrentReview(vaultID);

        VaultReview currentReview = restService.getVaultReview(reviewInfo.getVaultReviewId());
        VaultReviewModel vaultReviewModel = new VaultReviewModel(currentReview);

        DepositReviewModelList drml = new DepositReviewModelList();
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

        drml.setDepositReviewModels(depositReviewModels);

        model.addAttribute("currentReview", currentReview);
        model.addAttribute("vaultReviewModel", vaultReviewModel);
        model.addAttribute("drml", drml);

        return "admin/reviews/create";
    }


    // Process the completed review page
    @RequestMapping(value = "/admin/vaults/{vaultid}/reviews/{reviewid}", method = RequestMethod.POST)
    public String processReview(@ModelAttribute VaultReviewModel currentReview, @ModelAttribute DepositReviewModelList drml, ModelMap model, @PathVariable("vaultid") String vaultID, @PathVariable("reviewid") String reviewID, @RequestParam String action) throws Exception {

        // Note - The ModelAttributes made available here are not the same objects as those passed to the View,
        // they only contain the values entered on screen. With that in mind, fetch the original objects again and
        // update them appropriately.

        if ("Cancel".equals(action)) {
            System.out.println("Cancel!!!!!!!!!!");
            return "redirect:/admin/reviews";
        }

        // todo: We need to throw back an error if the review date has not been changed
        // but some deposits are being retained.

        VaultReview originalReview = restService.getVaultReview(reviewID);

        originalReview.setNewReviewDate(currentReview.StringToDate(currentReview.getNewReviewDate()));
        originalReview.setComment(currentReview.getComment());

        if ("Action".equals(action)) {
            originalReview.setActionedDate(new Date());
        }

        logger.info("Editing Vault Review id " + originalReview.getId());
        restService.editVaultReview(originalReview);

        for (DepositReviewModel drm : drml.getDepositReviewModels()) {
            DepositReview originalDepositReview = restService.getDepositReview(drm.getDepositReviewId());
            originalDepositReview.setToBeDeleted(drm.isToBeDeleted());
            originalDepositReview.setComment(drm.getComment());

            if ("Action".equals(action)) {
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


