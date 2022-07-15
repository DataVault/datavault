package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.BillingInfo;
import org.springframework.data.jpa.repository.EntityGraph;

public interface BillingCustomDAO extends BaseCustomDAO {

  @EntityGraph(BillingInfo.EG_BILLING_INFO)
  List<BillingInfo> list(String sort, String order, String offset, String maxResult);

  @EntityGraph(BillingInfo.EG_BILLING_INFO)
  List<BillingInfo> search(String query, String sort, String order, String offset, String maxResult);

  Long countByQuery(String query);
}
