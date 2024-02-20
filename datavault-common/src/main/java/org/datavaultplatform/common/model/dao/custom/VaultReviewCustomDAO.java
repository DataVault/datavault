package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.VaultReview;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface VaultReviewCustomDAO extends BaseCustomDAO {

    @EntityGraph(VaultReview.EG_VAULT_REVIEW)
    List<VaultReview> search(String query);
}
