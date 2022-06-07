package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.ArchiveStore;


public interface ArchiveStoreCustomDAO extends BaseCustomDAO {

    ArchiveStore findForRetrieval();
}
