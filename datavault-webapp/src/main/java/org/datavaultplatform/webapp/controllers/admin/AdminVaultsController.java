package org.datavaultplatform.webapp.controllers.admin;


import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * User: Stuart Lewis
 * Date: 27/09/2015
 */

@ConditionalOnBean(RestService.class)
@Controller
public class AdminVaultsController {

	private static final Logger logger = LoggerFactory.getLogger(AdminVaultsController.class);
	
	private static final String _0 = "0";
	private static final int MAX_RECORDS_PER_PAGE = 10;

    private final RestService restService;

    @Autowired
    public AdminVaultsController(RestService restService) {
        this.restService = restService;
    }


    @RequestMapping(value = "/admin/vaults", method = RequestMethod.GET)
    public String searchVaults(ModelMap model,
                               @RequestParam(value = "query", defaultValue = "") String query,
                               @RequestParam(value = "sort", defaultValue = "creationTime") String sort,
                               @RequestParam(value = "order", defaultValue = "desc") String order,
                               @RequestParam(value = "pageId", defaultValue = "1") int pageId) throws Exception {

        model.addAttribute("activePageId", pageId);

        // calculate offset which is passed to the service to fetch records from that row Id
        int offset = (pageId-1) * MAX_RECORDS_PER_PAGE;

        VaultsData filteredVaultsData = restService.searchVaults(query, sort, order, offset, MAX_RECORDS_PER_PAGE);
        int filteredRecordsTotal = filteredVaultsData.getRecordsFiltered();
        int numberOfPages = (int)Math.ceil((double)filteredRecordsTotal/MAX_RECORDS_PER_PAGE);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("vaults", filteredVaultsData.getData());
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
        model.addAttribute("ordervaultsize", "vaultSize".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderuser", "user".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderGroupId", "groupID".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderCrisId", "crisID".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderreviewDate", "reviewDate".equals(sort)?otherOrder:"asc");
        model.addAttribute("ordercreationtime", "creationTime".equals(sort)?otherOrder:"asc");

        return "admin/vaults/index";
    }

	private String constructTableRecordsInfo(int offset, int recordsTotal, int filteredRecords, int numberOfRecordsonPage, boolean isFiltered) {
		StringBuilder recordsInfo = new StringBuilder();
		recordsInfo.append("Showing ").append(offset + 1).append(" - ").append(offset + numberOfRecordsonPage);
		if(isFiltered) {
			recordsInfo.append(" vaults of ").append(filteredRecords)
                    .append(" (").append("filtered from ").append(recordsTotal).append(" total vaults)");
		} else {
			recordsInfo.append(" vaults of ").append(recordsTotal);
		}
		return recordsInfo.toString();
	}

    @RequestMapping(value = "/admin/vaults/csv", method = RequestMethod.GET)
    public void exportVaults(HttpServletResponse response,
                               @RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "sort", required = false) String sort,
                               @RequestParam(value = "order", required = false) String order) throws Exception {
        String theSort = sort;
        String theOrder = order;
        if (sort == null) theSort = "creationTime";
        if (order == null) theOrder = "desc";

        List<VaultInfo> vaults;

        VaultsData vaultData =  restService.searchVaults(query, theSort, theOrder, 0, Integer.MAX_VALUE);
        vaults = vaultData.getData();

        response.setContentType("text/csv");

        // creates mock data
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=\"vaults.csv\"";
        response.setHeader(headerKey, headerValue);

        String[] header = { "Vault name", "Deposits","Vault description", "Vault Size", "User ID", "User Name", "School", "Review Date","Creation Time" };

        String[] fieldMapping = { "name", "NumberOfDeposits", "description", "sizeStr", "userID", "userName","groupID","reviewDate", "CreationTime" };

        try {
            // uses the Super CSV API to generate CSV data from the model data
            ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

            csvWriter.writeHeader(header);

            for (VaultInfo aVault : vaults) {
                csvWriter.write(aVault, fieldMapping);
            }

            csvWriter.close();

        } catch (Exception e){
            logger.error("Unexpected Exception", e);
        }
    }

    @RequestMapping(value = "/admin/vaults/{vaultid}", method = RequestMethod.GET)
    public String showVault(ModelMap model, @PathVariable("vaultid") String vaultID) throws Exception {
        VaultInfo vault = restService.getVault(vaultID);

        model.addAttribute("vault", vault);
       
        model.addAttribute(restService.getRetentionPolicy(vault.getPolicyID()));
        model.addAttribute(restService.getGroup(vault.getGroupID()));
        model.addAttribute("deposits", restService.getDepositsListing(vaultID));
        

        return "admin/vaults/vault";
    }

    @RequestMapping(value = "/admin/vaults/{vaultid}/checkretentionpolicy", method = RequestMethod.POST)
    public String checkPolicy(ModelMap model, @PathVariable("vaultid") String vaultID) throws Exception {
        restService.checkVaultRetentionPolicy(vaultID);

        return "redirect:/admin/vaults/" + vaultID;
    }
}


