package org.datavaultplatform.webapp.controllers.admin;


import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.model.*;

import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.response.*;
import org.datavaultplatform.common.util.PageDTOVaultInfo;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.common.util.Utils;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.datavaultplatform.common.util.Utils.*;

@SuppressWarnings("CodeBlock2Expr")
@Controller
@ConditionalOnBean(RestService.class)
@Validated
public class AdminReviewsController implements AdminReviewsControllerApi {

    public static final Comparator<VaultInfo> BY_REVIEW_DATE_ASC = Comparator.comparing(
            VaultInfo::getReviewDate,
            Comparator.nullsLast(Comparator.naturalOrder())
    );

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

    @Override
    @GetMapping(value = "/admin/reviews", produces = MediaType.TEXT_HTML_VALUE)
    public String getVaultsForReview(ModelMap model) {
        VaultsData vaultsData = restService.getVaultsForReview();
        List<VaultInfo> vaultsInfo = Utils.getSafeStream(vaultsData.getData()).sorted(BY_REVIEW_DATE_ASC).toList();
        vaultsInfo.forEach(this::addVaultReviewStatusInfo);
        
        model.addAttribute("vaults", vaultsInfo);
        return "admin/reviews/index";
    }

    void addVaultReviewStatusInfo(VaultInfo info) {
        VaultReviewStatusInfo statusInfo = restService.getVaultReviewStatusInfo(info.getID());
        info.setVaultReviewStatusInfo(statusInfo);
    }

    // Return a review page
    @Override
    @GetMapping(value = "/admin/vaults/{vaultId}/reviews", produces = MediaType.TEXT_HTML_VALUE)
    public String showReview(ModelMap model,
                             @PathVariable String vaultId,
                             @RequestParam(value = "error", required = false) String error) {

        if (error != null) {
            if (error.equals("reviewdate")) {
                model.addAttribute("error", "If some deposits are to be retained then a next Review Date must be entered");
            }
        }

        VaultInfo vault = restService.getVault(vaultId);
        model.addAttribute("vault", vault);

        List<RoleAssignment> roleAssignmentsForVault = restService.getRoleAssignmentsForVault(vaultId);

        List<RoleAssignment> dataManagers = Utils.getSafeStream(roleAssignmentsForVault)
                .filter(Objects::nonNull)
                .filter(roleAssignment -> RoleUtils.isRoleOfName(roleAssignment, "Nominated Data Manager"))
                .toList();
        model.addAttribute("dataManagers", dataManagers);

        roleAssignmentsForVault.stream()
                .filter(Objects::nonNull)
                .filter(RoleUtils::isDataOwner)
                .findFirst()
                .ifPresent(roleAssignment -> model.addAttribute("dataOwner", roleAssignment));

        CreateRetentionPolicy retentionPolicy = restService.getRetentionPolicy(vault.getPolicyID());
        model.addAttribute("createRetentionPolicy", retentionPolicy);
        model.addAttribute(restService.getGroup(vault.getGroupID()));

        ReviewInfo reviewInfo = restService.getCurrentReview(vaultId);
        if (reviewInfo == null) {
            // There isn't a current review, so create one.
            reviewInfo = restService.createCurrentReview(vaultId);
        }
        
        // makes sure the VaultReview has a DepositReview for each of the Vault's Deposits
        restService.refreshUnderwayVaultReview(vaultId);
        
        VaultReview currentReview = restService.getVaultReview(reviewInfo.getVaultReviewId());
        VaultReviewModel vaultReviewModel = new VaultReviewModel(currentReview, vault.getReviewDate());

        int sizeDepositReviewIds = reviewInfo.getDepositReviewIds().size();
        int sizeDepositIds = reviewInfo.getDepositIds().size();
        
        Assert.isTrue(sizeDepositIds >= sizeDepositReviewIds, "size of depositIds not >= to size of depositReviewIds");

        vaultReviewModel.setDepositReviewModels(buildDepositReviewModels(reviewInfo));

        model.addAttribute("createRetentionPolicy", new CreateRetentionPolicy());
        model.addAttribute("vaultReviewModel", vaultReviewModel);

        return "admin/reviews/create";
    }

    private List<DepositReviewModel> buildDepositReviewModels(ReviewInfo reviewInfo) {
        Assert.notNull(reviewInfo, "ReviewInfo must not be null");

        List<DepositReviewModel> result = new ArrayList<>();
        for (int i = 0; i < reviewInfo.getDepositReviewIds().size(); i++) {

            String depositReviewId = reviewInfo.getDepositReviewIds().get(i);
            String depositId = reviewInfo.getDepositIds().get(i);

            var drm = buildDepositReviewModel(depositReviewId, depositId);
            result.add(drm);
        }

        result.sort(DepositReviewModel.BY_DEPOSIT_CREATION_TIME);
        return result;
    }

    private DepositReviewModel buildDepositReviewModel(String depositReviewId, String depositId) {
        DepositReviewModel drm = new DepositReviewModel();

        DepositInfo depositInfo = restService.getDeposit(depositId);
        DepositReview depositReview = restService.getDepositReview(depositReviewId);

        drm.updateFromDepositReviewAndDepositInfo(depositReview, depositInfo);
        return drm;
    }

