package org.datavaultplatform.broker.services;

import java.util.*;

import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.DepositDAO;

public class DepositsService {

    private DepositDAO depositDAO;
    
    public List<Deposit> getDeposits(String sort) {
        return depositDAO.list(sort);
    }
    
    public void addDeposit(Vault vault,
                           Deposit deposit,
                           String shortPath,
                           String origin) {
        
        Date d = new Date();
        deposit.setCreationTime(d);
        
        deposit.setVault(vault);
        deposit.setStatus(Deposit.Status.NOT_STARTED);
        
        // Set display values for the deposit path/origin
        deposit.setShortFilePath(shortPath);
        deposit.setFileOrigin(origin);
        
        // Generate a new UUID for this Bag.
        deposit.setBagId(UUID.randomUUID().toString());
        
        depositDAO.save(deposit);
    }
    
    public void updateDeposit(Deposit deposit) {
        depositDAO.update(deposit);
    }
    
    public Deposit getDeposit(String depositID) {
        return depositDAO.findById(depositID);
    }
    
    public void setDepositDAO(DepositDAO depositDAO) { this.depositDAO = depositDAO; }

    public int count() { return depositDAO.count(); }

    public int queueCount() { return depositDAO.queueCount(); }

    public int inProgressCount() { return depositDAO.inProgressCount(); }

    public List<Deposit> inProgress() { return depositDAO.inProgress(); }

    public List<Deposit> completed() { return depositDAO.completed(); }

    public List<Deposit> search(String query, String sort) { return this.depositDAO.search(query, sort); }

    public Long size() { return depositDAO.size(); }
    
    // Get the specified Deposit object and validate it against the current User and Vault
    public Deposit getUserDeposit(User user, String depositID) throws Exception {
        
        Deposit deposit = getDeposit(depositID);
        
        if (deposit == null) {
            throw new Exception("Invalid Deposit ID");
        }
        
        Vault vault = deposit.getVault();
        
        if (vault == null) {
            throw new Exception("Vault does not exist");
        }
        
        Boolean userVault = false;
        if (vault.getUser().equals(user)) {
            userVault = true;
        }
        
        Boolean groupOwner = false;
        if (vault.getGroup().getOwners().contains(user)) {
            groupOwner = true;
        }
        
        Boolean adminUser = user.isAdmin();
        
        if (!userVault && !groupOwner && !adminUser) {
            throw new Exception("Access denied");
        }

        return deposit;
    }
    
    public void deleteDeposit(String depositId) {
    	Deposit deposit = depositDAO.findById(depositId);
    	deposit.setSize(0);
    	depositDAO.update(deposit);
    }

    public List<DepositChunk> getChunksForAudit(){
        // TODO: get value from config
        int month = 2;
        int maxChunkAuditPerDeposit = 50;
        int maxChunkPerAudit = 2000;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -month); // to get previous years
        Date olderThanDate = cal.getTime();

//        System.out.println("older than date: "+olderThanDate);

        List<Deposit> deposits = depositDAO.getDepositsWaitingForAudit(olderThanDate);
        List<DepositChunk> chunksToAudit = new ArrayList<DepositChunk>();

        int totalCount = 0;
        for(Deposit deposit : deposits){
//            System.out.println("check deposit: "+deposit.getID());

            List<DepositChunk> depositChunks = deposit.getDepositChunks();

            int count = 0;
            for(DepositChunk chunk : depositChunks){
//                System.out.println("check chunk: "+chunk.getID());

                Date lastAuditTime = chunk.getLastAuditTime();

                if(lastAuditTime == null || lastAuditTime.before(olderThanDate)){
//                    System.out.println("add chunk");
                    chunksToAudit.add(chunk);
                }

                count++; totalCount++;
                if(totalCount >= maxChunkPerAudit) {
//                    System.out.println("maxChunkPerAudit reached");
                    return chunksToAudit;
                }else if(count >= maxChunkAuditPerDeposit){
//                    System.out.println("maxChunkAuditPerDeposit reached");
                    break; // get out of loop
                }
            }
        }

        return chunksToAudit;
    }
}

