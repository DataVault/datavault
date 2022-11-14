package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.dao.custom.DepositChunkCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DepositChunkDAO extends BaseDAO<DepositChunk>, DepositChunkCustomDAO {
  @Override
  @EntityGraph(DepositChunk.EG_DEPOSIT_CHUNK)
  Optional<DepositChunk> findById(String id);

  @Override
  @EntityGraph(DepositChunk.EG_DEPOSIT_CHUNK)
  List<DepositChunk> findAll();
}
