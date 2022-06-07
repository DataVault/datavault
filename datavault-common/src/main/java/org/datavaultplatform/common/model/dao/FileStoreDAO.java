package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.FileStore;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface FileStoreDAO extends BaseDAO<FileStore> {
}
