package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.response.AuditChunkStatusInfo;
import org.datavaultplatform.common.response.AuditInfo;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public String getDepositsAuditsListing(ModelMap model) throws Exception {
        AuditInfo[] audits = restService.getAuditsListingAll();

        List<Map<String,Object>> deposits = new ArrayList<>();

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

        System.out.println("Deposits list size: "+deposits.size());

        model.addAttribute("deposits", deposits);

        return "admin/audits/deposits";
    }
}


