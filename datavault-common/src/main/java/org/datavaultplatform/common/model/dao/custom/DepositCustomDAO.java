package org.datavaultplatform.common.model.dao.custom;

import java.util.Date;
import java.util.List;
import org.datavaultplatform.common.model.Deposit;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DepositCustomDAO extends BaseCustomDAO {

    @EntityGraph(Deposit.EG_DEPOSIT)
    List<Deposit> list(String query, String userId, String sort, String order, int offset, int maxResult);

    int count(String userId, String query);

    int queueCount(String userId);

    int inProgressCount(String userId);

    @EntityGraph(Deposit.EG_DEPOSIT)
    List<Deposit> inProgress();

    @EntityGraph(Deposit.EG_DEPOSIT)
    List<Deposit> completed();

    @EntityGraph(Deposit.EG_DEPOSIT)
    List<Deposit> search(String query, String sort, String order, String userId);

    @EntityGraph(Deposit.EG_DEPOSIT)
    List<Deposit> getDepositsWaitingForAudit(Date olderThanDate);

    Long size(String userId);
}
