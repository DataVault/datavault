package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.DataCreator;
import org.datavaultplatform.common.model.dao.custom.DataCreatorCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DataCreatorDAO
    extends BaseDAO<DataCreator>, DataCreatorCustomDAO {
}
