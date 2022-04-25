package org.datavaultplatform.broker.controllers;

import org.apache.commons.collections4.CollectionUtils;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.vault.Review;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiHeader;
import org.jsondoc.core.annotation.ApiHeaders;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@Api(name="Reviews", description = "Review functions")
public class ReviewsController {

    private VaultsService vaultsService;
    private VaultsReviewService vaultsReviewService;
    private DepositsReviewService depositsReviewService;
    private UsersService usersService;
    private ClientsService clientsService;
    private EventService eventService;

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setVaultsReviewService(VaultsReviewService vaultsReviewService) {
        this.vaultsReviewService = vaultsReviewService;
    }

    public void setDepositsReviewService(DepositsReviewService depositsReviewService) {
        this.depositsReviewService = depositsReviewService;
    }

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    public void setClientsService(ClientsService clientsService) {
        this.clientsService = clientsService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }


    @ApiMethod(
            path = "/vaults/{vaultid}/vaultreviews",
            verb = ApiVerb.GET,
            description = "Gets a list of Reviews for a Vault",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/vaults/{vaultid}/vaultreviews", method = RequestMethod.GET)
    public List<ReviewInfo> getVaultReviews(@RequestHeader(value = "X-UserID", required = true) String userID,
                                             @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        List<ReviewInfo> reviewinfos = new ArrayList<ReviewInfo>();

        for (VaultReview vr : vault.getVaultReviews()) {

            VaultReview vaultReview = vr;
            List <DepositReview> depositReviews = vr.getDepositReviews();

            // Create Lists of Deposit and DepositReview ids
            List<String> depositIds = new ArrayList<>();
            List<String> depositReviewIds = new ArrayList<>();

            for (DepositReview depositReview : depositReviews) {
                depositIds.add(depositReview.getDeposit().getID());
                depositReviewIds.add(depositReview.getId());
            }

            ReviewInfo reviewInfo = new ReviewInfo();
            reviewInfo.setVaultReviewId(vaultReview.getId());
            reviewInfo.setDepositIds(depositIds);
            reviewInfo.setDepositReviewIds(depositReviewIds);

            reviewinfos.add(reviewInfo);
        }

        return reviewinfos;
    }


    @ApiMethod(
            path = "/vaults/vaultreviews/{vaultReviewId}",
            verb = ApiVerb.GET,
            description = "Get a Vault Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @RequestMapping(value = "/vaults/vaultreviews/{vaultReviewId}", method = RequestMethod.GET)
    public VaultReview getVaultReview(@RequestHeader(value = "X-UserID", required = true) String userID,
                                @PathVariable("vaultReviewId") String vaultReviewId) throws Exception {

        return vaultsReviewService.getVaultReview(vaultReviewId);
    }



    @ApiMethod(
            path = "/vaultreviews/{vaultReviewId}/depositreviews",
            verb = ApiVerb.GET,
            description = "Gets a list of Deposit Reviews for a Vault Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/vaultreviews/{vaultReviewId}/depositreviews", method = RequestMethod.GET)
    public List<DepositReview> getDepositReviews(@RequestHeader(value = "X-UserID", required = true) String userID,
                                             @PathVariable("vaultReviewId") String vaultReviewId) throws Exception {

        VaultReview vaultReview = vaultsReviewService.getVaultReview(vaultReviewId);
        return vaultReview.getDepositReviews();

    }

    @ApiMethod(
            path = "/vaultreviews/depositreviews/{depositReviewId}",
            verb = ApiVerb.GET,
            description = "Gets a particular Deposit Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/vaultreviews/depositreviews/{depositReviewId}", method = RequestMethod.GET)
    public DepositReview getDepositReview(@RequestHeader(value = "X-UserID", required = true) String userID,
                                                 @PathVariable("depositReviewId") String depositReviewId) throws Exception {

        DepositReview depositReview = depositsReviewService.getDepositReview(depositReviewId);
        return depositReview;

    }


}
