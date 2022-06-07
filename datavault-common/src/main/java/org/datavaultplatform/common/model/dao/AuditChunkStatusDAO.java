package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.dao.custom.AuditChunkStatusCustomDAO;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AuditChunkStatusDAO extends BaseDAO<AuditChunkStatus>, AuditChunkStatusCustomDAO {
  @Override
  default List<AuditChunkStatus> list() {
    return findAll(Sort.by(Order.asc("timestamp")));
  }

  List<AuditChunkStatus> findByDepositChunkId(String depositChunkId);

  @Query(value = "SELECT distinct acs FROM AuditChunkStatus acs WHERE acs.depositChunk.deposit = :deposit order by acs.timestamp asc")
  List<AuditChunkStatus> findByDeposit(Deposit deposit);

  @Query(value = "SELECT distinct acs FROM AuditChunkStatus acs WHERE acs.depositChunk.deposit.id = :depositId order by acs.timestamp asc")
  List<AuditChunkStatus> findByDepositId(String depositId);

}
