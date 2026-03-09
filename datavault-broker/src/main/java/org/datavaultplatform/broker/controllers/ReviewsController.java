package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.broker.controllers.admin.AdminReviewsController;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
//@CrossOrigin
@Tag(name="reviews-controller", description = "Review functions")
public class ReviewsController {

    private final VaultsService vaultsService;
    private final VaultsReviewService vaultsReviewService;
    private final DepositsReviewService depositsReviewService;
    private final UsersService usersService;
    private final ClientsService clientsService;
    private final EventService eventService;

    @Autowired
    public ReviewsController(VaultsService vaultsService, VaultsReviewService vaultsReviewService,
        DepositsReviewService depositsReviewService, UsersService usersService,
        ClientsService clientsService, EventService eventService) {
        this.vaultsService = vaultsService;
        this.vaultsReviewService = vaultsReviewService;
        this.depositsReviewService = depositsReviewService;
        this.usersService = usersService;
        this.clientsService = clientsService;
        this.eventService = eventService;
    }

    @Operation(
            summary = "Gets a list of Reviews for a Vault",
            description = "Retrieves a list of all Reviews for a specific Vault."
    )
    @GetMapping(value = "/vaults/{vaultId}/vaultreviews", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReviewInfo> getVaultReviews(@RequestHeader(HEADER_USER_ID) String userId,
                                            @PathVariable String vaultId) throws Exception {

        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);

        List<ReviewInfo> reviewinfos = new ArrayList<>();

        List<VaultReview> reviews = this.vaultsReviewService.findByVaultId(vault.getID());
        Utils.getSafeStream(reviews).forEach( vr -> {
            reviewinfos.add(AdminReviewsController.getReviewInfo(vr));
        });

        return reviewinfos;
    }

    @Operation(
            summary = "Get a Vault Review",
            description = "Retrieves a specific Vault Review by its ID."
    )
    @GetMapping(value = "/vaults/vaultreviews/{vaultReviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultReview getVaultReview(@RequestHeader(HEADER_USER_ID) String userId,
                                      @PathVariable String vaultReviewId) {

        return vaultsReviewService.getVaultReview(vaultReviewId);
    }



    @Operation(
            summary = "Gets a list of Deposit Reviews for a Vault Review",
            description = "Retrieves a list of all Deposit Reviews for a specific Vault Review."
    )
    @GetMapping(value = "/vaultreviews/{vaultReviewId}/depositreviews", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DepositReview> getDepositReviews(@RequestHeader(HEADER_USER_ID) String userId,
                                                 @PathVariable String vaultReviewId) {

        VaultReview vaultReview = vaultsReviewService.getVaultReview(vaultReviewId);
        return vaultReview.getDepositReviews();

    }

    @Operation(
            summary = "Gets a particular Deposit Review",
            description = "Retrieves a specific Deposit Review by its ID."
    )
    @GetMapping(value = "/vaultreviews/depositreviews/{depositReviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DepositReview getDepositReview(@RequestHeader(HEADER_USER_ID) String userId,
                                          @PathVariable String depositReviewId) {

        DepositReview depositReview = depositsReviewService.getDepositReview(depositReviewId);
        return depositReview;

    }


}
