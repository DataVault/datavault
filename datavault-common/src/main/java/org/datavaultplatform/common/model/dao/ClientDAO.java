package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.dao.custom.ClientCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ClientDAO extends BaseDAO<Client>, ClientCustomDAO {

  @Override
  @EntityGraph(Client.EG_CLIENT)
  Optional<Client> findById(String id);

  @Override
  @EntityGraph(Client.EG_CLIENT)
  List<Client> findAll();
}
