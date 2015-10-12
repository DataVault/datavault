package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Vault;
 
public interface VaultDAO {

    public void save(Vault vault);
    
    public void update(Vault vault);

    public List<Vault> list();

    public List<Vault> list(String sort);

    public Vault findById(String Id);

    public List<Vault> search(String query, String sort);

    public int count();
}
