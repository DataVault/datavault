package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.dao.custom.PendingVaultCustomDAO;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PendingVaultDAO extends BaseDAO<PendingVault>, PendingVaultCustomDAO {

  @Override
  default List<PendingVault> list() {
    return findAll(Sort.by(Order.asc("creationTime")));
  }
}
