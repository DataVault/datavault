package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DepositReview;

public interface DepositReviewCustomDAO extends BaseCustomDAO {

    List<DepositReview> search(String query);

}
