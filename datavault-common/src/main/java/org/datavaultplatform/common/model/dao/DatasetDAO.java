package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Dataset;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DatasetDAO extends BaseDAO<Dataset> {

  @Override
  default List<Dataset> list() {
    return findAll();
  }
}
