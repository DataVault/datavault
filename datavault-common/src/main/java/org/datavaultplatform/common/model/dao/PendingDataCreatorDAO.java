package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.PendingDataCreator;
import org.datavaultplatform.common.model.dao.custom.PendingDataCreatorCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PendingDataCreatorDAO
    extends BaseDAO<PendingDataCreator>, PendingDataCreatorCustomDAO {

  @Override
  @EntityGraph(PendingDataCreator.EG_PENDING_DATA_CREATOR)
  Optional<PendingDataCreator> findById(String id);

  @Override
  @EntityGraph(PendingDataCreator.EG_PENDING_DATA_CREATOR)
  List<PendingDataCreator> findAll();
}
