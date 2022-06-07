package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.dao.custom.DepositReviewCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DepositReviewDAO extends BaseDAO<DepositReview>, DepositReviewCustomDAO {
}
