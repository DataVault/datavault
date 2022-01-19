package org.datavaultplatform.webapp.controllers.admin;


import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException; 
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.EventInfo;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.webapp.exception.ForbiddenException;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewHistoryModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;


@Controller
public class AdminPendingVaultsController {

	private static final Logger logger = LoggerFactory.getLogger(AdminPendingVaultsController.class);
	
	private static final String _0 = "0";
	private static final int MAX_RECORDS_PER_PAGE = 5;

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }
    
    
    @RequestMapping(value = "/admin/pendingVaults", method = RequestMethod.GET)
    public String searchPendingVaults(ModelMap model,
                               @RequestParam(value = "query", defaultValue = "") String query,
                               @RequestParam(value = "sort", defaultValue = "creationTime") String sort,
                               @RequestParam(value = "order", defaultValue = "desc") String order,
                               @RequestParam(value = "pageId", defaultValue = "1") int pageId) throws Exception {

        model.addAttribute("activePageId", pageId);

        // calculate offset which is passed to the service to fetch records from that row Id
        int offset = (pageId-1) * MAX_RECORDS_PER_PAGE;

        VaultsData filteredVaultsData = restService.searchPendingVaults(query, sort, order, offset, MAX_RECORDS_PER_PAGE, null);
        int filteredRecordsTotal = filteredVaultsData.getRecordsFiltered();
        int numberOfPages = (int)Math.ceil((double)filteredRecordsTotal/MAX_RECORDS_PER_PAGE);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("pendingVaults", filteredVaultsData.getData());

        model.addAttribute("query", query);

        boolean isFiltered = query != null  && !query.equals("");
        model.addAttribute("recordsInfo",
                constructTableRecordsInfo(
                        offset,
                        filteredVaultsData.getRecordsTotal(),
                        filteredRecordsTotal,
                        filteredVaultsData.getData().size(),

                        isFiltered
                ) );

        // Pass the sort and order
        if (sort == null) sort = "";
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        String otherOrder = order.equals("asc")?"desc":"asc";
        model.addAttribute("ordername", "name".equals(sort)?otherOrder:"asc");

        return "admin/pendingVaults/index";
    }

    @RequestMapping(value = "/admin/pendingVaults/saved", method = RequestMethod.GET)
    public String searchSavedPendingVaults(ModelMap model,
                                      @RequestParam(value = "query", defaultValue = "") String query,
                                      @RequestParam(value = "sort", defaultValue = "creationTime") String sort,
                                      @RequestParam(value = "order", defaultValue = "desc") String order,
                                      @RequestParam(value = "pageId", defaultValue = "1") int pageId) throws Exception {

        model.addAttribute("activePageId", pageId);

        // calculate offset which is passed to the service to fetch records from that row Id
        int offset = (pageId-1) * MAX_RECORDS_PER_PAGE;

        VaultsData filteredVaultsData = restService.searchPendingVaults(query, sort, order, offset, MAX_RECORDS_PER_PAGE, false);
        int filteredRecordsTotal = filteredVaultsData.getRecordsFiltered();
        int numberOfPages = (int)Math.ceil((double)filteredRecordsTotal/MAX_RECORDS_PER_PAGE);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("pendingVaults", filteredVaultsData.getData());

        model.addAttribute("query", query);

        boolean isFiltered = query != null  && !query.equals("");
        model.addAttribute("recordsInfo",
                constructTableRecordsInfo(
                        offset,
                        filteredVaultsData.getRecordsTotal(),
                        filteredRecordsTotal,
                        filteredVaultsData.getData().size(),

                        isFiltered
                ) );

        // Pass the sort and order
        if (sort == null) sort = "";
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        String otherOrder = order.equals("asc")?"desc":"asc";
        model.addAttribute("ordername", "name".equals(sort)?otherOrder:"asc");

        return "admin/pendingVaults/saved";
    }

    @RequestMapping(value = "/admin/pendingVaults/confirmed", method = RequestMethod.GET)
    public String searchConfirmedPendingVaults(ModelMap model,
                                      @RequestParam(value = "query", defaultValue = "") String query,
                                      @RequestParam(value = "sort", defaultValue = "creationTime") String sort,
                                      @RequestParam(value = "order", defaultValue = "desc") String order,
                                      @RequestParam(value = "pageId", defaultValue = "1") int pageId) throws Exception {

        model.addAttribute("activePageId", pageId);

        // calculate offset which is passed to the service to fetch records from that row Id
        int offset = (pageId-1) * MAX_RECORDS_PER_PAGE;

        VaultsData filteredVaultsData = restService.searchPendingVaults(query, sort, order, offset, MAX_RECORDS_PER_PAGE, true);
        int filteredRecordsTotal = filteredVaultsData.getRecordsFiltered();
        int numberOfPages = (int)Math.ceil((double)filteredRecordsTotal/MAX_RECORDS_PER_PAGE);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("pendingVaults", filteredVaultsData.getData());

        model.addAttribute("query", query);

        boolean isFiltered = query != null  && !query.equals("");
        model.addAttribute("recordsInfo",
                constructTableRecordsInfo(
                        offset,
                        filteredVaultsData.getRecordsTotal(),
                        filteredRecordsTotal,
                        filteredVaultsData.getData().size(),

                        isFiltered
                ) );

        // Pass the sort and order
        if (sort == null) sort = "";
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        String otherOrder = order.equals("asc")?"desc":"asc";
        model.addAttribute("ordername", "name".equals(sort)?otherOrder:"asc");

        return "admin/pendingVaults/confirmed";
    }
    
    
    @RequestMapping(value = "/admin/pendingVaults/summary/{pendingVaultId}", method = RequestMethod.GET)
    public String getVault(ModelMap model, @PathVariable("pendingVaultId") String vaultID, Principal principal) {
        logger.info("VaultID:'" + vaultID + "'");
        VaultInfo pendingVault = restService.getPendingVault(vaultID);
        logger.info("pendingVault.id:'" + pendingVault.getID() + "'");
        model.addAttribute("pendingVault", pendingVault);
        
        CreateRetentionPolicy createRetentionPolicy = null;
        if (pendingVault.getPolicyID() != null) {
            createRetentionPolicy = restService.getRetentionPolicy(pendingVault.getPolicyID());
        }
        model.addAttribute("createRetentionPolicy", createRetentionPolicy);
        
        Group group = restService.getGroup(pendingVault.getGroupID());
        model.addAttribute("group", group);

        return "admin/pendingVaults/summary";
    }

    @RequestMapping(value = "/admin/pendingVaults/upgrade/{pendingVaultId}", method = RequestMethod.GET)
    public String upgradeVault(@PathVariable("pendingVaultId") String pendingVaultID) {
        // need to either pass in create vault or get vaultnfo from the pending id param
        // and convert it to create vault object like in VaultController.getPendingVault
        VaultInfo pendingVault = restService.getPendingVault(pendingVaultID);
        CreateVault cv = pendingVault.convertToCreate();
        logger.info("Attempting to upgrade pending vault to full vault");
        VaultInfo newVault = restService.addVault(cv);
        logger.info("Completed upgrading pending vault to full vault");
        //need to add full vault datacreators, roles etc.
        // any other new info too pure stuff billing etc.
        // then delete the pending vault
        restService.deletePendingVault(cv.getPendingID());
        String vaultUrl = "/vaults/" + newVault.getID() + "/";
        return "redirect:" + vaultUrl;
    }

