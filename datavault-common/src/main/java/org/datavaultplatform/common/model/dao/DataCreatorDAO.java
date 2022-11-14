package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.DataCreator;
import org.datavaultplatform.common.model.dao.custom.DataCreatorCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DataCreatorDAO
    extends BaseDAO<DataCreator>, DataCreatorCustomDAO {

  @Override
  @EntityGraph(DataCreator.EG_DATA_CREATOR)
  Optional<DataCreator> findById(String id);

  @Override
  @EntityGraph(DataCreator.EG_DATA_CREATOR)
  List<DataCreator> findAll();
}
