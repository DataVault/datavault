package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PendingDataCreator;

import java.util.List;

public interface PendingDataCreatorDAO {
    void save(List<PendingDataCreator> pendingDataCreators);

    PendingDataCreator findById(String Id);

    void update(PendingDataCreator pendingDataCreator);
}
