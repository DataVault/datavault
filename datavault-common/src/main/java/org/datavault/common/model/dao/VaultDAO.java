package org.datavault.common.model.dao;

import java.util.List;
import org.datavault.common.model.Vault;
 
public interface VaultDAO {

    public void save(Vault vault);
    
    public List<Vault> list();

    public Vault findById(String Id);
    
}
