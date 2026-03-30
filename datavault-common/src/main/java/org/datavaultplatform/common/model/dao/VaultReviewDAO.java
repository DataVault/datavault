package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.custom.VaultReviewCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


  @EntityGraph(VaultReview.EG_VAULT_REVIEW)
  List<VaultReview> findByVaultId(String vaultId);

  // should be the latest where actioned date is not null
  @Query("""
          SELECT vr FROM VaultReview vr WHERE vr.vault.id = :vaultId
          AND vr.creationTime  = (
                    select max(vr2.creationTime) from VaultReview vr2
                    where vr2.vault.id = :vaultId
                    and   vr2.actionedDate IS NOT NULL
          )
          """)
  Optional<VaultReview> findLatestSubmittedVaultReview(@Param("vaultId") String vaultId);

  // should be the latest AND also have null actioned date
  @Query("""
          SELECT vr FROM VaultReview vr WHERE vr.vault.id = :vaultId
          AND vr.creationTime = (
                       select max(vr2.creationTime) from VaultReview vr2
                       where vr2.vault.id = :vaultId)
          AND vr.actionedDate IS NULL
          """)
  Optional<VaultReview> findUnderwayVaultReview(@Param("vaultId") String vaultId);
}