    // Process the completed review page
    @Override
    @PostMapping(value = "/admin/vaults/{vaultId}/reviews/{reviewId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String processReview(@ModelAttribute VaultReviewModel vaultReviewModel,
                                RedirectAttributes redirectAttributes,
                                @PathVariable String vaultId,
                                @PathVariable String reviewId,
                                @RequestParam String action) {

        Assert.notNull(vaultReviewModel, "VaultReviewModel must not be null");
        
        // Note - The ModelAttributes made available here are not the same objects as those passed to the View,
        // they only contain the values entered on screen. With that in mind, fetch the original objects again and
        // update them appropriately.

        if (ACTION_CANCEL.equals(action)) {
            return "redirect:/admin/reviews";
        }

        if (ACTION_SUBMIT.equals(action)) {
            if (!validateNextReviewDate(vaultReviewModel, redirectAttributes)) {
                return "redirect:/admin/vaults/" + vaultId + "/reviews";
            }
        }

        LocalDateTime now = LocalDateTime.now(clock);

        VaultReview originalVaultReview = restService.getVaultReview(reviewId);

        updateVaultReviewAndVault(originalVaultReview, vaultReviewModel, vaultId, now, action);

        List<DepositReviewModel> depositReviewModels = vaultReviewModel.getDepositReviewModels();

        //noinspection CodeBlock2Expr
        getSafeStream(depositReviewModels).forEach(drm -> {
            processSingleDepositReview(drm, now, action);
        });

        return "redirect:/admin/reviews";
    }

    /**
     * You cannot have a DepositReviewModel:RETAIN without a non-null nextReviewDate
     * @param vaultReviewModel
     * @param redirectAttributes
     * @return false if there's no NEW REVIEW DATE and at least 1 DRM with "retain".
     */
    protected boolean validateNextReviewDate(VaultReviewModel vaultReviewModel, RedirectAttributes redirectAttributes) {
        
        if (vaultReviewModel.getNextReviewDate() != null) {
            return true;
        }

        List<DepositReviewModel> depositReviewModels = vaultReviewModel.getDepositReviewModels();
        if (depositReviewModels == null) {
            return true;
        }
        for (DepositReviewModel drm : depositReviewModels) {
            if (drm == null) {
                continue;
            }
            if (drm.getDeleteStatus() == DepositReviewDeleteStatus.RETAIN) {
                redirectAttributes.addAttribute("error", "reviewdate");
                return false;
            }
        }
        return true;
    }


    protected void updateVaultReviewAndVault(VaultReview originalVaultReview, VaultReviewModel vrm, String vaultID, LocalDateTime now, String action) {
        Assert.notNull(originalVaultReview, "originalVaultReview cannot be null");
        Assert.notNull(vrm, "vaultReviewModel cannot be null");

        // WHEN YOU Save or Submit a VaultReivew - the comment is saved
        originalVaultReview.setComment(vrm.getComment());

        if (ACTION_SUBMIT.equals(action)) {
            originalVaultReview.setActionedDate(now);

            Assert.isTrue(StringUtils.isNotBlank(vaultID), "The vaultId cannot be blank");
            VaultInfo vault = restService.getVault(vaultID);

            // copy the current 'Vault.reviewDate' into 'originalVaultReview.oldReviewDate'
            originalVaultReview.setOldReviewDate(vault.getReviewDate());

            // the nextReviewDate is the used to update the Vault.reviewDate FOR THE NEXT REVIEW - not this one.
            LocalDate nextReviewDate = vrm.getNextReviewDate();
            if (nextReviewDate != null) {
                LOG.info("Editing Review Date for Vault id {} with new Review Date {}", vaultID, nextReviewDate);
                // the nextReviewDate is the used to update the Vault.reviewDate FOR THE NEXT REVIEW - not this one.
                restService.updateReviewDateOfVault(vaultID, nextReviewDate);
            }
        }

        LOG.info("Editing Vault Review id {}", originalVaultReview.getId());
        restService.editVaultReview(originalVaultReview);
    }

    protected void processSingleDepositReview(DepositReviewModel drm, LocalDateTime now, String action) {
        Assert.notNull(drm, "The depositReviewModel cannot be null");
        DepositReview originalDepositReview = restService.getDepositReview(drm.getDepositReviewId());
        originalDepositReview.setDeleteStatus(drm.getDeleteStatus());
        originalDepositReview.setComment(drm.getComment());

        if (ACTION_SUBMIT.equals(action)) {
            if (drm.getDeleteStatus() == DepositReviewDeleteStatus.NOW) {
                originalDepositReview.setActionedDate(now);
                LOG.info("Deleting deposit id {}", drm.getDepositId());
                restService.deleteDeposit(drm.getDepositId());
            } else if (drm.getDeleteStatus() == DepositReviewDeleteStatus.RETAIN) {
                originalDepositReview.setActionedDate(now);
                LOG.info("Retaining deposit id {}", drm.getDepositId());
            }
        }
        LOG.info("Editing Deposit Review id {}", originalDepositReview.getId());
        restService.editDepositReview(originalDepositReview);
    }
    
    @GetMapping("/admin/reviews/vaults/search")
    @ResponseBody
    @Override
    public PageDTOVaultInfo searchVaultsForReview(
            @RequestParam("q")
            String partialVaultName) {

        PageDTOVaultInfo result = restService.searchVaultsForReview(partialVaultName);
        result.getContent().sort(BY_REVIEW_DATE_ASC);
        result.getContent().stream().filter(Objects::nonNull).forEach(this::addVaultReviewStatusInfo);
        return result;
    }   
    
}


