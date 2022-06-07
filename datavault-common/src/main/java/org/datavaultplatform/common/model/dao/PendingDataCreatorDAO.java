package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PendingDataCreator;
import org.datavaultplatform.common.model.dao.custom.PendingDataCreatorCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PendingDataCreatorDAO
    extends BaseDAO<PendingDataCreator>, PendingDataCreatorCustomDAO {
}
