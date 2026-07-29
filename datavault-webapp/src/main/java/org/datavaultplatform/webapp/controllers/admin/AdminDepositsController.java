package org.datavaultplatform.webapp.controllers.admin;


import jakarta.servlet.http.HttpServletResponse;

import org.datavaultplatform.common.response.*;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
public class AdminDepositsController implements AdminDepositsControllerApi {

    private final RestService restService;
    private static final int DEFAULT_RECORDS_PER_PAGE = 10;
    private static final int MAX_RESULTS_FOR_DEPOSITS_CSV = 300;

    private static final Logger logger = LoggerFactory.getLogger(AdminDepositsController.class);

    @Autowired
    public AdminDepositsController(RestService restService) {
        this.restService = restService;
    }

    @Override
    @GetMapping(value = "/admin/deposits", produces = MediaType.TEXT_HTML_VALUE)
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

    @Override
    @GetMapping(value = "/admin/deposits/csv", produces = ResponseType.TEXT_CSV_VALUE)
    public void exportVaults(HttpServletResponse response,
                             @RequestParam(value = "query", required = false, defaultValue = "") String query,
                             @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                             @RequestParam(value = "order", required = false, defaultValue = "desc") String order) throws Exception {

        List<DepositInfo> deposits;

        DepositsData depositData =restService.limitedSearchDepositsData(query, sort, order, 0, MAX_RESULTS_FOR_DEPOSITS_CSV);
        deposits = depositData.getData();

        response.setContentType(ResponseType.TEXT_CSV_VALUE);
        

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
            logger.error("Unexpected Exception",e);
        }
    }

    /**
     * Deletes a specific deposit and returns the redirect path to the vault's deposit index.
     * <p>
     * This method coordinates with the administrative view at 
     * {@code WEB-INF/templates/admin/deposits/index.html}.
     * @param depositId specifies the deposit to be removed.
     * @param vaultId the id of the vault that contains this deposit.
     * required to construct the result.
     * @return a URL pointing back to the deposit page.
     */
    @Override
    @DeleteMapping(value = "/admin/deposits/{depositId}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String deleteDeposit(@PathVariable String depositId, @RequestParam(value = "vaultId", required = false) String vaultId) {
        restService.deleteDeposit(depositId);
        return "vaults/" + vaultId + "/deposits/" + depositId;
    }

    @Override
    @GetMapping(value = "/admin/deposits/audit", produces = MediaType.TEXT_HTML_VALUE)
    public String runDepositAudit() {

        String result = restService.auditDeposits();

        return "admin/deposits/index";
    }

    @Override
    @GetMapping(value = "/admin/audits", produces = MediaType.TEXT_HTML_VALUE)
    public String getAuditsListing(ModelMap model) throws Exception {
        AuditInfo[] audits = restService.getAuditsListingAll();

        model.addAttribute("audits", audits);

        return "admin/audits/index";
    }

    @Override
    @GetMapping(value = "/admin/depositsAudits", produces = MediaType.TEXT_HTML_VALUE)
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
//            logger.info("Deposit Map size: "+deposits.size());
//            logger.info("Audit: "+audit.getId());
            List<AuditChunkStatusInfo> auditChunks = audit.getAuditChunks();

            for(AuditChunkStatusInfo auditChunk : auditChunks){
//                logger.info("Audit Chunk: "+auditChunk.getID());
                Deposit deposit = auditChunk.getDeposit();
//                logger.info("Deposit: "+deposit.getID());
                Map<String, Object> mapDeposit = new HashMap<>();
                Optional<Map<String, Object>> result = deposits.stream()
                        .filter(m -> ((Deposit)m.get("deposit")).getID().equals(deposit.getID()))
                        .findAny();
                if(result.isPresent()){
//                    logger.info("Deposit already in map");
                    mapDeposit = result.get();
                }else{
//                    logger.info("Create new deposit for map");
                    mapDeposit.put("deposit", deposit);
                    List<Map<String, Object>> chunkInfoList = new ArrayList<>();
                    for(DepositChunk depositChunk : deposit.getDepositChunks()){
//                        logger.info("\t add deposit chunk: "+depositChunk.getID());
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
//                logger.info("chunkInfoList size: "+chunkInfoList.size());
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

        logger.info("Deposits list size: "+deposits.size());

        model.addAttribute("deposits", deposits);

        return "admin/audits/deposits";
    }
}


