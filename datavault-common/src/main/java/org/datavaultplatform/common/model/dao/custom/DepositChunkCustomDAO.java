package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DepositChunk;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DepositChunkCustomDAO extends BaseCustomDAO {
  @EntityGraph(DepositChunk.EG_DEPOSIT_CHUNK)
  List<DepositChunk> list(String sort);
}
