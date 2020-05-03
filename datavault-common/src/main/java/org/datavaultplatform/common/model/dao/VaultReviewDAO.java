package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.VaultReview;

import java.util.List;

public interface VaultReviewDAO {

    public void save(VaultReview vaultReview);
    
    public void update(VaultReview vaultReview);
    
    public List<VaultReview> list();

    public VaultReview findById(String Id);

    public List<VaultReview> search(String query);

    public int count();
}
