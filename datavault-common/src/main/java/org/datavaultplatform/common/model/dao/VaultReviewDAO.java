package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.custom.VaultReviewCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface VaultReviewDAO extends BaseDAO<VaultReview>, VaultReviewCustomDAO {
  @Override
  @EntityGraph(VaultReview.EG_VAULT_REVIEW)
  Optional<VaultReview> findById(String id);

  @Override
  @EntityGraph(VaultReview.EG_VAULT_REVIEW)
  List<VaultReview> findAll();
}
