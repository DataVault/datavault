package org.datavaultplatform.broker.services;

import java.util.*;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.DepositChunkDAO;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DepositsService {

    private static final Logger logger = LoggerFactory.getLogger(DepositsService.class);

    private final int auditPeriodMinutes;
    private final int auditPeriodHours;
    private final int auditPeriodDays;
    private final int auditPeriodMonths;
    private final int auditPeriodYears;
    private final int auditMaxChunksPerDeposits;
    private final int auditMaxTotalChunks;

    private final DepositDAO depositDAO;
    private final DepositChunkDAO depositChunkDAO;
    private final AuditChunkStatusDAO auditChunkStatusDAO;

    /*
        <property name="depositDAO" ref="depositDAO" />
        <property name="depositChunkDAO" ref="depositChunkDAO" />
        <property name="auditChunkStatusDAO" ref="auditChunkStatusDAO" />
        <property name="auditPeriodMinutes" value="${audit.period.minutes:0}"/>
        <property name="auditPeriodHours" value="${audit.period.hours:0}"/>
        <property name="auditPeriodDays" value="${audit.period.days:0}"/>
        <property name="auditPeriodMonths" value="${audit.period.months:0}"/>
        <property name="auditPeriodYears" value="${audit.period.years:2}"/>
        <property name="auditMaxChunksPerDeposits" value="${audit.maxChunksPerDeposits:50}"/>
        <property name="auditMaxTotalChunks" value="${audit.maxTotalChunks:2000}"/>
     */
    @Autowired
    public DepositsService(
        DepositDAO depositDAO,
        DepositChunkDAO depositChunkDAO,
        AuditChunkStatusDAO auditChunkStatusDAO,
        @Value("${audit.period.minutes:0}") int auditPeriodMinutes,
        @Value("${audit.period.hours:0}") int auditPeriodHours,
        @Value("${audit.period.days:0}") int auditPeriodDays,
        @Value("${audit.period.months:0}") int auditPeriodMonths,
        @Value("${audit.period.years:2}") int auditPeriodYears,
        @Value("${audit.maxChunksPerDeposits:50}") int auditMaxChunksPerDeposits,
        @Value("${audit.maxTotalChunks:2000}") int auditMaxTotalChunks){
        this.depositDAO = depositDAO;
        this.depositChunkDAO = depositChunkDAO;
        this.auditChunkStatusDAO = auditChunkStatusDAO;
        this.auditPeriodMinutes = auditPeriodMinutes;
        this.auditPeriodHours = auditPeriodHours;
        this.auditPeriodDays = auditPeriodDays;
        this.auditPeriodMonths = auditPeriodMonths;
        this.auditPeriodYears = auditPeriodYears;
        this.auditMaxChunksPerDeposits = auditMaxChunksPerDeposits;
        this.auditMaxTotalChunks  = auditMaxTotalChunks;
    }

    public List<Deposit> getDeposits(String query, String userId, String sort, String order, int offset, int maxResult) {
        return depositDAO.list(query, userId, sort, order, offset, maxResult);
    }

    public int getTotalDepositsCount(String userID, String query) {
        return depositDAO.count(userID, query);
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
    
    public int count(String userId) { return depositDAO.count(userId, null); }

    public int queueCount(String userId) { return depositDAO.queueCount(userId); }

    public int inProgressCount(String userId) { return depositDAO.inProgressCount(userId); }

    public List<Deposit> inProgress() { return depositDAO.inProgress(); }

    public List<Deposit> completed() { return depositDAO.completed(); }

    public List<Deposit> search(String query, String sort, String order, String userId) {
        return this.depositDAO.search(query, sort, order, userId);
    }

    public Long size(String userId) { return depositDAO.size(userId); }
    
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

        logger.debug("MINUTE: "+getAuditPeriodMinutes());
        logger.debug("HOUR: "+getAuditPeriodHours());
        logger.debug("DAY_OF_YEAR: "+getAuditPeriodDays());
        logger.debug("MONTH: "+getAuditPeriodMonths());
        logger.debug("YEAR: "+getAuditPeriodYears());

        logger.debug("Max per Deposit: "+maxChunkAuditPerDeposit);
        logger.debug("Total Max: "+maxChunkPerAudit);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -getAuditPeriodMinutes()); // to get previous days
        cal.add(Calendar.HOUR, -getAuditPeriodHours()); // to get previous days
        cal.add(Calendar.DAY_OF_YEAR, -getAuditPeriodDays()); // to get previous days
        cal.add(Calendar.MONTH, -getAuditPeriodMonths()); // to get previous month
        cal.add(Calendar.YEAR, -getAuditPeriodYears()); // to get previous years
        Date olderThanDate = cal.getTime();

        logger.debug("older than date: "+olderThanDate);

        List<Deposit> deposits = depositDAO.getDepositsWaitingForAudit(olderThanDate);
        List<DepositChunk> chunksToAudit = new ArrayList<>();

        int totalCount = 0;
        for(Deposit deposit : deposits){
            logger.debug("Total Count: "+totalCount);
            logger.debug("check deposit: "+deposit.getID());

            List<DepositChunk> depositChunks = deposit.getDepositChunks();

            logger.debug("Number of chunks in deposit: " + depositChunks.size());

            Collections.sort(depositChunks, new Comparator<DepositChunk>(){
                public int compare(DepositChunk c1, DepositChunk c2) {
                    if(auditChunkStatusDAO.getLastChunkAuditTime(c1) == null){
                        return -1;
                    }else if(auditChunkStatusDAO.getLastChunkAuditTime(c2) == null){
                        return 1;
                    }else{
                        return auditChunkStatusDAO.getLastChunkAuditTime(c1).getTimestamp().compareTo(
                                auditChunkStatusDAO.getLastChunkAuditTime(c2).getTimestamp());
                    }
                }
            });

            int count = 0;
            for(DepositChunk chunk : depositChunks){
                logger.debug("Chunk in deposit count: "+count);
                if(totalCount >= maxChunkPerAudit) {
                    logger.debug("maxChunkPerAudit reached");
                    return chunksToAudit;
                }else if(count >= maxChunkAuditPerDeposit){
                    logger.debug("maxChunkAuditPerDeposit reached");
                    break; // get out of loop
                }

                logger.debug("check chunk: "+chunk.getID());

                AuditChunkStatus lastAuditChunkInfo = auditChunkStatusDAO.getLastChunkAuditTime(chunk);

                if(lastAuditChunkInfo == null){
                    logger.debug("add chunk, No previous audit.");
                    chunksToAudit.add(chunk);
                    count++; totalCount++;
                }else if( lastAuditChunkInfo.getTimestamp().before(olderThanDate) && (
                                lastAuditChunkInfo.getStatus().equals(AuditChunkStatus.Status.COMPLETE) ||
                                        lastAuditChunkInfo.getStatus().equals(AuditChunkStatus.Status.FIXED) ) ){
                    logger.debug("add chunk, last audit: "+lastAuditChunkInfo.getTimestamp());
                    chunksToAudit.add(chunk);
                    count++; totalCount++;
                }else{
                    logger.debug("too recent audit");
                }
            }


        }

        return chunksToAudit;
    }

    public int getAuditPeriodMinutes() {
        return auditPeriodMinutes;
    }


    public int getAuditPeriodHours() {
        return auditPeriodHours;
    }


    public int getAuditPeriodDays() {
        return auditPeriodDays;
    }


    public int getAuditPeriodMonths() {
        return auditPeriodMonths;
    }

    public int getAuditPeriodYears() {
        return auditPeriodYears;
    }

    public int getAuditMaxChunksPerDeposits() {
        return auditMaxChunksPerDeposits;
    }

    public int getAuditMaxTotalChunks() {
        return auditMaxTotalChunks;
    }

    public DepositChunk getDepositChunkById(String id){
        return depositChunkDAO.findById(id);
    }

    public void updateDepositChunk(DepositChunk chunk) {
        depositChunkDAO.update(chunk);
    }
}

