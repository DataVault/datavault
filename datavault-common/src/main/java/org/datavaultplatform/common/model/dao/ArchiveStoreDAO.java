package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.dao.custom.ArchiveStoreCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ArchiveStoreDAO extends BaseDAO<ArchiveStore>, ArchiveStoreCustomDAO {
}
