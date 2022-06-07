package org.datavaultplatform.common.model.dao.custom;

import java.util.HashMap;
import java.util.List;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.DepositChunk;

public interface AuditChunkStatusCustomDAO extends BaseCustomDAO {

    List<AuditChunkStatus> findByAudit(Audit audit);

    List<AuditChunkStatus> findByDepositChunk(DepositChunk depositChunk);

    List<AuditChunkStatus> findBy(HashMap<String, Object> properties);

    List<AuditChunkStatus> findBy(String propertyName, Object propertyValue);

    AuditChunkStatus getLastChunkAuditTime(DepositChunk chunk);

}
