package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.User;
import org.springframework.data.jpa.repository.EntityGraph;

public interface UserCustomDAO extends BaseCustomDAO {

    @EntityGraph(User.EG_USER)
    List<User> search(String query);
}
