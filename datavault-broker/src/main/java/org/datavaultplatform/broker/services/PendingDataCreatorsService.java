package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.PendingDataCreator;
import org.datavaultplatform.common.model.dao.PendingDataCreatorDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class PendingDataCreatorsService {
    private final PendingDataCreatorDAO pendingDataCreatorDAO;
    private final Logger logger = LoggerFactory.getLogger(PendingDataCreatorsService.class);

    @Autowired
    public PendingDataCreatorsService(PendingDataCreatorDAO pendingDataCreatorDAO) {
        this.pendingDataCreatorDAO = pendingDataCreatorDAO;
    }

    public void addPendingCreators(List<PendingDataCreator> creators) {

        if (creators != null  && ! creators.isEmpty()) {
            this.pendingDataCreatorDAO.save(creators);
        }
    }

    public void deletePendingDataCreator(String creatorId) {
        if (pendingDataCreatorDAO.findById(creatorId) == null) {
            throw new IllegalStateException("Cannot delete a role assignment that does not exist");
        }
        this.pendingDataCreatorDAO.delete(creatorId);
    }
}
