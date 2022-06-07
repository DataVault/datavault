package org.datavaultplatform.broker.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.datavaultplatform.common.metadata.Provider;
import org.datavaultplatform.common.metadata.impl.MockProvider;
import org.datavaultplatform.common.metadata.impl.PureFlatFileProvider;
import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.dao.DatasetDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExternalMetadataService {

    private final String metadataURL;
    private final String flatFileLocation;
    private final Provider metadataProvider;
    private final DatasetDAO datasetDAO;
    private final UsersService usersService;
    public static final String EDUNIREFNO = "edunirefno";


    /*
     <bean id="externalMetadataService" class="org.datavaultplatform.broker.services.ExternalMetadataService">
    	  <property name="flatFileLocation" value="${flatfile.location:/tmp/}" />
        <property name="metadataURL" value="${metadata.url}" />
        <property name="datasetDAO" ref="datasetDAO" />
        <property name="usersService" ref="usersService" />
    </bean>
     */
  @Autowired
  public ExternalMetadataService(
      @Value("${metadata.url}") String metadataURL,
      @Value("${flatfile.location:/tmp/}") String flatFileLocation,
      DatasetDAO datasetDAO,
      UsersService usersService) {
    this.metadataURL = metadataURL;
    this.flatFileLocation = flatFileLocation;
    this.datasetDAO = datasetDAO;
    this.usersService = usersService;

    if (metadataURL.equals("")) {
      this.metadataProvider = new MockProvider();
    } else {
      Provider prov = new PureFlatFileProvider();
      ((PureFlatFileProvider) prov).setFlatFileDir(this.flatFileLocation);
      this.metadataProvider = prov;
      //this.metadataProvider = new PureProvider(metadataURL);
    }
  }

    public List<Dataset> getDatasets(String userID) {
    	System.out.println("Getting Datasets for UUN " + userID);
    	if (this.metadataProvider instanceof PureFlatFileProvider) {
    		User user = this.usersService.getUser(userID);
    		if (user != null) {
    			System.out.println("User surname " + user.getLastname());
	    		Map<String, String> props = user.getProperties();
	    		System.out.println("Setting default employee id for testing:123363");
    			String employeeId = "123363";

    			if (props != null) {
    				System.out.println("Props is not null");
    				for (String key : props.keySet()) {
    					System.out.println("key is :'" + "'" + key + " value is :'" + props.get(key) + "'");
    				}
    			}
	    		if (props != null  && props.get(ExternalMetadataService.EDUNIREFNO) != null) {
	    			System.out.println("Getting employeeId from properties");
		    		employeeId = props.get(ExternalMetadataService.EDUNIREFNO);
	    		}
	    		return metadataProvider.getDatasetsForUser(employeeId);
    		}
    	} else {
    		return metadataProvider.getDatasetsForUser(userID);
    	}
    	
    	return new ArrayList<>();
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
    
    public Map<String, String> getPureProjectIds() {
    	Map<String, String> projectIds = metadataProvider.getPureProjectIds();
    
    	return projectIds;
    }
    
    public String getPureProjectId(String datasetId) {
    	String projectId = metadataProvider.getPureProjectId(datasetId);
    	
    	return projectId;
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
        return datasetDAO.findById(id).orElse(null);
    }
    
}
