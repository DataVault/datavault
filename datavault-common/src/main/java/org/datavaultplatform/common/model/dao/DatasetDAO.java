package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.Dataset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DatasetDAO extends BaseDAO<Dataset> {

  @Override
  @EntityGraph(Dataset.EG_DATASET)
  default List<Dataset> list() {
    return findAll();
  }

  @Override
  @EntityGraph(Dataset.EG_DATASET)
  Optional<Dataset> findById(String id);

  @Override
  @EntityGraph(Dataset.EG_DATASET)
  List<Dataset> findAll();

}
