package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Deposit;

public interface DepositDAO {

    public void save(Deposit deposit);
    
    public void update(Deposit deposit);
    
    public List<Deposit> list(String sort, String userId);

    public Deposit findById(String Id);

    public int count(String userId);

    public int queueCount(String userId);

    public int inProgressCount(String userId);

    public List<Deposit> inProgress();

    public List<Deposit> completed();

    public List<Deposit> search(String query, String sort, String userId);

    public Long size(String userId);
}
