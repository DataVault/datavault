package org.datavaultplatform.webapp.controllers.admin;


import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.DepositsData;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * User: Stuart Lewis
 * Date: 27/09/2015
 */

@Controller
public class AdminDepositsController {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }
    private static final Logger logger = LoggerFactory.getLogger(AdminDepositsController.class);
	

    @RequestMapping(value = "/admin/deposits", method = RequestMethod.GET)
    public String getDepositsListing(ModelMap model,
                                     @RequestParam(value = "query", required = false) String query,
                                     @RequestParam(value = "sort", required = false) String sort) throws Exception {
        if ((query == null) || ("".equals(query))) {
            if ((sort == null) || ("".equals(sort))) {
                model.addAttribute("deposits", restService.getDepositsListingAll());
            } else {
                model.addAttribute("deposits", restService.getDepositsListingAll(sort));
            }
            model.addAttribute("query", "");
        } else {
            if ((sort == null) || ("".equals(sort))) {
                model.addAttribute("deposits", restService.searchDeposits(query));
            } else {
                model.addAttribute("deposits", restService.searchDeposits(query, sort));
            }
            model.addAttribute("query", query);
        }

        return "admin/deposits/index";
    }
    
    @RequestMapping(value = "/admin/deposits/csv", method = RequestMethod.GET)
    public void exportVaults(HttpServletResponse response,
                               @RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "sort", required = false) String sort,
                               @RequestParam(value = "order", required = false) String order) throws Exception {
        
        List<DepositInfo> deposits = null;
        if ((query == null) || ("".equals(query))) {
            if ((sort == null) || ("".equals(sort))) {
            	 DepositsData depositData =restService.getDepositsListingAllData();
            	 deposits = depositData.getData();
            } else {
            	DepositsData depositData = restService.getDepositsListingAllData(sort);
            	deposits = depositData.getData();
            }
           
        } else {
            if ((sort == null) || ("".equals(sort))) {
            	DepositsData depositData = restService.searchDepositsData(query);
            	deposits = depositData.getData();
            } else {
            	DepositsData depositData =restService.searchDepositsData(query, sort);
            	deposits = depositData.getData();
            }
           
        }

        
        response.setContentType("text/csv");

        // creates mock data
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=\"deposits.csv\"";
        response.setHeader(headerKey, headerValue);

        String[] header = { "Deposit name", "Size","Date Deposited", "Status", "Depositor", "Vault Name", "Pure Record ID", "School","Deposit ID" ,"Vault ID","Vault Owner","Vault Review Date"};

        String[] fieldMapping = { "name", "sizeStr", "creationTime", "status", "userName", "vaultName","datasetID","groupName", "ID", "vaultID","vaultOwnerName","vaultReviewDate"};

        try {
            // uses the Super CSV API to generate CSV data from the model data
            ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

            csvWriter.writeHeader(header);

            for (DepositInfo aDeposit : deposits) {
                csvWriter.write(aDeposit, fieldMapping);
            }

            csvWriter.close();

        } catch (Exception e){
            logger.error("IOException: "+e);
            e.printStackTrace();
        }
    }

    
    @RequestMapping(value = "/admin/deposits/{depositID}", method = RequestMethod.DELETE)
    @ResponseBody
    public String deleteDeposit(ModelMap model, @PathVariable("depositID") String depositID,
    		@RequestParam(value = "vaultId", required = false) String vaultId) throws Exception {
     
    	restService.deleteDeposit(depositID);
        return "vaults/"+vaultId+"/deposits/"+ depositID;
    }
}


