package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
        model.addAttribute("deposits", restService.getDepositsListing(vaultID));

        // Fetch any existing review records for this vault.
        VaultReview[] vaultReviews = restService.getReviewsListing(vaultID);

        // Initialse the currentReview to be empty as the view an VaultReview object even if one doesn't exist yet.
        System.out.println("Setting currentReview to new thing");
        VaultReview currentReview = new VaultReview();
        List<VaultReview> oldReviews = new ArrayList<VaultReview>();

        for (VaultReview vaultReview : vaultReviews) {
            if (vaultReview.getActionedDate() == null) {
                // We should be able to safely assume there is either zero or one such record.
                currentReview = vaultReview;
            } else {
                oldReviews.add(vaultReview);
            }
        }

        currentReview.setNewReviewDate(new Date());
        System.out.println("currentReview is " + currentReview);
        System.out.println("Id is " + currentReview.getId());
        System.out.println("newReviewDate is " + currentReview.getNewReviewDate());

        model.addAttribute("currentReview", currentReview);

        return "admin/reviews/create";
    }


    // Process the completed review page
    @RequestMapping(value = "/admin/vaults/{vaultid}/reviews/{reviewid}", method = RequestMethod.POST)
    public String processReview(ModelMap model, @PathVariable("vaultid") String vaultID, @RequestParam String action) throws Exception {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        VaultInfo vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);
        model.addAttribute(restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));
        model.addAttribute("deposits", restService.getDepositsListing(vaultID));

        // Fetch any exiting review records for this vault.
        VaultReview[] vaultReviews = restService.getReviewsListing(vaultID);

        VaultReview currentReview = null;
        List<VaultReview> oldReviews = new ArrayList<VaultReview>();

        for (VaultReview vaultReview : vaultReviews) {
            if (vaultReview.getActionedDate() == null) {
                // We should be able to safely assume there is either zero or one such record.
                currentReview = vaultReview;
            } else {
                oldReviews.add(vaultReview);
            }
        }

        model.addAttribute("currentReview", currentReview);
        

        return "admin/reviews/create";
    }

}


