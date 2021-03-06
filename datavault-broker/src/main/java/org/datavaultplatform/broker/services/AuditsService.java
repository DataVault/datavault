package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.AuditDAO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AuditsService {

    private AuditDAO auditDAO;
    private AuditChunkStatusDAO auditChunkStatusDAO;
    
    public List<Audit> getAudits() {
        return auditDAO.list();
    }

    public List<AuditChunkStatus> getAllAuditChunkStatus() {
        return auditChunkStatusDAO.list();
    }

    public List<AuditChunkStatus> getAuditChunkStatus(Audit audit) {
        return auditChunkStatusDAO.findByAudit(audit);
    }
    
    public void addAudit(Audit audit, List<DepositChunk> chunks) {
        
        Date d = new Date();
        audit.setTimestamp(d);

        audit.setStatus(Audit.Status.IN_PROGRESS);

        audit.setDepositChunks(chunks);

        auditDAO.save(audit);
    }
    
    public void updateAudit(Audit Audit) {
        auditDAO.update(Audit);
    }
    
    public Audit getAudit(String AuditID) {
        return auditDAO.findById(AuditID);
    }
    
    public void setAuditDAO(AuditDAO auditDAO) {
        this.auditDAO = auditDAO;
    }

    public void setAuditChunkStatusDAO(AuditChunkStatusDAO auditChunkStatusDAO) {
        this.auditChunkStatusDAO = auditChunkStatusDAO;
    }

    public AuditChunkStatus addAuditStatus(Audit audit, DepositChunk chunk, String archiveId, String location){
        // Create new AuditChunkStatus
        AuditChunkStatus auditChunkStatus = new AuditChunkStatus();
        auditChunkStatus.setAudit(audit);
        auditChunkStatus.setDepositChunk(chunk);
        auditChunkStatus.setTimestamp(new Date());
        auditChunkStatus.setArchiveId(archiveId);
        auditChunkStatus.setLocation(location);
        auditChunkStatus.started();

        auditChunkStatusDAO.save(auditChunkStatus);

        return auditChunkStatus;
    }

    public List<AuditChunkStatus> getRunningAuditChunkStatus(Audit audit, DepositChunk chunk, String archiveId, String location){
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("audit", audit);
        properties.put("depositChunk", chunk);
        properties.put("archiveId", archiveId);
        if(location != null) properties.put("location", location);
        properties.put("status", AuditChunkStatus.Status.IN_PROGRESS);
        List<AuditChunkStatus> chunkStatusList = this.auditChunkStatusDAO.findBy(properties);

        return chunkStatusList;
    }

    public List<AuditChunkStatus> getAuditChunkStatusFromAudit(Audit audit){
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("audit", audit);
        List<AuditChunkStatus> chunkStatusList = this.auditChunkStatusDAO.findBy(properties);

        return chunkStatusList;
    }

    public void updateAuditChunkStatus(AuditChunkStatus auditChunkStatus) {
        auditChunkStatusDAO.update(auditChunkStatus);
    }

    public int count() { return auditDAO.count(); }
}

