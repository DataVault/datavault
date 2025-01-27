package org.datavaultplatform.common.model.dao.custom;

import java.util.List;

import org.datavaultplatform.common.model.Retrieve;
import org.springframework.data.jpa.repository.EntityGraph;

public interface RetrieveCustomDAO extends BaseCustomDAO {

    @EntityGraph(Retrieve.EG_RETRIEVE)
    List<Retrieve> list(String query, String userId, String sort, String order, int offset, int maxResult);

    int count(String userId, String query);

    int inProgressCount(String userId);

    @EntityGraph(Retrieve.EG_RETRIEVE)
    List<Retrieve> inProgress();

    int queueCount(String userId);
}
