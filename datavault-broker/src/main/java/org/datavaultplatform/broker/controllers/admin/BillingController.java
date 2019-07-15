package org.datavaultplatform.broker.controllers.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.datavaultplatform.broker.services.ExternalMetadataService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class BillingController {
	private ExternalMetadataService externalMetadataService;
	//private BillingService billingService;
	private VaultsService vaultsService;
	private static final Logger LOGGER = LoggerFactory.getLogger(BillingController.class);

    @RequestMapping(value = "/admin/bill", method = RequestMethod.GET)
    public VaultsData getBillingVaultsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @RequestParam(value = "sort", required = false)
                                        @ApiQueryParam(name = "sort", description = "Vault sort field", allowedvalues = {"id", "name", "vaultSize", "user", "creationTime", "reviewDate"}, defaultvalue = "creationTime", required = false) String sort,
                                        @RequestParam(value = "order", required = false)
                                        @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "dec"}, defaultvalue = "asc", required = false) String order,
                                        @RequestParam(value = "offset", required = false)
									    @ApiQueryParam(name = "offset", description = "Vault row id ", defaultvalue = "0", required = false) String offset,
									    @RequestParam(value = "maxResult", required = false)
									    @ApiQueryParam(name = "maxResult", description = "Number of records", required = false) String maxResult) throws Exception {

        if (sort == null) sort = "";
        if (order == null) order = "asc";
        Long recordsTotal = 0L;
		/*
		 * List<BillingInformation> billingResponses = new ArrayList<>();
		 * List<BillingInfo> billingDetails = billingService.getVaults(sort,
		 * order,offset, maxResult);
		 */
        
        List<VaultInfo> billingResponses = new ArrayList<>();
        List<Vault> billingDetails = vaultsService.getVaults(sort, order,offset, maxResult);
        
        if(CollectionUtils.isNotEmpty(billingDetails)) {
        	Map<String, String> pureProjectIds = externalMetadataService.getPureProjectIds();
			for (Vault billingList : billingDetails) {				
				VaultInfo billingInfo = billingList.convertToResponseBilling();
	            //set the project Id from Pure if it doesn't exists in DB
	           
				billingResponses.add(billingInfo);
	        }
	        recordsTotal = vaultsService.getTotalNumberOfVaults();
	        Map<String, Long> projectSizeMap = constructProjectSizeMap(billingResponses);
	        //update project Size in the response
	        for(VaultInfo vault: billingResponses) {
	        	if(vault.getProjectId() != null) {
	        		vault.setProjectSize(projectSizeMap.get(vault.getProjectId()));
	        	}
	        }
        }
        //calculate and create a map of project size
        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setData(billingResponses);
        return data;
    }
    /**
     * This method calculates and creates a map of projectId and project size
     * @param vaults
     * @return
     */
    private Map<String, Long> constructProjectSizeMap(List<VaultInfo> vaults) {
    	Map<String, Long> projectSizeMap = new HashMap<>();
    	for(VaultInfo vault: vaults) {
    		if(vault.getProjectId() != null) {
				if(projectSizeMap.containsKey(vault.getProjectId())) {
	    			Long projectSize = projectSizeMap.get(vault.getProjectId());
	    			projectSizeMap.put(vault.getProjectId(), projectSize.longValue()+ vault.getVaultSize());
	    		} else {
	    			projectSizeMap.put(vault.getProjectId(), vault.getVaultSize());
	    		}
    		}
    	}
    	return projectSizeMap;
    }
    
	public ExternalMetadataService getExternalMetadataService() {
		return externalMetadataService;
	}

	public void setExternalMetadataService(ExternalMetadataService externalMetadataService) {
		this.externalMetadataService = externalMetadataService;
	}

	/*
	 * public BillingService getBillingService() { return billingService; } public
	 * void setBillingService(BillingService billingService) { this.billingService =
	 * billingService; }
	 */
	public VaultsService getVaultsService() {
		return vaultsService;
	}
	public void setVaultsService(VaultsService vaultsService) {
		this.vaultsService = vaultsService;
	}   
    
  
}
