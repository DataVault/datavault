package org.datavault.common.model.dao;

import java.util.List;
import org.datavault.common.model.Deposit;
 
public interface DepositDAO {

    public void save(Deposit deposit);
    
    public void update(Deposit deposit);
    
    public List<Deposit> list();

    public Deposit findById(String Id);
    
}
