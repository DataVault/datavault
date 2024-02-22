package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.BillingInfo;
import org.datavaultplatform.common.model.dao.custom.BillingCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface BillingDAO extends BaseDAO<BillingInfo>, BillingCustomDAO {

  @Override
  @EntityGraph(BillingInfo.EG_BILLING_INFO)
  default List<BillingInfo> list() {
    return findAll();
  }

  default long getTotalNumberOfVaults() {
    return count();
  }

  @Override
  @EntityGraph(BillingInfo.EG_BILLING_INFO)
  Optional<BillingInfo> findById(String id);

  @Override
  @EntityGraph(BillingInfo.EG_BILLING_INFO)
  List<BillingInfo> findAll();

}
