package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.User;

public interface UserCustomDAO extends BaseCustomDAO {

    List<User> search(String query);
}
