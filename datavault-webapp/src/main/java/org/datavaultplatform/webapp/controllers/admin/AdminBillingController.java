package org.datavaultplatform.webapp.controllers.admin;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.response.BillingInformation;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;



@Controller
@ConditionalOnBean(RestService.class)
public class AdminBillingController {


	private static final Logger logger = LoggerFactory.getLogger(AdminBillingController.class);

	private static final int MAX_RECORDS_PER_PAGE = 10;

    private final RestService restService;

    @Autowired
    public AdminBillingController(RestService restService) {
        this.restService = restService;
    }


    @RequestMapping(value = "/admin/billing", method = RequestMethod.GET)
    public String billingByVaults(ModelMap model,
                               @RequestParam(value = "query", required = false, defaultValue = "") String query,
                               @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                               @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                               @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId)
            throws Exception {

        // calculate offset which is passed to the service to fetch records from that row Id
        int offset = (pageId - 1) * MAX_RECORDS_PER_PAGE;

        VaultsData filteredBillingData = restService.searchVaultsForBilling(query, sort, order, offset, MAX_RECORDS_PER_PAGE);
        int filteredRecordsTotal = filteredBillingData.getRecordsFiltered();
        int numberOfPages = (int)Math.ceil((double)filteredRecordsTotal/MAX_RECORDS_PER_PAGE);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("activePageId", pageId);
        model.addAttribute("vaults", filteredBillingData.getData());
        model.addAttribute("query", query);

        boolean isFiltered = !query.equals("");
        model.addAttribute("recordsInfo",
                constructTableRecordsInfo(offset, filteredBillingData.getRecordsTotal(),
                        filteredRecordsTotal, filteredBillingData.getData().size(), isFiltered));
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        String otherOrder = order.equals("asc")?"desc":"asc";
        model.addAttribute("ordername", "name".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderuser", "vault.user.id".equals(sort)?otherOrder:"asc");
        model.addAttribute("ordervaultsize", "vaultSize".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderProjectId", "projectId".equals(sort)?otherOrder:"asc");
        model.addAttribute("ordercreationtime", "creationTime".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderreviewDate", "reviewDate".equals(sort)?otherOrder:"asc");

        return "admin/billing/index";
    }
    
    @RequestMapping(value = "/admin/billing/csv", method = RequestMethod.GET)
    public void exportBillingVaults(HttpServletResponse response,
                               @RequestParam(value = "query", required = false, defaultValue = "") String query,
                               @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                               @RequestParam(value = "order", required = false, defaultValue = "desc") String order)
            throws Exception {

        List<VaultInfo> vaults = null;

        VaultsData vaultData =  restService.searchVaultsForBilling(query, sort, order, 0, 0);
        vaults = vaultData.getData();

        response.setContentType("text/csv");

        // creates mock data
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=\"billing.csv\"";
        response.setHeader(headerKey, headerValue);

        String[] header = { "Vault name", "Vault Size","Project Id","Project Size","Amount To Be Billed","Amount Billed", "User Name", "Review Date","Creation Time" };

        String[] fieldMapping = { "name",  "sizeStr","projectId","projectSize","amountToBeBilled","amountBilled",  "userName","reviewDate", "CreationTime" };

        try {
            // uses the Super CSV API to generate CSV data from the model data
            ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

            csvWriter.writeHeader(header);

            for (VaultInfo aVault : vaults) {
                csvWriter.write(aVault, fieldMapping);
            }

            csvWriter.close();

        } catch (Exception e){
            logger.error("IOException: "+e);
            e.printStackTrace();
        }
    }
    
    private String constructTableRecordsInfo(int offset, int recordsTotal, int filteredRecords, int numberOfRecordsonPage, boolean isFiltered) {
		StringBuilder recordsInfo = new StringBuilder();
		recordsInfo.append("Showing ").append(offset).append(" - ").append(offset + numberOfRecordsonPage);
		if(isFiltered) {
			recordsInfo.append(" vaults of ").append(filteredRecords).append(" (").append("filtered from ")
                    .append(recordsTotal).append(" total vaults)");
		} else {
			recordsInfo.append(" vaults of ").append(recordsTotal);
		}
		return recordsInfo.toString();
	}
    
    @RequestMapping(value = "/admin/billing/{vaultId}", method = RequestMethod.GET)
    public String retrieveBillingInfo(ModelMap model, @PathVariable("vaultId") String vaultId) {
    	BillingInformation billingDetails = restService.getVaultBillingInfo(vaultId);
        model.addAttribute("billingDetails", billingDetails);
        String billingPage = "admin/billing/billingDetailsNA";

        /*if (billingDetails.getBillingType().equals(PendingVault.Billing_Type.GRANT_FUNDING)) {
            billingPage = "admin/billing/billingDetailsGrant";
        }
        if (billingDetails.getBillingType().equals(PendingVault.Billing_Type.BUDGET_CODE)) {
            billingPage = "admin/billing/billingDetailsBudget";
        }
        if (billingDetails.getBillingType().equals(PendingVault.Billing_Type.SLICE)) {
            billingPage = "admin/billing/billingDetailsSlice";
        }*/
        return this.getBillingInfoPage(billingDetails.getBillingType());
    }
   
    @RequestMapping(value = "/admin/billing/updateBillingDetails", method = RequestMethod.POST)
    public String updateBillingDetails(ModelMap model,           
            @ModelAttribute("billingDetails") BillingInformation billingDetails         
            ) throws Exception {
    	 System.out.println("-----------getBudgetCode---------"+billingDetails.getBudgetCode());
    	restService.updateBillingInfo(billingDetails.getVaultID(),billingDetails);
    	//return "admin/billing/billingDetails";
        return this.getBillingInfoPage(billingDetails.getBillingType());
    }

    private String getBillingInfoPage(PendingVault.Billing_Type type) {
        String retVal = "admin/billing/billingDetails";

        // so the old pre new interface vaults still go to the old page
        if (type == null) {
            return retVal;
        }

        if (type.equals(PendingVault.Billing_Type.ORIG)) {
            retVal = "admin/billing/billingDetails";
        }

        if (type.equals(PendingVault.Billing_Type.NA)) {
            retVal = "admin/billing/billingDetailsNA";
        }

        if (type.equals(PendingVault.Billing_Type.GRANT_FUNDING)) {
            retVal = "admin/billing/billingDetailsGrant";
        }
        if (type.equals(PendingVault.Billing_Type.BUDGET_CODE)) {
            retVal = "admin/billing/billingDetailsBudget";
        }
        if (type.equals(PendingVault.Billing_Type.SLICE)) {
            retVal = "admin/billing/billingDetailsSlice";
        }
        return retVal;
    }

	
}


