package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.dao.custom.ArchiveStoreCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ArchiveStoreDAO extends BaseDAO<ArchiveStore>, ArchiveStoreCustomDAO {
  @Override
  @EntityGraph(ArchiveStore.EG_ARCHIVE_STORE)
  List<ArchiveStore> findAll();

  @Override
  @EntityGraph(ArchiveStore.EG_ARCHIVE_STORE)
  Optional<ArchiveStore> findById(String id);
}
