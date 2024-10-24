package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.AuditDAO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditsService {

    private final AuditDAO auditDAO;
    private final AuditChunkStatusDAO auditChunkStatusDAO;

    @Autowired
    public AuditsService(AuditDAO auditDAO, AuditChunkStatusDAO auditChunkStatusDAO) {
        this.auditDAO = auditDAO;
        this.auditChunkStatusDAO = auditChunkStatusDAO;
    }

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
        return auditDAO.findById(AuditID).orElse(null);
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
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(PropNames.AUDIT, audit);
        properties.put(PropNames.DEPOSIT_CHUNK, chunk);
        properties.put(PropNames.ARCHIVE_ID, archiveId);
        if (location != null) {
            properties.put(PropNames.LOCATION, location);
        }
        properties.put(PropNames.STATUS, AuditChunkStatus.Status.IN_PROGRESS);
        List<AuditChunkStatus> chunkStatusList = this.auditChunkStatusDAO.findBy(properties);

        return chunkStatusList;
    }

    public List<AuditChunkStatus> getAuditChunkStatusFromAudit(Audit audit){
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(PropNames.AUDIT, audit);
        List<AuditChunkStatus> chunkStatusList = this.auditChunkStatusDAO.findBy(properties);

        return chunkStatusList;
    }

    public void updateAuditChunkStatus(AuditChunkStatus auditChunkStatus) {
        auditChunkStatusDAO.update(auditChunkStatus);
    }

    public long count() { return auditDAO.count(); }
}