/*
    @RequestMapping(value = "/admin/pendingVaults/addVault/{pendingVaultId}", method = RequestMethod.GET)
    public String addVault(@PathVariable("pendingVaultId") String pendingVaultID,
    	                   @RequestParam("reviewDate") String reviewDateString) {
    	logger.info("pendingVaultID: " + pendingVaultID);
    	logger.info("reviewDateString: " + reviewDateString);
    	
    	try {
    		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    		Date reviewDate = new SimpleDateFormat("dd-MMM-yyy").parse(reviewDateString);

    		boolean vaultAdded = restService.addVaultForPendingVault(pendingVaultID, reviewDate);
    		logger.info("vaultAdded: " + vaultAdded);
        
    	} catch(ParseException pe ) {
    		logger.info("Parse error: " + pe);
    	}
    	
//    	if ("cancel".equals(action)) {
//            return "redirect:/admin/pendingVaults";
//        }

        return "redirect:/admin/pendingVaults";
    }

*/

    /*
    @RequestMapping(value = "/admin/pendingVaults/delete/{pendingVaultId}", method = RequestMethod.GET)
    public String deletePendingVault(@PathVariable("pendingVaultId") String pendingVaultID) {
    	logger.info("pendingVaultID: " + pendingVaultID);
        PendingVault pendingVault = restService.getPendingVaultRecord(vaultID);
        model.addAttribute("pendingVault", pendingVault);

        return "redirect:/admin/pendingVaults";
    }*/

    @RequestMapping(value = "/admin/pendingVaults/{pendingVaultID}", method = RequestMethod.GET)
    public String deletePendingVault(ModelMap model, @PathVariable("pendingVaultID") String pendingVaultID) throws Exception {

        restService.deletePendingVault(pendingVaultID);
        return "redirect:/admin/pendingVaults";
    }

    private String constructTableRecordsInfo(int offset, int recordsTotal, int filteredRecords, int numberOfRecordsonPage, boolean isFiltered) {
		StringBuilder recordsInfo = new StringBuilder();
		recordsInfo.append("Showing ").append(offset + 1).append(" - ").append(offset + numberOfRecordsonPage);
		if(isFiltered) {
			recordsInfo.append(" pending vaults of ").append(filteredRecords)
                    .append(" (").append("filtered from ").append(recordsTotal).append(" total pending vaults)");
		} else {
			recordsInfo.append(" pending vaults of ").append(recordsTotal);
		}
		return recordsInfo.toString();
	}
    
}


