package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface UserCustomDAO extends BaseCustomDAO {

    @EntityGraph(User.EG_USER)
    List<User> search(String query);

    @EntityGraph(User.EG_USER)
    @Query("SELECT u FROM User u WHERE u.email IS NULL OR u.email NOT LIKE '%@%'")
    List<User> findUsersWithInvalidEmail();
}