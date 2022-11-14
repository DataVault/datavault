package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.Group;
import org.springframework.data.jpa.repository.EntityGraph;


public interface GroupCustomDAO extends BaseCustomDAO {

    @EntityGraph(Group.EG_GROUP)
    List<Group> list(String userId);

    int count(String userId);
}
