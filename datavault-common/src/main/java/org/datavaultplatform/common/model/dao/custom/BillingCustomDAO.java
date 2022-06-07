package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.BillingInfo;

public interface BillingCustomDAO extends BaseCustomDAO {

  List<BillingInfo> list(String sort, String order, String offset, String maxResult);

  List<BillingInfo> search(String query, String sort, String order, String offset, String maxResult);

  Long countByQuery(String query);
}
