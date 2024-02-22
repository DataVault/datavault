package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.FileStore;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface FileStoreDAO extends BaseDAO<FileStore> {

  @Override
  @EntityGraph(FileStore.EG_FILE_STORE)
  Optional<FileStore> findById(String id);

  @Override
  @EntityGraph(FileStore.EG_FILE_STORE)
  List<FileStore> findAll();
}
