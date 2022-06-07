package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DepositReview;

public interface DepositReviewCustomDAO {

    public void save(DepositReview depositReview);
    
    public void update(DepositReview depositReview);
    
    public List<DepositReview> list();

    public DepositReview findById(String Id);

    public List<DepositReview> search(String query);

    public int count();
}
