package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.PendingDataCreator;

public interface PendingDataCreatorCustomDAO {
    void save(List<PendingDataCreator> pendingDataCreators);

    PendingDataCreator findById(String Id);

    void update(PendingDataCreator pendingDataCreator);

    void delete(String id);
}
