package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.Retrieve;

public interface RetrieveCustomDAO extends BaseCustomDAO {
    
    List<Retrieve> list(String userId);

    int count(String userId);

    int inProgressCount(String userId);

    List<Retrieve> inProgress();

    int queueCount(String userId);
}
