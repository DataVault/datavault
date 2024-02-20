package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.Group;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;


public interface GroupCustomDAO extends BaseCustomDAO {

    @EntityGraph(Group.EG_GROUP)
    List<Group> list(String userId);

    int count(String userId);
}
