package org.datavaultplatform.webapp.controllers.admin;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.datavaultplatform.common.response.BillingInformation;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class AdminBillingController {


	private static final Logger logger = LoggerFactory.getLogger(AdminBillingController.class);
	
	private static final String _0 = "0";
	private static final String MAX_RECORDS_PER_PAGE = "10";

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }


    @RequestMapping(value = "/admin/billing", method = RequestMethod.GET)
    public String billingByVaults(ModelMap model,
                               @RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "sort", required = false) String sort,
                               @RequestParam(value = "order", required = false) String order,
                               @RequestParam(value = "pageId", required = false) String pageId) throws Exception {


        String theSort = sort;
        String theOrder = order;
        String offset = pageId;
        if (sort == null) theSort = "creationTime";
        if (order == null) theOrder = "desc";
        if(pageId == null) offset = "1";
        model.addAttribute("activePageId", offset);

        // calculate offset which is passed to the service to fetch records from that row Id
        int maxRecords = Integer.valueOf(MAX_RECORDS_PER_PAGE).intValue();
        offset = String.valueOf(Integer.valueOf(offset).intValue() * maxRecords - maxRecords);
        
        if ((query == null) || ("".equals(query))) {
        	VaultsData billingData = restService.getBillingVaultsAll(theSort, theOrder, offset, MAX_RECORDS_PER_PAGE);
        	//restService.getVaultsListingAll(theSort, theOrder, offset, MAX_RECORDS_PER_PAGE);
        	long recordsTotal = billingData.getRecordsTotal();
        	int numberOfPages = (int)Math.ceil(recordsTotal/(Double.valueOf(MAX_RECORDS_PER_PAGE)));
        	List<String> pages = new ArrayList<>();
        	int i =1;
        	while(i <= numberOfPages) {
        		pages.add(String.valueOf(i));
        		i++;
        	}
        	model.addAttribute("pages", pages);
			model.addAttribute("vaults", billingData.getData());
            model.addAttribute("query", "");
            model.addAttribute("recordsInfo", constructTableRecordsInfo(offset, recordsTotal, billingData.getRecordsFiltered(),
            		billingData.getData().size(), Boolean.FALSE));
        } else {
        	VaultsData filteredBillingData = restService.searchVaultsForBilling(query, theSort, theOrder, offset, MAX_RECORDS_PER_PAGE);
            long filteredRecordsTotal = filteredBillingData.getRecordsFiltered();
            int numberOfPages = (int)Math.ceil(filteredRecordsTotal/(Double.valueOf(MAX_RECORDS_PER_PAGE)));
            List<String> pages = new ArrayList<>();
        	int i =1;
        	while(i <= numberOfPages) {
        		pages.add(String.valueOf(i));
        		i++;
        	}
        	model.addAttribute("pages", pages);
            model.addAttribute("vaults", filteredBillingData.getData());
            model.addAttribute("query", query);
            model.addAttribute("recordsInfo", constructTableRecordsInfo(offset, filteredBillingData.getRecordsTotal(),
            		filteredRecordsTotal, filteredBillingData.getData().size(), Boolean.TRUE));
        }
        
        model.addAttribute("theSort", theSort);
        model.addAttribute("theOrder", theOrder);

        // Pass the sort and order
        if (sort == null) sort = "";
        model.addAttribute("sort", sort);
        model.addAttribute("orderid", "asc");
        model.addAttribute("ordername", "asc");       
        model.addAttribute("orderuser", "asc");       
        model.addAttribute("ordervaultsize", "asc");
        model.addAttribute("orderProjectId", "asc");
        model.addAttribute("ordercreationtime", "asc");
        model.addAttribute("orderreviewDate", "asc");
        if ("asc".equals(order)) {
            if ("id".equals(sort)) model.addAttribute("orderid", "dec");
            if ("name".equals(sort)) model.addAttribute("ordername", "dec");            
            if ("reviewDate".equals(sort)) model.addAttribute("orderreviewDate", "dec");            
            if ("user".equals(sort)) model.addAttribute("orderuser", "dec");
            if ("vaultSize".equals(sort)) model.addAttribute("ordervaultsize", "dec");    
            if ("projectId".equals(sort)) model.addAttribute("orderProjectId", "dec");  
            if ("creationTime".equals(sort)) model.addAttribute("ordercreationtime", "dec");
        }

        return "admin/billing/index";
    }
    
    @RequestMapping(value = "/admin/billing/csv", method = RequestMethod.GET)
    public void exportBillingVaults(HttpServletResponse response,
                               @RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "sort", required = false) String sort,
                               @RequestParam(value = "order", required = false) String order) throws Exception {
        String theSort = sort;
        String theOrder = order;
        if (sort == null) theSort = "creationTime";
        if (order == null) theOrder = "desc";

        List<VaultInfo> vaults = null;

        if ((query == null) || ("".equals(query))) {
            VaultsData vaultData = restService.getBillingVaultsAll(theSort, theOrder, _0, _0);
			vaults = vaultData.getData();
        } else {
        	VaultsData vaultData =  restService.searchVaultsForBilling(query, theSort, theOrder, _0, _0);
        	vaults = vaultData.getData();
        }

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
    
    private String constructTableRecordsInfo(String offset, long recordsTotal, long filteredRecords, int numberOfRecordsonPage, boolean isFiltered) {
		StringBuilder recordsInfo = new StringBuilder();
		recordsInfo.append("Showing ").append(Integer.valueOf(offset).intValue() + 1)
		.append(" - ").append(Integer.valueOf(offset).intValue()+  numberOfRecordsonPage);
		if(isFiltered) {
			recordsInfo.append(" vaults of ").append(filteredRecords)
			.append(" (").append("filtered from ").append(recordsTotal).append(" total vaults)");
		} else {
			recordsInfo.append(" vaults of ").append(recordsTotal);
		}
		return recordsInfo.toString();
	}
    
    @RequestMapping(value = "/admin/billing/{vaultId}", method = RequestMethod.GET)
    public String retrieveBillingInfo(ModelMap model, @PathVariable("vaultId") String vaultId) {
    	BillingInformation billingDetails = restService.getVaultBillingInfo(vaultId);
        model.addAttribute("billingDetails", billingDetails);       
        return "admin/billing/billingDetails";
    }
   
    @RequestMapping(value = "/admin/billing/updateBillingDetails", method = RequestMethod.POST)
    public String updateBillingDetails(ModelMap model,           
            @ModelAttribute("billingDetails") BillingInformation billingDetails         
            ) throws Exception {
    	 System.out.println("-----------getBudgetCode---------"+billingDetails.getBudgetCode());
    	restService.updateBillingInfo(billingDetails.getVaultID(),billingDetails);
    	return "admin/billing/billingDetails";
    }

	
}


