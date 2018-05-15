package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.metadata.Provider;
import org.datavaultplatform.common.metadata.impl.PureProvider;
import org.datavaultplatform.common.metadata.impl.TestPureProvider;
import org.datavaultplatform.common.model.dao.DatasetDAO;
import java.util.List;
import java.util.Map;

public class ExternalMetadataService {

    private String metadataURL;
    private Provider metadataProvider;
    private DatasetDAO datasetDAO;
    private UsersService usersService;

    public void setUsersService(UsersService usersService) {
		this.usersService = usersService;
	}
    
    public void setMetadataURL(String metadataURL) {
        this.metadataURL = metadataURL;
        
        if (metadataURL.equals("")) {
            this.metadataProvider = new TestPureProvider();
        } else {
            this.metadataProvider = new PureProvider(metadataURL);
        }
    }
    
    public List<Dataset> getDatasets(String userID) {
    	if (this.metadataProvider instanceof TestPureProvider) {
    		User user = this.usersService.getUser(userID);
    		Map<String, String> props = user.getProperties();
    		String employeeId = props.get("eduniRefno");
    		return metadataProvider.getDatasetsForUser(employeeId);
    	} else {
    		return metadataProvider.getDatasetsForUser(userID);
    	}
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
    
    public String getDatasetContent(String id) {
        
        // Query the metadata provider
        Dataset dataset = metadataProvider.getDataset(id);

        return dataset.getContent();
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
