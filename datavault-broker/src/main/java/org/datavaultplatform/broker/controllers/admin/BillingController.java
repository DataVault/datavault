package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.datavaultplatform.broker.services.BillingService;
import org.datavaultplatform.broker.services.ExternalMetadataService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.BillingInfo;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.BillingInformation;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class BillingController {
	private final ExternalMetadataService externalMetadataService;
	private final VaultsService vaultsService;
	private final BillingService billingService;
	private static final Logger LOGGER = LoggerFactory.getLogger(BillingController.class);

	@Autowired
	public BillingController(ExternalMetadataService externalMetadataService,
			VaultsService vaultsService, BillingService billingService) {
		this.externalMetadataService = externalMetadataService;
		this.vaultsService = vaultsService;
		this.billingService = billingService;
	}

	@GetMapping("/admin/billing/search")
    public VaultsData searchAllBillingVaults(@RequestHeader(HEADER_USER_ID) String userID,
                                                  @RequestParam String query,
                                                  @RequestParam(value = "sort", required = false) String sort,
                                                  @RequestParam(value = "order", required = false)
                                                  @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "desc"}, defaultvalue = "asc", required = false) String order,
                                                  @RequestParam(value = "offset", required = false)
											      @ApiQueryParam(name = "offset", description = "Vault row id ", defaultvalue = "0", required = false) String offset,
											      @RequestParam(value = "maxResult", required = false)
											      @ApiQueryParam(name = "maxResult", description = "Number of records", required = false) String maxResult) {

        List<VaultInfo> billingResponses = new ArrayList<>();
        int recordsTotal = 0;
        int recordsFiltered = 0;
        List<Vault> vaults = vaultsService.search(userID, query, sort, order, offset, maxResult);
        if(CollectionUtils.isNotEmpty(vaults)) {
			for (Vault vault : vaults) {
	            billingResponses.add(vault.convertToResponseBilling());
	        }
	        //calculate and create a map of project size
	        Map<String, Long> projectSizeMap = vaultsService.getAllProjectsSize();
	        //update project Size in the response
	        for(VaultInfo vault: billingResponses) {
	        	if(vault.getProjectId() != null) {
	        		vault.setProjectSize(projectSizeMap.get(vault.getProjectId()));
	        	}
	        }
	        recordsTotal = vaultsService.getTotalNumberOfVaults(userID);
	        recordsFiltered = vaultsService.getTotalNumberOfVaults(userID, query);
        }
        
        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setRecordsFiltered(recordsFiltered);
        data.setData(billingResponses);
        return data;
    }
    
    @GetMapping("/admin/billing/{vaultId}")
    public BillingInformation getVaultBillingInfo(@RequestHeader(HEADER_USER_ID) String userID,
                                  @PathVariable("vaultId") String vaultId) {

    	BillingInformation vaultBillingInfo = vaultsService.getVault(vaultId).convertToBillingDetailsResponse();

        return vaultBillingInfo;
    }
    @PostMapping("/admin/billing/{vaultid}/updateBilling")
    public BillingInformation updateBillingDetails(@RequestHeader(HEADER_USER_ID) String userID,
                                    @PathVariable("vaultid") String vaultID,
                                    @RequestBody BillingInformation billingDetails) {
    	Vault vault = vaultsService.getVault(vaultID);
    	vault.setProjectId(billingDetails.getProjectId());
    	vaultsService.updateVault(vault);
    	if(null!=vault)
    	{
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
