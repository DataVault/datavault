package org.datavaultplatform.common.model.dao.custom;

import java.util.Date;
import java.util.List;
import org.datavaultplatform.common.model.Deposit;

public interface DepositCustomDAO extends BaseCustomDAO {

    List<Deposit> list(String query, String userId, String sort, String order, int offset, int maxResult);

    int count(String userId, String query);

    int queueCount(String userId);

    int inProgressCount(String userId);

    List<Deposit> inProgress();

    List<Deposit> completed();

    List<Deposit> search(String query, String sort, String order, String userId);

    List<Deposit> getDepositsWaitingForAudit(Date olderThanDate);

    Long size(String userId);
}
