package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.metadata.*;
import org.datavaultplatform.common.metadata.impl.*;

import java.util.List;

public class ExternalMetadataService {

    Provider metadataProvider = new MockProvider();
    
    public List<Dataset> getDatasets(String userID) {
        return metadataProvider.getDatasetsForUser(userID);
    }
    
    public Dataset getDataset(String id) {
        return metadataProvider.getDataset(id);
    }
}
