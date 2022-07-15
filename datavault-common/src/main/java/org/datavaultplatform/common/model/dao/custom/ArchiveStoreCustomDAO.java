package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.ArchiveStore;
import org.springframework.data.jpa.repository.EntityGraph;


public interface ArchiveStoreCustomDAO extends BaseCustomDAO {

    @EntityGraph(ArchiveStore.EG_ARCHIVE_STORE)
    ArchiveStore findForRetrieval();
}
