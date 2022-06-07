package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.BillingInfo;
import org.datavaultplatform.common.model.dao.custom.BillingCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface BillingDAO extends BaseDAO<BillingInfo>, BillingCustomDAO {

  @Override
  default List<BillingInfo> list() {
    return findAll();
  }

  default long getTotalNumberOfVaults() {
    return count();
  }

}