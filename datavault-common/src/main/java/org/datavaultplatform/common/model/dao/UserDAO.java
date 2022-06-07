package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.dao.custom.UserCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserDAO extends BaseDAO<User>, UserCustomDAO {
}
