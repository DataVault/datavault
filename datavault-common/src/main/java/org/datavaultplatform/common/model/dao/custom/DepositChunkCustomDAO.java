package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DepositChunk;

public interface DepositChunkCustomDAO extends BaseCustomDAO {
  List<DepositChunk> list(String sort);
}
