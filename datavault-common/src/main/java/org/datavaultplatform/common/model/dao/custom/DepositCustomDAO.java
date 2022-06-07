package org.datavaultplatform.common.model.dao.custom;

import java.util.Date;
import java.util.List;
import org.datavaultplatform.common.model.Deposit;

public interface DepositCustomDAO {

    public void save(Deposit deposit);
    
    public void update(Deposit deposit);
    
    public List<Deposit> list(String query, String userId, String sort, String order, int offset, int maxResult);

    public Deposit findById(String Id);

    public int count(String userId, String query);

    public int queueCount(String userId);

    public int inProgressCount(String userId);

    public List<Deposit> inProgress();

    public List<Deposit> completed();

    public List<Deposit> search(String query, String sort, String order, String userId);

    public List<Deposit> getDepositsWaitingForAudit(Date olderThanDate);

    public Long size(String userId);
}
