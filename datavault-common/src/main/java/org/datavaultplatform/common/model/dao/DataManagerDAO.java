package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.DataManager;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DataManagerDAO extends BaseDAO<DataManager> {
  @Override
  @EntityGraph(DataManager.EG_DATA_MANAGER)
  Optional<DataManager> findById(String id);

  @Override
  @EntityGraph(DataManager.EG_DATA_MANAGER)
  List<DataManager> findAll();
}
