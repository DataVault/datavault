package org.datavaultplatform.broker.services;

import java.util.*;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAOImpl;
import org.datavaultplatform.common.model.dao.DepositChunkDAO;
import org.datavaultplatform.common.model.dao.DepositDAO;

public class DepositsService {

    private int auditPeriodMinutes = 0;
    private int auditPeriodHours = 0;
    private int auditPeriodDays = 0;
    private int auditPeriodMonths = 0;
    private int auditPeriodYears = 2;
    private int auditMaxChunksPerDeposits = 50;
    private int auditMaxTotalChunks = 2000;

    private DepositDAO depositDAO;
    private DepositChunkDAO depositChunkDAO;
    private AuditChunkStatusDAO auditChunkStatusDAO;
    
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
    public void setDepositChunkDAO(DepositChunkDAO depositChunkDAO) { this.depositChunkDAO = depositChunkDAO; }
    public void setAuditChunkStatusDAO(AuditChunkStatusDAO auditChunkStatusDAO) { this.auditChunkStatusDAO = auditChunkStatusDAO; }

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
        int maxChunkAuditPerDeposit = getAuditMaxChunksPerDeposits();
        int maxChunkPerAudit = getAuditMaxTotalChunks();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -getAuditPeriodMinutes()); // to get previous days
        cal.add(Calendar.HOUR, -getAuditPeriodHours()); // to get previous days
        cal.add(Calendar.DAY_OF_YEAR, -getAuditPeriodDays()); // to get previous days
        cal.add(Calendar.MONTH, -getAuditPeriodMonths()); // to get previous month
        cal.add(Calendar.YEAR, -getAuditPeriodYears()); // to get previous years
        Date olderThanDate = cal.getTime();

        System.out.println("older than date: "+olderThanDate);

        List<Deposit> deposits = depositDAO.getDepositsWaitingForAudit(olderThanDate);
        List<DepositChunk> chunksToAudit = new ArrayList<DepositChunk>();

        int totalCount = 0;
        for(Deposit deposit : deposits){
            System.out.println("check deposit: "+deposit.getID());

            List<DepositChunk> depositChunks = deposit.getDepositChunks();

            int count = 0;
            for(DepositChunk chunk : depositChunks){
                System.out.println("check chunk: "+chunk.getID());

                AuditChunkStatus auditChunkInfo = auditChunkStatusDAO.getLastChunkAuditTime(chunk);

                if(auditChunkInfo == null || auditChunkInfo.getTimestamp().before(olderThanDate)){
                    System.out.println("add chunk");
                    chunksToAudit.add(chunk);
                    count++; totalCount++;
                }

                if(totalCount >= maxChunkPerAudit) {
                    System.out.println("maxChunkPerAudit reached");
                    return chunksToAudit;
                }else if(count >= maxChunkAuditPerDeposit){
                    System.out.println("maxChunkAuditPerDeposit reached");
                    break; // get out of loop
                }
            }
        }

        return chunksToAudit;
    }

    public int getAuditPeriodMinutes() {
        return auditPeriodMinutes;
    }

    public void setAuditPeriodMinutes(int auditPeriodMinutes) {
        this.auditPeriodMinutes = auditPeriodMinutes;
    }

    public int getAuditPeriodHours() {
        return auditPeriodHours;
    }

    public void setAuditPeriodHours(int auditPeriodHours) {
        this.auditPeriodHours = auditPeriodHours;
    }

    public int getAuditPeriodDays() {
        return auditPeriodDays;
    }

    public void setAuditPeriodDays(int auditPeriodDays) {
        this.auditPeriodDays = auditPeriodDays;
    }

    public int getAuditPeriodMonths() {
        return auditPeriodMonths;
    }

    public void setAuditPeriodMonths(int auditPeriodMonths) {
        this.auditPeriodMonths = auditPeriodMonths;
    }

    public int getAuditPeriodYears() {
        return auditPeriodYears;
    }

    public void setAuditPeriodYears(int auditPeriodYears) {
        this.auditPeriodYears = auditPeriodYears;
    }

    public int getAuditMaxChunksPerDeposits() {
        return auditMaxChunksPerDeposits;
    }

    public void setAuditMaxChunksPerDeposits(int auditMaxChunksPerDeposits) {
        this.auditMaxChunksPerDeposits = auditMaxChunksPerDeposits;
    }

    public int getAuditMaxTotalChunks() {
        return auditMaxTotalChunks;
    }

    public void setAuditMaxTotalChunks(int auditMaxTotalChunks) {
        this.auditMaxTotalChunks = auditMaxTotalChunks;
    }

    public DepositChunk getDepositChunkById(String id){
        return depositChunkDAO.findById(id);
    }

    public void updateDepositChunk(DepositChunk chunk) {
        depositChunkDAO.update(chunk);
    }
}

