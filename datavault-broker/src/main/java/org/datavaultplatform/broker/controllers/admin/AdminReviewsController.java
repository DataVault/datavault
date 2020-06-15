package org.datavaultplatform.broker.controllers.admin;

import org.apache.commons.collections.CollectionUtils;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.vault.Review;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.DepositInfo;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@RestController
@Api(name="AdminReviews", description = "Administrator Review functions")
public class AdminReviewsController {

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
            path = "/admin/vaultsForReview",
            verb = ApiVerb.GET,
            description = "Gets a list of Vaults for Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/vaultsForReview", method = RequestMethod.GET)
    public VaultsData getVaultsForReview(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

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
            path = "/admin/vaults/{vaultid}/vaultreviews",
            verb = ApiVerb.GET,
            description = "Gets a list of Reviews for a Vault",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/vaults/{vaultid}/vaultreviews", method = RequestMethod.GET)
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
            path = "/admin/vaults/{vaultid}/vaultreviews/current",
            verb = ApiVerb.GET,
            description = "Gets the current review for a Vault",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/vaults/{vaultid}/vaultreviews/current", method = RequestMethod.GET)
    public ReviewInfo getCurrentReview(@RequestHeader(value = "X-UserID", required = true) String userID,
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
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/vaults/vaultreviews/current", method = RequestMethod.POST)
    public ReviewInfo createCurrentReview(@RequestHeader(value = "X-UserID", required = true) String userID,
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
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @RequestMapping(value = "/admin/vaults/vaultreviews", method = RequestMethod.PUT)
    public VaultReview editVaultReview(@RequestHeader(value = "X-UserID", required = true) String userID,
                                       @RequestHeader(value = "X-Client-Key", required = true) String clientKey,
                                       @RequestBody VaultReview vaultReview) throws Exception {


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
            path = "/admin/vaults/vaultreviews/{vaultReviewId}",
            verb = ApiVerb.GET,
            description = "Get a Vault Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @RequestMapping(value = "/admin/vaults/vaultreviews/{vaultReviewId}", method = RequestMethod.GET)
    public VaultReview getVaultReview(@RequestHeader(value = "X-UserID", required = true) String userID,
                                @PathVariable("vaultReviewId") String vaultReviewId) throws Exception {

        return vaultsReviewService.getVaultReview(vaultReviewId);
    }



    @ApiMethod(
            path = "/admin/vaultreviews/{vaultReviewId}/depositreviews",
            verb = ApiVerb.GET,
            description = "Gets a list of Deposit Reviews for a Vault Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/vaultreviews/{vaultReviewId}/depositreviews", method = RequestMethod.GET)
    public List<DepositReview> getDepositReviews(@RequestHeader(value = "X-UserID", required = true) String userID,
                                             @PathVariable("vaultReviewId") String vaultReviewId) throws Exception {

        VaultReview vaultReview = vaultsReviewService.getVaultReview(vaultReviewId);
        return vaultReview.getDepositReviews();

    }

    @ApiMethod(
            path = "/admin/vaultreviews/depositreviews/{depositReviewId}",
            verb = ApiVerb.GET,
            description = "Gets a particular Deposit Review",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/vaultreviews/depositreviews/{depositReviewId}", method = RequestMethod.GET)
    public DepositReview getDepositReview(@RequestHeader(value = "X-UserID", required = true) String userID,
                                                 @PathVariable("depositReviewId") String depositReviewId) throws Exception {

        DepositReview depositReview = depositsReviewService.getDepositReview(depositReviewId);
        return depositReview;

    }

    @ApiMethod(
            path = "/admin/vaultreviews/depositreviews",
            verb = ApiVerb.PUT,
            description = "Edit a Vault DepositReview",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @RequestMapping(value = "/admin/vaultreviews/depositreviews", method = RequestMethod.PUT)
    public DepositReview editDepositReview(@RequestHeader(value = "X-UserID", required = true) String userID,
                                       @RequestBody DepositReview depositReview) throws Exception {


        depositsReviewService.updateDepositReview(depositReview);

        return depositReview;
    }


}
