package org.datavaultplatform.webapp.controllers.admin;


import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.DepositsData;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.response.AuditChunkStatusInfo;
import org.datavaultplatform.common.response.AuditInfo;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * User: Stuart Lewis
 * Date: 27/09/2015
 */

@ConditionalOnBean(RestService.class)
@Controller
public class AdminDepositsController {

    private final RestService restService;
    private static final int DEFAULT_RECORDS_PER_PAGE = 10;

    private static final Logger logger = LoggerFactory.getLogger(AdminDepositsController.class);

    @Autowired
    public AdminDepositsController(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/deposits", method = RequestMethod.GET)
    public String getDepositsListing(ModelMap model,
                                     @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                     @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                                     @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                     @RequestParam(value = "pageId", defaultValue = "1") int pageId)
            throws Exception {

        model.addAttribute("activePageId", pageId);

        // calculate offset which is passed to the service to fetch records from that row Id
        int offset = (pageId-1) * DEFAULT_RECORDS_PER_PAGE;

        model.addAttribute("offset", offset);

        DepositInfo[] deposits = restService.getDepositsListingAll(query, sort, order, offset, DEFAULT_RECORDS_PER_PAGE);

        int totalDeposits = restService.getTotalDepositsCount(query);

        int numberOfPages = (int)Math.ceil((double) deposits.length/DEFAULT_RECORDS_PER_PAGE);

        model.addAttribute("query", "");
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("deposits", deposits);
        model.addAttribute("query", query);
        model.addAttribute("recordPerPage", DEFAULT_RECORDS_PER_PAGE);
        model.addAttribute("totalRecords", totalDeposits);
        model.addAttribute("totalPages", (int)Math.ceil((double)totalDeposits/DEFAULT_RECORDS_PER_PAGE));

        model.addAttribute("numberOfPages", numberOfPages);

        String otherOrder = order.equals("asc")?"desc":"asc";
        model.addAttribute("orderName", "name".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderDepositSize", "depositSize".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderCreationTime", "creationTime".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderStatus", "status".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderUserID", "userID".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderId", "id".equals(sort)?otherOrder:"asc");
        model.addAttribute("orderVaultId", "vaultID".equals(sort)?otherOrder:"asc");

        return "admin/deposits/index";
    }

    @RequestMapping(value = "/admin/deposits/csv", method = RequestMethod.GET)
    public void exportVaults(HttpServletResponse response,
                             @RequestParam(value = "query", required = false, defaultValue = "") String query,
                             @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                             @RequestParam(value = "order", required = false, defaultValue = "desc") String order) throws Exception {

        List<DepositInfo> deposits = null;

        DepositsData depositData =restService.searchDepositsData(query, sort, order);
        deposits = depositData.getData();

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

    @RequestMapping(value = "/admin/deposits/audit", method = RequestMethod.GET)
    public String runDepositAudit() throws Exception{

        String result = restService.auditDeposits();

        return "admin/deposits/index";
    }

    @RequestMapping(value = "/admin/audits", method = RequestMethod.GET)
    public String getAuditsListing(ModelMap model) throws Exception {
        AuditInfo[] audits = restService.getAuditsListingAll();

        model.addAttribute("audits", audits);

        return "admin/audits/index";
    }

    @RequestMapping(value = "/admin/depositsAudits", method = RequestMethod.GET)
    public String getDepositsAuditsListing(ModelMap model,
                                           @RequestParam(value = "sort", required = false) String sort)
            throws Exception {
        AuditInfo[] audits = restService.getAuditsListingAll();

        List<Map<String,Object>> deposits = new ArrayList<>();

        if (sort == null) {
            sort = "date";
        }
        model.addAttribute("sort", sort);

        for(AuditInfo audit : audits){
//            System.out.println("Deposit Map size: "+deposits.size());
//            System.out.println("Audit: "+audit.getId());
            List<AuditChunkStatusInfo> auditChunks = audit.getAuditChunks();

            for(AuditChunkStatusInfo auditChunk : auditChunks){
//                System.out.println("Audit Chunk: "+auditChunk.getID());
                Deposit deposit = auditChunk.getDeposit();
//                System.out.println("Deposit: "+deposit.getID());
                Map<String, Object> mapDeposit = new HashMap<>();
                Optional<Map<String, Object>> result = deposits.stream()
                        .filter(m -> ((Deposit)m.get("deposit")).getID().equals(deposit.getID()))
                        .findAny();
                if(result.isPresent()){
//                    System.out.println("Deposit already in map");
                    mapDeposit = result.get();
                }else{
//                    System.out.println("Create new deposit for map");
                    mapDeposit.put("deposit", deposit);
                    List<Map<String, Object>> chunkInfoList = new ArrayList<>();
                    for(DepositChunk depositChunk : deposit.getDepositChunks()){
//                        System.out.println("\t add deposit chunk: "+depositChunk.getID());
                        Map<String, Object> chunkInfo = new HashMap<>();
                        chunkInfo.put("deposit_chunk", depositChunk);
                        chunkInfoList.add(chunkInfo);
                    }

                    if(sort.equals("chunkNum")){
                        chunkInfoList.sort(Comparator.comparing(m ->
                                        ((DepositChunk)m.get("deposit_chunk")).getChunkNum(),
                                Comparator.nullsLast(Comparator.naturalOrder())));
                    }

                    mapDeposit.put("chunks_info", chunkInfoList);
                    deposits.add(mapDeposit);
                }
                List<Map<String, Object>> chunkInfoList = (List<Map<String, Object>>)mapDeposit.get("chunks_info");
//                System.out.println("chunkInfoList size: "+chunkInfoList.size());
                result = chunkInfoList.stream()
                        .filter(m -> ((DepositChunk)m.get("deposit_chunk")).getID()
                                .equals(auditChunk.getDepositChunk().getID()))
                        .findAny();
                if(result.isPresent()){
//                    System.err.println("add last_audit_chunk: "+auditChunk.getID());
                    Map<String, Object> chunkInfo = result.get();
                    chunkInfo.put("last_audit_chunk", auditChunk);

                }else{
                    System.err.println("Chunk missing from deposit");
                }
            }
        }

        if(sort.equals("chunkStatus")){
            for(Map<String,Object> deposit : deposits){
                List<Map<String, Object>> chunkInfoList = (List<Map<String, Object>>)deposit.get("chunks_info");
                chunkInfoList.sort(Comparator.comparing(m ->
                        ((AuditChunkStatusInfo)m.get("last_audit_chunk")).getStatus(),
                        Comparator.nullsLast(Comparator.naturalOrder())));
            }
        }

        System.out.println("Deposits list size: "+deposits.size());

        model.addAttribute("deposits", deposits);

        return "admin/audits/deposits";
    }
}


