package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.Retrieve;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface RetrieveCustomDAO extends BaseCustomDAO {

    @EntityGraph(Retrieve.EG_RETRIEVE)
    List<Retrieve> list(String userId);

    int count(String userId);

    int inProgressCount(String userId);

    @EntityGraph(Retrieve.EG_RETRIEVE)
    List<Retrieve> inProgress();

    int queueCount(String userId);
}
