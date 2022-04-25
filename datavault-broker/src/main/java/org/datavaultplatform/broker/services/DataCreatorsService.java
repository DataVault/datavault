package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.DataCreator;
import org.datavaultplatform.common.model.dao.DataCreatorDAO;
import org.datavaultplatform.common.model.dao.PendingDataCreatorDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.springframework.stereotype.Service;
@Service
public class DataCreatorsService {
    private DataCreatorDAO dataCreatorDAO;
    private final Logger logger = LoggerFactory.getLogger(DataCreatorsService.class);

    public void setDataCreatorDAO(DataCreatorDAO dataCreatorDAO) {
        this.dataCreatorDAO = dataCreatorDAO;
    }

    public void addCreators(List<DataCreator> creators) {

        if (creators != null  && ! creators.isEmpty()) {
            this.dataCreatorDAO.save(creators);
        }
    }

    public void deletePendingDataCreator(String creatorId) {
        if (dataCreatorDAO.findById(creatorId) == null) {
            throw new IllegalStateException("Cannot delete a role assignment that does not exist");
        }
        this.dataCreatorDAO.delete(creatorId);
    }
}
