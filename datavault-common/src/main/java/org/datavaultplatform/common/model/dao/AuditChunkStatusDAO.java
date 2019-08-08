package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.DepositChunk;

import java.util.HashMap;
import java.util.List;

public interface AuditChunkStatusDAO {
    void save(AuditChunkStatus auditChunkStatus);

    void update(AuditChunkStatus auditChunkStatus);

    List<AuditChunkStatus> list();

    AuditChunkStatus findById(String Id);

    List<AuditChunkStatus> findByAudit(String auditId);

    List<AuditChunkStatus> findByDepositChunk(String depositChunkId);

    List<AuditChunkStatus> findBy(HashMap<String, Object> properties);

    AuditChunkStatus getLastChunkAuditTime(DepositChunk chunk);

    int count();
}
