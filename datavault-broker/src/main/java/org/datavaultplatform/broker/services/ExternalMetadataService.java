package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.metadata.*;
import org.datavaultplatform.common.metadata.impl.*;
import java.util.List;

public class ExternalMetadataService {

    private String metadataURL;
    private Provider metadataProvider;
    
    public void setMetadataURL(String metadataURL) {
        this.metadataURL = metadataURL;
        
        if (metadataURL.equals("")) {
            this.metadataProvider = new MockProvider();
        } else {
            this.metadataProvider = new PureProvider(metadataURL);
        }
    }
    
    public List<Dataset> getDatasets(String userID) {
        return metadataProvider.getDatasetsForUser(userID);
    }
    
    public Dataset getDataset(String id) {
        return metadataProvider.getDataset(id);
    }
}
