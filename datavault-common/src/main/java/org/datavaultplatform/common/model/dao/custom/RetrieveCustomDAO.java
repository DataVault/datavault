package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.Retrieve;
import org.springframework.data.jpa.repository.EntityGraph;

public interface RetrieveCustomDAO extends BaseCustomDAO {

    @EntityGraph(Retrieve.EG_RETRIEVE)
    List<Retrieve> list(String userId);

    int count(String userId);

    int inProgressCount(String userId);

    @EntityGraph(Retrieve.EG_RETRIEVE)
    List<Retrieve> inProgress();

    int queueCount(String userId);
}
