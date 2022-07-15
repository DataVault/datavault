package org.datavaultplatform.common.model.dao.custom;

import java.util.HashMap;
import java.util.List;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.DepositChunk;
import org.springframework.data.jpa.repository.EntityGraph;

public interface AuditChunkStatusCustomDAO extends BaseCustomDAO {

    @EntityGraph(AuditChunkStatus.EG_AUDIT_CHUNK_STATUS)
    List<AuditChunkStatus> findByAudit(Audit audit);

    @EntityGraph(AuditChunkStatus.EG_AUDIT_CHUNK_STATUS)
    List<AuditChunkStatus> findByDepositChunk(DepositChunk depositChunk);

    @EntityGraph(AuditChunkStatus.EG_AUDIT_CHUNK_STATUS)
    List<AuditChunkStatus> findBy(HashMap<String, Object> properties);

    @EntityGraph(AuditChunkStatus.EG_AUDIT_CHUNK_STATUS)
    List<AuditChunkStatus> findBy(String propertyName, Object propertyValue);

    @EntityGraph(AuditChunkStatus.EG_AUDIT_CHUNK_STATUS)
    AuditChunkStatus getLastChunkAuditTime(DepositChunk chunk);

}
