package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.DepositPath;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DepositPathDAO extends BaseDAO<DepositPath> {

    @EntityGraph(DepositChunk.EG_DEPOSIT_CHUNK)
    void deleteAllByDepositId(String depositId);

}
