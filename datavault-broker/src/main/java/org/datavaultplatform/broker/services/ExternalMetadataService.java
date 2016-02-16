package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.common.metadata.*;
import org.datavaultplatform.common.metadata.impl.*;
import org.datavaultplatform.common.model.dao.DatasetDAO;
import java.util.List;

public class ExternalMetadataService {

    private String metadataURL;
    private Provider metadataProvider;
    private DatasetDAO datasetDAO;
    
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
        
        // First check in the local database ...
        Dataset dataset = getCachedDataset(id);
        
        if (dataset == null) {
            // Query the metadata provider
            dataset = metadataProvider.getDataset(id);
        }
        
        return dataset;
    }
    
    public List<Dataset> getCachedDatasets() {
        return datasetDAO.list();
    }
    
    public void addCachedDataset(Dataset dataset) {
        
        datasetDAO.save(dataset);
    }
    
    public void updateCachedDataset(Dataset dataset) {
        datasetDAO.update(dataset);
    }
    
    public Dataset getCachedDataset(String id) {
        return datasetDAO.findById(id);
    }
    
    public void setDatasetDAO(DatasetDAO datasetDAO) {
        this.datasetDAO = datasetDAO;
    }
}
