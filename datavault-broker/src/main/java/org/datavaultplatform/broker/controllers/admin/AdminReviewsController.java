package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.vault.Review;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.common.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@RestController
//@CrossOrigin
@Tag(name="admin-reviews-controller", description = "Administrator Review functions")
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


    @Operation(
            summary = "Gets a list of Vaults for Review",
            description = "Retrieves a list of all Vaults that are due for review."
    )
    @GetMapping(value = "/admin/vaultsForReview", produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultsData getVaultsForReview( @RequestHeader(HEADER_USER_ID) String userId) {

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


    @Operation(
            summary = "Gets the current review for a Vault",
            description = "Retrieves the most recent review details for a specific Vault."
    )
    @GetMapping(value = "/admin/vaults/{vaultID}/vaultreviews/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReviewInfo getCurrentReview(@RequestHeader(HEADER_USER_ID) String userId,
                                       @PathVariable String vaultID) throws Exception {

        User user = usersService.getUser(userId);
        if (user == null) {
            return null;
        }

        // throws Exception if vault cannot be found
        Vault vault = vaultsService.getUserVault(user, vaultID);

        VaultReview vaultReview = vault.getMostRecentVaultReview().orElse(null);

        if (vaultReview == null) {
           return null;
        }

        return getReviewInfo(vaultReview);
    }
    
    @Operation(
            summary = "Creates the current review for a Vault",
            description = "Initiates a new review process for a specified Vault.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The unique ID of the Vault",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", description = "Vault Identifier", examples = "v-123-abc")
                    )
            )
    )
    @PostMapping(value = "/admin/vaults/vaultreviews/current", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ReviewInfo createCurrentReview(@RequestHeader(HEADER_USER_ID) String userId,
                                          @RequestBody String vaultId) throws Exception {

        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);

        // if vault is not due - return null else create a vault if a pending review does not exist?
        // TODO DJH - we should create a review if one is due and does not exist already ?
        
        VaultReview vaultReview = vaultsReviewService.createVaultReview(vault);

        // If we pass back the Vault Review and Deposit Review, we lose the links between the objects, so pass
        // back a wee Transfer Object POJO that just contains the ids and let the client then request whatever it needs.
        ReviewInfo reviewInfo = getReviewInfo(vaultReview);
        return reviewInfo;
    }


    @Operation(
            summary = "Edit a Vault Review",
            description = "Updates an existing Vault Review."
    )
    @PutMapping(value = "/admin/vaults/vaultreviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultReview editVaultReview(@RequestHeader(HEADER_USER_ID) String userId,
                                       @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                       @RequestBody VaultReview vaultReview) {


        vaultsReviewService.updateVaultReview(vaultReview);

        // If the Review has been actioned, then create an Event. The Review should only be actioned once.
        if (vaultReview.getActionedDate() != null) {
            Assert.state(vaultReview.getVault() != null, "The VaultReview cannot have null Vault");
            Review vaultEvent = new Review(vaultReview.getVault().getID());
            vaultEvent.setVault(vaultReview.getVault());
            vaultEvent.setUser(usersService.getUser(userId));
            vaultEvent.setAgentType(Agent.AgentType.BROKER);
            Client client = clientsService.getClientByApiKey(clientKey);
            String clientName = client == null ? null : client.getName();
            vaultEvent.setAgent(clientName);
            eventService.addEvent(vaultEvent);
        }
        
        return vaultReview;
    }



    @Operation(
            summary = "Edit a Deposit Review",
            description = "Updates an existing Deposit Review."
    )
    @PutMapping(value = "/admin/vaultreviews/depositreviews", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DepositReview editDepositReview(@RequestHeader(HEADER_USER_ID) String userId,
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
}
