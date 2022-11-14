package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.VaultReview;
import org.springframework.data.jpa.repository.EntityGraph;

public interface VaultReviewCustomDAO extends BaseCustomDAO {

    @EntityGraph(VaultReview.EG_VAULT_REVIEW)
    List<VaultReview> search(String query);
}
