package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.vault.Review;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultReviewStatusInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.common.util.PageDTO;
import org.datavaultplatform.common.util.Utils;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiHeader;
import org.jsondoc.core.annotation.ApiHeaders;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
//@CrossOrigin
@Api(name="AdminReviews", description = "Administrator Review functions")
public class AdminReviewsController {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AdminReviewsController.class);
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

        List<Vault> vaults = vaultsService.getVaults();
        List<Vault> vaultsForReview = vaultsReviewService.getVaultsForReview(vaults);
        
        Assert.state(vaultsForReview != null, "The vaultsForReview should not be null");

        List<VaultInfo> vaultResponses = vaultsForReview.stream()
                .filter(Objects::nonNull)
                .map(Vault::convertToResponse)
                .toList();

        VaultsData result = new VaultsData();
        result.setData(vaultResponses);
        return result;
    }

    @GetMapping("/admin/vaultsForReview/all")
    public VaultsData getAllVaults(@RequestHeader(HEADER_USER_ID) String userID) {

        List<Vault> vaults = vaultsService.getVaults();

        Assert.state(vaults != null, "The vaults should not be null");

        List<VaultInfo> vaultResponses = vaults.stream()
                .filter(Objects::nonNull)
                .map(Vault::convertToResponse)
                .toList();

        VaultsData result = new VaultsData();
        result.setData(vaultResponses);
        return result;
    }

    @GetMapping("/admin/vaultsForReview/search")
    public PageDTO<VaultInfo> getVaultsByPartialName(
            @RequestParam(name = "q") String partialName) {
        Page<Vault> vaults = vaultsService.getFirstFiftyVaultsWithNameContaining(partialName);
        return new PageDTO<>(vaults.map(Vault::convertToResponse));
    }

    @GetMapping("/admin/vaults/{vaultId}/reviewstatus")
    public VaultReviewStatusInfo getVaultReviewStatusInfo(@PathVariable String vaultId) {
        return vaultsReviewService.getCurrentVaultReviewStatus(vaultId);
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
    @GetMapping("/admin/vaults/{vaultID}/vaultreviews/current")
    public ReviewInfo getCurrentReview(@RequestHeader(HEADER_USER_ID) String userID,
                                       @PathVariable String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        if (user == null) {
            return null;
        }

        // throws Exception if vault cannot be found
        Vault vault = vaultsService.getUserVault(user, vaultID);

        // If we find a record that has not been actioned then we know we have an active current record.
        VaultReview vaultReview = vault.findLatestVaultReviewIfStillUnderway().orElse(null);

        if (vaultReview == null) {
           return null;
        }

        return getReviewInfo(vaultReview);
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

        // if vault is not due - return null else create a vault if a pending review does not exist?
        
        VaultReview vaultReview = vaultsReviewService.createVaultReview(vault);

        // If we pass back the Vault Review and Deposit Review, we lose the links between the objects, so pass
        // back a wee Transfer Object POJO that just contains the ids and let the client then request whatever it needs.
        ReviewInfo reviewInfo = getReviewInfo(vaultReview);
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


        VaultReview updatedVaultReview = vaultsReviewService.updateVaultReview(vaultReview);

        // If the Review has been actioned, then create an Event. The Review should only be actioned once.
        if (updatedVaultReview.isReviewSubmitted()) {
            Assert.state(updatedVaultReview.getVault() != null, "The VaultReview cannot have null Vault");
            Vault vault = updatedVaultReview.getVault();
            Review vaultEvent = new Review(vault.getID());
            vaultEvent.setVault(vault);
            vaultEvent.setUser(usersService.getUser(userID));
            vaultEvent.setAgentType(Agent.AgentType.BROKER);
            Client client = clientsService.getClientByApiKey(clientKey);
            String clientName = client == null ? null : client.getName();
            vaultEvent.setAgent(clientName);
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


    public static ReviewInfo getReviewInfo(VaultReview vaultReview) {
        Assert.notNull(vaultReview, "The vaultReview cannot be null");
        List<DepositReview> depositReviews = vaultReview.getDepositReviews();

        // Create Lists of Deposit and DepositReview ids
        List<String> depositIds = new ArrayList<>();
        List<String> depositReviewIds = new ArrayList<>();

        Utils.getSafeStream(depositReviews)
                .forEach(dr -> {
                    Deposit deposit = dr.getDeposit();
                    String depositId = deposit == null ? null : deposit.getID();
                    depositIds.add(depositId);
                    depositReviewIds.add(dr.getId());
                });

        ReviewInfo reviewInfo = new ReviewInfo();
        reviewInfo.setVaultReviewId(vaultReview.getId());
        reviewInfo.setDepositIds(depositIds);
        reviewInfo.setDepositReviewIds(depositReviewIds);
        return reviewInfo;
    }

    @PostMapping("/admin/vaults/vaultreviews/{vaultId}/refresh")
    public void refreshDepositsOnUnderwayVaultReview(@PathVariable String vaultId) {
        
        Assert.notNull(vaultId, "The vaultId cannot be null");

        RefreshedVaultReview refreshed = vaultsReviewService.refreshDepositsOnUnderwayVaultReview(vaultId).orElse(null);
        if (refreshed == null) {
            LOG.debug("No underway VaultReview found for Vault {}", vaultId);
            return;
        }
        VaultReview underway = refreshed.underway();
        Vault vault = underway.getVault();
        List<DepositReview> depositReviewsAdded = refreshed.depositReviewsAdded();
        int size = depositReviewsAdded.size();
        AtomicInteger counter = new AtomicInteger(1);
        depositReviewsAdded.forEach(dr -> {
            LOG.info("[{}/{}] Added DepositReviewId[{}] for DepositId[{}] to VaultReviewId[{}] for VaultId[{}]",
                    counter.getAndIncrement(), size, dr.getId(), dr.getDeposit().getID(), underway.getId(), vault.getID());
        });
        LOG.info("Added [{}] DepositReviews to VaultReviewId[{}] for VaultId[{}]", size, underway.getId(), vault.getID());
    }
}
