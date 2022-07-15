package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.dao.custom.DepositCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DepositDAO extends BaseDAO<Deposit>, DepositCustomDAO {

  @Override
  @EntityGraph(Deposit.EG_DEPOSIT)
  Optional<Deposit> findById(String id);

  @Override
  @EntityGraph(Deposit.EG_DEPOSIT)
  List<Deposit> findAll();
}
