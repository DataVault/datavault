package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.dao.custom.RetrieveCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RetrieveDAO extends BaseDAO<Retrieve>, RetrieveCustomDAO {

  @Override
  @EntityGraph(Retrieve.EG_RETRIEVE)
  Optional<Retrieve> findById(String id);

  @Override
  @EntityGraph(Retrieve.EG_RETRIEVE)
  List<Retrieve> findAll();

}
