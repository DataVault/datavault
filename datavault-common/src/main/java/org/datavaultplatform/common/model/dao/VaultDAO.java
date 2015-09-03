package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Vault;
 
public interface VaultDAO {

    public void save(Vault vault);
    
    public void update(Vault vault);
    
    public List<Vault> list();

    public Vault findById(String Id);
    
}
