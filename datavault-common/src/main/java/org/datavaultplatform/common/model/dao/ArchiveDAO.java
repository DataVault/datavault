package org.datavaultplatform.common.model.dao;


import org.datavaultplatform.common.model.Archive;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ArchiveDAO extends BaseDAO<Archive> {
}
