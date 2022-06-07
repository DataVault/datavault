package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.dao.custom.ClientCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ClientDAO extends BaseDAO<Client>, ClientCustomDAO {
}
