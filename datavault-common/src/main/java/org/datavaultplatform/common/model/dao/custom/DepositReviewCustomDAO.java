package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DepositReview;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DepositReviewCustomDAO extends BaseCustomDAO {

    @EntityGraph(DepositReview.EG_DEPOSIT_REVIEW)
    List<DepositReview> search(String query);

}
