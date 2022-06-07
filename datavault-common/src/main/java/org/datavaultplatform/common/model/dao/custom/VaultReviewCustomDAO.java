package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.VaultReview;

public interface VaultReviewCustomDAO extends BaseCustomDAO {

    List<VaultReview> search(String query);
}
