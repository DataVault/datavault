package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.datavaultplatform.broker.services.BillingService;
import org.datavaultplatform.broker.services.ExternalMetadataService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.BillingInfo;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.BillingInformation;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
//@CrossOrigin
@Tag(name = "billing-controller", description = "Administrator Billing functions")
public class BillingController {
	private final ExternalMetadataService externalMetadataService;
	private final VaultsService vaultsService;
	private final BillingService billingService;
	private final RolesAndPermissionsService permissionsService;
	private static final Logger LOGGER = LoggerFactory.getLogger(BillingController.class);

	@Autowired
	public BillingController(ExternalMetadataService externalMetadataService,
							 VaultsService vaultsService, BillingService billingService,
							 RolesAndPermissionsService permissionsService) {
		this.externalMetadataService = externalMetadataService;
		this.vaultsService = vaultsService;
		this.billingService = billingService;
		this.permissionsService = permissionsService;
	}

	@Operation(
			summary = "Search all Billing Vaults",
			description = "Searches for Vaults with billing information based on a query string and other parameters.",
			parameters = {
					@Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
					@Parameter(in = ParameterIn.QUERY, name = "query", required = true, description = "Search query string", schema = @Schema(type = "string")),
					@Parameter(in = ParameterIn.QUERY, name = "sort", description = "Sort field", schema = @Schema(type = "string")),
					@Parameter(in = ParameterIn.QUERY, name = "order", description = "Sort order", schema = @Schema(type = "string", allowableValues = {"asc", "desc"}, defaultValue = "asc")),
					@Parameter(in = ParameterIn.QUERY, name = "offset", description = "Row offset", schema = @Schema(type = "string", defaultValue = "0")),
					@Parameter(in = ParameterIn.QUERY, name = "maxResult", description = "Maximum number of results", schema = @Schema(type = "string"))
			}
	)
    @GetMapping(value = "/admin/billing/search", produces = MediaType.APPLICATION_JSON_VALUE)
	public VaultsData searchAllBillingVaults(@RequestHeader(HEADER_USER_ID) String userId,
											 @RequestParam String query,
											 @RequestParam(value = "sort", required = false) String sort,
											 @RequestParam(value = "order", required = false) String order,
											 @RequestParam(value = "offset", required = false) String offset,
											 @RequestParam(value = "maxResult", required = false) String maxResult) {

		List<VaultInfo> billingResponses = new ArrayList<>();
        int recordsTotal = 0;
        int recordsFiltered = 0;
        List<Vault> vaults = vaultsService.search(userId, query, sort, order, offset, maxResult);
        if(CollectionUtils.isNotEmpty(vaults)) {
			for (Vault vault : vaults) {
	            billingResponses.add(vault.convertToResponseBilling());
	        }
	        //calculate and create a map of project size
	        Map<String, Long> projectSizeMap = vaultsService.getAllProjectsSize();
	        //update project Size in the response
	        for(VaultInfo vault: billingResponses) {

				User owner = permissionsService.getVaultOwner(vault.getID());
				if(owner != null) {
					vault.setOwnerId(owner.getID());
					vault.setOwnerName(owner.getFirstname() + " " + owner.getLastname());
				}

	        	if(vault.getProjectId() != null) {
	        		vault.setProjectSize(projectSizeMap.get(vault.getProjectId()));
	        	}
	        }
	        recordsTotal = vaultsService.getTotalNumberOfVaults(userId);
	        recordsFiltered = vaultsService.getTotalNumberOfVaults(userId, query);
        }
        
        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setRecordsFiltered(recordsFiltered);
        data.setData(billingResponses);
        return data;
    }
    
    @Operation(
			summary = "Get a Vault's billing information",
			description = "Retrieves the billing information for a specific Vault."
	)
    @GetMapping(value = "/admin/billing/{vaultId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BillingInformation getVaultBillingInfo(@RequestHeader(HEADER_USER_ID) String userId,
                                                  @PathVariable String vaultId) {

    	BillingInformation vaultBillingInfo = vaultsService.getVault(vaultId).convertToBillingDetailsResponse();

        return vaultBillingInfo;
    }

	@Operation(
			summary = "Update a Vault's billing details",
			description = "Updates the billing details for a specific Vault."
	)
	@PostMapping(value = "/admin/billing/{vaultId}/updateBilling", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public BillingInformation updateBillingDetails(@RequestHeader(HEADER_USER_ID) String userId,
												   @PathVariable String vaultId,
												   @RequestBody BillingInformation billingDetails) {
		Vault vault = vaultsService.getVault(vaultId);
    	vault.setProjectId(billingDetails.getProjectId());
    	vaultsService.updateVault(vault);
		if (null != vault) {
    		BillingInfo billinginfo = vault.getBillinginfo();
    		if(billinginfo == null) {
    			billinginfo =  new BillingInfo();
    		}
    		if (billinginfo.getBillingType() == null) {
    			billinginfo.setBillingType(PendingVault.Billing_Type.ORIG);
			}
    		billinginfo.setAmountBilled(billingDetails.getAmountBilled());
    		billinginfo.setAmountToBeBilled(billingDetails.getAmountToBeBilled());
    		billinginfo.setBudgetCode(billingDetails.getBudgetCode());
    		billinginfo.setContactName(billingDetails.getContactName());
    		billinginfo.setSchool(billingDetails.getSchool());
    		billinginfo.setSpecialComments(billingDetails.getSpecialComments());
    		billinginfo.setSubUnit(billingDetails.getSubUnit());
    		billinginfo.setVault(vault);
			billinginfo.setPaymentDetails(billingDetails.getPaymentDetails());

    		billingService.saveOrUpdateVault(billinginfo);
            
    	}	
    	return vault.convertToBillingDetailsResponse();
    }
    
	public ExternalMetadataService getExternalMetadataService() {
		return externalMetadataService;
	}

	public VaultsService getVaultsService() {
		return vaultsService;
	}

	public BillingService getBillingService() {
		return billingService;
	}

}
