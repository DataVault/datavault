package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.dao.AuditDAO;

import java.util.Date;
import java.util.List;

public class AuditsService {

    private AuditDAO auditDAO;
    
    public List<Audit> getAudits() {
        return auditDAO.list();
    }
    
    public void addAudit(Audit audit, List<DepositChunk> chunks) {
        
        Date d = new Date();
        audit.setTimestamp(d);

        audit.setStatus(Audit.Status.NOT_STARTED);

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

    public int count() { return auditDAO.count(); }

    public int queueCount() { return auditDAO.queueCount(); }

    public int inProgressCount() { return auditDAO.inProgressCount(); }

    public List<Audit>inProgress() { return auditDAO.inProgress(); }
}

