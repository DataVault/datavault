package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@Api(name="AdminReviews", description = "Administrator Review functions")
public class AdminReviewsController {

    private final VaultsService vaultsService;
    private final VaultsReviewService vaultsReviewService;
    private final DepositsReviewService depositsReviewService;
    private final UsersService usersService;
    private final ClientsService clientsService;
    private final EventService eventService;

    @Autowired
    public AdminReviewsController(VaultsService vaultsService,
        VaultsReviewService vaultsReviewService, DepositsReviewService depositsReviewService,
        UsersService usersService, ClientsService clientsService, EventService eventService) {
        this.vaultsService = vaultsService;
        this.vaultsReviewService = vaultsReviewService;
        this.depositsReviewService = depositsReviewService;
        this.usersService = usersService;
        this.clientsService = clientsService;
        this.eventService = eventService;
    }


    @ApiMethod(
            path = "/admin/vaultsForReview",
            verb = ApiVerb.GET,
            description = "Gets a list of Vaults for Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/admin/vaultsForReview")
    public VaultsData getVaultsForReview(@RequestHeader(HEADER_USER_ID) String userID) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        List<Vault> vaults = vaultsService.getVaults();
        List<Vault> vaultsForReview = vaultsReviewService.getVaultsForReview(vaults);

        if(CollectionUtils.isNotEmpty(vaultsForReview)) {
            for (Vault vault : vaultsForReview) {
                vaultResponses.add(vault.convertToResponse());
            }
        }

        VaultsData vaultsData = new VaultsData();
        vaultsData.setData(vaultResponses);
        return vaultsData;
    }


    @ApiMethod(
            path = "/admin/vaults/{vaultid}/vaultreviews/current",
            verb = ApiVerb.GET,
            description = "Gets the current review for a Vault",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/admin/vaults/{vaultid}/vaultreviews/current")
    public ReviewInfo getCurrentReview(@RequestHeader(HEADER_USER_ID) String userID,
                                             @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);
        List<Deposit> deposits = vault.getDeposits();

        VaultReview vaultReview = null;
        List <DepositReview> depositReviews = null;

        // If we find a record that has not been actioned then we know we have an active current record.
        for (VaultReview vr : vault.getVaultReviews()) {
            if (vr.getActionedDate() == null) {
                vaultReview = vr;
                depositReviews = vr.getDepositReviews();
            }
        }

        if (vaultReview == null) {
           return null;
        } else {
            // If we pass back the Vault Review and Deposit Review we lose the links between the objects, so pass
            // back a wee Transfer Object POJO that just contains the ids, and let the client then request whatever it needs.

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

            return reviewInfo;
        }
    }

    @ApiMethod(
            path = "/admin/vaults/vaultreviews/current",
            verb = ApiVerb.POST,
            description = "Creates the current review for a Vault",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @PostMapping("/admin/vaults/vaultreviews/current")
    public ReviewInfo createCurrentReview(@RequestHeader(HEADER_USER_ID) String userID,
                                       @RequestBody String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        VaultReview vaultReview = vaultsReviewService.createVaultReview(vault);
        List <DepositReview> depositReviews = depositsReviewService.addDepositReviews(vault, vaultReview);

        // If we pass back the Vault Review and Deposit Review we lose the links between the objects, so pass
        // back a wee Transfer Object POJO that just contains the ids, and let the client then request whatever it needs.

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

        return reviewInfo;
    }


    @ApiMethod(
            path = "/admin/vaults/vaultreviews",
            verb = ApiVerb.PUT,
            description = "Edit a Vault Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PutMapping("/admin/vaults/vaultreviews")
    public VaultReview editVaultReview(@RequestHeader(HEADER_USER_ID) String userID,
                                       @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                       @RequestBody VaultReview vaultReview) {


        vaultsReviewService.updateVaultReview(vaultReview);

        // If the Review has been actioned then create an Event. The Review should only be actioned once.
        if (vaultReview.getActionedDate() != null) {
            Review vaultEvent = new Review(vaultReview.getVault().getID());
            vaultEvent.setVault(vaultReview.getVault());
            vaultEvent.setUser(usersService.getUser(userID));
            vaultEvent.setAgentType(Agent.AgentType.BROKER);
            vaultEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
            eventService.addEvent(vaultEvent);
        }
        
        return vaultReview;
    }



    @ApiMethod(
            path = "/admin/vaultreviews/depositreviews",
            verb = ApiVerb.PUT,
            description = "Edit a Vault DepositReview",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PutMapping("/admin/vaultreviews/depositreviews")
    public DepositReview editDepositReview(@RequestHeader(HEADER_USER_ID) String userID,
                                       @RequestBody DepositReview depositReview) {


        depositsReviewService.updateDepositReview(depositReview);

        return depositReview;
    }


}
