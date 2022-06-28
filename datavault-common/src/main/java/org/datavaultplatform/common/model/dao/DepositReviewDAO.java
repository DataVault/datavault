package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.dao.custom.DepositReviewCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DepositReviewDAO extends BaseDAO<DepositReview>, DepositReviewCustomDAO {
  @Override
  @EntityGraph(DepositReview.EG_DEPOSIT_REVIEW)
  Optional<DepositReview> findById(String id);

  @Override
  @EntityGraph(DepositReview.EG_DEPOSIT_REVIEW)
  List<DepositReview> findAll();
}
