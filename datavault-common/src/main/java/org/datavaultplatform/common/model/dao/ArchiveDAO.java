package org.datavaultplatform.common.model.dao;


import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.Archive;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ArchiveDAO extends BaseDAO<Archive> {

  @Override
  @EntityGraph(Archive.EG_ARCHIVE)
  List<Archive> findAll();

  @Override
  @EntityGraph(Archive.EG_ARCHIVE)
  Optional<Archive> findById(String id);
}
